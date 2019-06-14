

Bitmap 是 Android 应用中的内存大户，网上有不少关于如何计算 Bitmap 占用内存大小的文章。个人觉得 Bitmap 内存大小很重要，但其内存是如何被管理(分配和回收)同样也很重要。所以本文试图从整体上看一下 Bitmap 创建和回收过程。我力求理清其主脉络，聚焦内存分配与回收，所以忽略掉许多技术细节。可能存在疏漏欢迎批评指正。

<!--more-->

Bitmap 占用内存多是因为其像素数据(pixels)大。像素数据的存储在不同 Android 版本之间有所变化。具体来说：

+ Android 2.3 (API Level 10) 以及之前 - 像素数据保存在 native heap
+ Android 3.0 到 Android 7.1 (API Level 11-26) - 像素数据保存在 java heap
+ Android 8.0 以及之后 - 像素数据保存在 native heap

像素数据的存储方式的变化，导致 Bitmap 内存管理也有所变化。所以相关代码在不同 Android 版本之间差异有时非常大。本文分析基于 [Android 8.0 源码](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Bitmap.cpp)分析。

# 总览

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-arch.png)

# 创建

Android SDK 中创建 Bitmap 的 API 超级多，整理后可以分成三种情况：

+ 从无到有 **创建** Bitmap - Bitmap.createBitmap()
+ 从已有的 Bitmap **拷贝** - Bitmap.copy()
+ 从已有的资源 **解码**
  + BitmapFactory.decodeResource()
  + [ImageDecoder.decodeBitmap](https://developer.android.com/reference/android/graphics/ImageDecoder)

[ImageDecoder](https://developer.android.com/reference/android/graphics/ImageDecoder) 是 Android 9.0 新加的类。不明白为什么官方要加这个类，还嫌以前的接口不够乱么。

以上说的会直接创建 Bitmap 的 API，实际上加载布局或资源文件时也可能会创建 Bitmap：

```xml
<ImageView android:src="@drawable/resId">
```

```java
Drawable drawable = Resources.getDrawable(resId)
```

Bitmap 是一个"重"资源且管理 Bitmap 并不是个简单活：

+ 解码过程耗CPU - 中低端手机上解码一张中等大小(752x942)的PNG图片耗时超过 20ms。更多测试数据见 [Bitmap 解码性能测试](https://www.sunmoonblog.com/2019/05/31/bitmap-decode-perf/)
+ 像素数据耗内存 - 一张大小 4048x3036 的图片占用内存约 12MB

官方直接在 [Managing Bitmap Memory](https://developer.android.com/topic/performance/graphics/manage-memory) 这个文档的开头就提醒开发者没事不要瞎折腾：

> Note: For most cases, we recommend that you use the Glide library to fetch, decode, and display bitmaps in your app. Glide abstracts out most of the complexity in handling these and other tasks related to working with bitmaps and other images on Android. For information about using and downloading Glide, visit the Glide repository on GitHub.

所以多数项目会使用 [Glide](https://github.com/bumptech/glide) 或 [Picosso](https://github.com/square/picasso) 加载图片，而非系统提供的 API。第三方库的使用让 Bitmap 的创建过程变得更加复杂。

上面啰嗦了这么多，总结就是看起来创建 Bitmap 的方式特别多：

+ 你可以通过N多的 API 来创建 Bitmap
+ 加载布局或资源时可能也创建了 Bitmap
+ 第三方库也会创建 Bitmap

但稍加分析就能发现无论哪种方式创建 Bitmap 最终都会走到相同的方法调用。见下图：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-overview.png)

可以粗略总结成四个步骤：

+ 资源转换 - `nativeDecodeXXX()` 负责将 Java 层传来的不同类型的资源转换成可解码的数据类型
+ 内存分配 - 准确地说这一步是内存管理，但简单起见我们只关注内存分配。分配的内存用于图片解码
+ 图片解码 - Skia 将实际的解码工作交由第三方库，第三库的解码结果填在上一步分配的内存中
+ 创建 Java 对象 - 将填有解码数据的内存块包装成 Java 层的 `android.graphics.Bitmap` 对象

上图中 [BitmapFactory.doDecode()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#233) 是核心，其关键步骤如下：

1. Update with options supplied by the client.
2. Create the codec.
3. Handle sampleSize. (跟 BitmapFactory.Options.inSampleSize 参数相关)
4. Set the decode colorType.
5. Handle scale.  (跟 BitmapFactory.Options.inScaled 参数相关)
6. Handle reuseBitmap (跟 BitmapFactory.Options.inBitmap 参数相关)
7. Choose decodeAllocator
8. Construct a color table
9. AllocPixels 
10. Use SkAndroidCodec to perform the decode.
11. Create the java bitmap

我们稍后对其中分配内存相关部分(包括第7, 9, 10步)详细讨论。

## 资源转换
Java 层的待解码资源包括：

+ File
+ Resource
+ ByteArray
+ Stream
+ FileDescriptor

到了 JNI 层待解码的资源被重新分类成四种，包括：

+ FileDescriptor
+ Asset
+ ByteArray
+ DecodeAsset

`BitmapFactory` 提供四个方法将对应的资源转换成 `SkStreamRewindable` 兼容的数据类型，然后统一交由 [BitmapFactory.doDecode()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#233)。 

```cpp
nativeDecodeFileDescriptor()
nativeDecodeAsset()
nativeDecodeByteArray()
nativeDecodeAsset()
```

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-convert-resource.png)

### 内存分配
前面提到 `BitmapFactory.doDecode()` 的第7步是选择 decodeAllocator。decodeAllocator 负责具体如何分配内存。有不同种类的 Allocator：

+ [ScaleCheckingAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#144) - 复用 Bitmap 且需要缩放
+ [RecyclingPixelAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/GraphicsJNI.h#171) - 复用 Bitmap 但不需要缩放
+ [SkBitmap::HeapAllocator](https://github.com/google/skia/blob/master/include/core/SkBitmap.h#L1119) - 不复用 Bitmap，但需要缩放或者是GPU Bitmap
+ [HeapAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/GraphicsJNI.h#125) - 如果不满足上述几种情况，就使用缺省的 Allocator。注：其父类是 [SkBRDAllocator](https://github.com/google/skia/blob/master/include/android/SkBRDAllocator.h)

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-createion-allocator-classes.png)

如何选择 Allocator 需要考虑因素包括：

+ 是否复用 Bitmap - inBitmap
+ 是否缩放 Bitmap - inScaled
+ 是否 Hardware Bitmap - isHardware

|是否复用 Bitmap|是否缩放 Bitmap|是否 Hardware Bitmap|Allocator类型|
|--------------|--------------|-------------|------------|
|是            |是            |-            |ScaleCheckingAllocator|
|是            |否            |-            |RecyclingPixelAllocator|
|否            |是            |是           |SkBitmap::HeapAllocator|
|-             |-            |-            |HeapAllocator|

说明：

+ 不复用 Bitmap，但需要缩放或者是 GPU Bitmap 时使用 SkBitmap::HeapAllocator
+ 缺省使用 Allocator

```cpp
// BitmapFactory.cpp
static jobject doDecode() {
    ...
    SkBitmap decodingBitmap;
    if (!decodingBitmap.setInfo(bitmapInfo) ||
            !decodingBitmap.tryAllocPixels(decodeAllocator, colorTable.get())) {
        // SkAndroidCodec should recommend a valid SkImageInfo, so setInfo()
        // should only only fail if the calculated value for rowBytes is too
        // large.
        // tryAllocPixels() can fail due to OOM on the Java heap, OOM on the
        // native heap, or the recycled javaBitmap being too small to reuse.
        return nullptr;
    }
    ...
}

// SkBitmap.cpp https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213
bool SkBitmap::tryAllocPixels(Allocator* allocator) {
    HeapAllocator stdalloc;

    if (nullptr == allocator) {
        allocator = &stdalloc;
    }
    return allocator->allocPixelRef(this);
}
```

+ `BitmapFactory.doDecode()` 调用 `SkBitmap.tryAllocPixels()` 为 Bitmap 分配内存
+ `SkBitmap.tryAllocPixels()` 调用 `Allocator.allocPixelRef()` 为 Bitmap 分配内存

复用 Bitmap 时分配内存的情况相对简单，这里略过。主要看不复用 Bitmap 的场景。

以一个不复用 Bitmap 但需要缩放的 Bitmap 为例，对应的 `Allocator` 是 `SkBitmap::HeapAllocator`。那么实际的 `allocPixelRef()` 流程是这样的：

+ [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
+ [SkBitmap::HeapAllocator::allocPixelRef](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L368)
+ [SkMallocPixelRef::MakeAllocate](https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L57)
+ [sk_calloc_canfail](https://github.com/google/skia/blob/master/include/private/SkMalloc.h#L66)
+ [sk_malloc_flags](https://github.com/google/skia/blob/master/src/ports/SkMemory_malloc.cpp#L66)

整个流程最终会调用 `malloc()` 分配指定大小的内存。部分关键代码如下：

```cpp
// SkMallocPixelRef.cpp
sk_sp<SkPixelRef> SkMallocPixelRef::MakeAllocate(const SkImageInfo& info, size_t rowBytes) {
    ...
    void* addr = sk_calloc_canfail(size);

	...
    
}

// SkMalloc.h
static inline void* sk_calloc_canfail(size_t size) {
#if defined(IS_FUZZING_WITH_LIBFUZZER)
    // The Libfuzzer environment is very susceptible to OOM, so to avoid those
    // just pretend we can't allocate more than 200kb.
    if (size > 200000) {
        return nullptr;
    }
#endif
    return sk_malloc_flags(size, SK_MALLOC_ZERO_INITIALIZE);
}

// SkMemory_malloc.cpp
void* sk_malloc_flags(size_t size, unsigned flags) {
    void* p;
    if (flags & SK_MALLOC_ZERO_INITIALIZE) {
        p = calloc(size, 1);
    } else {
        p = malloc(size);
    }
    if (flags & SK_MALLOC_THROW) {
        return throw_on_failure(size, p);
    } else {
        return p;
    }
}
```

以一个不复用 Bitmap 也不需要缩放的 Bitmap 为例，对应的 `Allocator` 是缺省的 `HeapAllocator`。那么实际的 `allocPixelRef()` 流程是这样的：

+ [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
+ [HeapAllocator::allocPixelRef](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Graphics.cpp#616)
+ [android::Bitmap::allocateHeapBitmap](https://android.googlesource.com/platform/frameworks/base/+/master/libs/hwui/hwui/Bitmap.cpp#79)

整个流程最终也会分配指定大小的内存。部分关键代码如下：

```cpp
// Bitmap.cpp https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/libs/hwui/hwui/Bitmap.cpp#86
static sk_sp<Bitmap> allocateHeapBitmap(size_t size, const SkImageInfo& info, size_t rowBytes) {
    void* addr = calloc(size, 1);
    if (!addr) {
        return nullptr;
    }
    return sk_sp<Bitmap>(new Bitmap(addr, size, info, rowBytes));
}

// Graphics.cpp https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Graphics.cpp#616
bool HeapAllocator::allocPixelRef(SkBitmap* bitmap, SkColorTable* ctable) {
    mStorage = android::Bitmap::allocateHeapBitmap(bitmap, ctable);
    return !!mStorage;
}

// GraphicsJNI.h https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/GraphicsJNI.h#125
class HeapAllocator : public SkBRDAllocator {
public:
   HeapAllocator() { };
    ~HeapAllocator() { };
    virtual bool allocPixelRef(SkBitmap* bitmap, SkColorTable* ctable) override;
    /**
     * Fetches the backing allocation object. Must be called!
     */
    android::Bitmap* getStorageObjAndReset() {
        return mStorage.release();
    };
    SkCodec::ZeroInitialized zeroInit() const override { return SkCodec::kYes_ZeroInitialized; }
private:
    sk_sp<android::Bitmap> mStorage;
};
```

无论如何，分配出来的内存块最终都由 `SkBitmap` 来持有(实际上是由 [SkBitmap 的 SkPixmap](https://github.com/google/skia/blob/master/src/core/SkPixmap.cpp)，我们忽略这里的细节)。注意这里的 `dst->setPixelRef(std::move(pr), 0, 0);`

```cpp
// SkBitmap.cpp https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L368
bool SkBitmap::tryAllocPixels(Allocator* allocator) {
    HeapAllocator stdalloc;

    if (nullptr == allocator) {
        allocator = &stdalloc;
    }
    return allocator->allocPixelRef(this);
}


/** We explicitly use the same allocator for our pixels that SkMask does,
 so that we can freely assign memory allocated by one class to the other.
 */
bool SkBitmap::HeapAllocator::allocPixelRef(SkBitmap* dst) {
    const SkImageInfo info = dst->info();
    if (kUnknown_SkColorType == info.colorType()) {
//        SkDebugf("unsupported config for info %d\n", dst->config());
        return false;
    }

    sk_sp<SkPixelRef> pr = SkMallocPixelRef::MakeAllocate(info, dst->rowBytes());
    if (!pr) {
        return false;
    }

    dst->setPixelRef(std::move(pr), 0, 0);
    SkDEBUGCODE(dst->validate();)
    return true;
}

void SkBitmap::setPixelRef(sk_sp<SkPixelRef> pr, int dx, int dy) {
    ...
    fPixelRef = kUnknown_SkColorType != this->colorType() ? std::move(pr) : nullptr;
    void* p = nullptr;
    size_t rowBytes = this->rowBytes();
    // ignore dx,dy if there is no pixelref
    if (fPixelRef) {
        rowBytes = fPixelRef->rowBytes();
        // TODO(reed):  Enforce that PixelRefs must have non-null pixels.
        p = fPixelRef->pixels();
        if (p) {
            p = (char*)p + dy * rowBytes + dx * this->bytesPerPixel();
        }
    }
    SkPixmapPriv::ResetPixmapKeepInfo(&fPixmap, p, rowBytes);
    ...
}
```

注：native 层的 Bitmap 类似乎是在 [hwui库](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/libs/hwui/hwui/Bitmap.h#45)中定义的

### 图片解码
在 Skia 中 `SkCodec` 代表解码器，解码器的类层次结构如下：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-sk-codec-class.png)

Skia 将实际的解码工作交由第三方库。不同图片格式有各自对应的解码器。比如 PNG 图片由 `SkPngCodec` 解码。而 `SkPngCodec` 实际上是对 libpng 的封装。

前面提到过 `BitmapFactory.doDecode()` 的第2步是创建解码器，而第10步是调用该解码器进行解码。具体如下：

+ [SkCodec::MakeFromStream()](https://github.com/google/skia/blob/master/src/codec/SkCodec.cpp#L70) 根据图片格式选择一个合适的 `SkCodec`，比如为 PNG 图片选择 `SkPngCodec`
+ [SkAndroidCodec::MakeFromStream()](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L78) 创建 `SkAndroidCodec`， 它是上一步创建的 `SkCodec` 的代理。`SkAndroidCodec` 的具体类型跟图片格式相关
  + PNG，JPEG，GIF，BMP 等格式时创建 `SkSampledCodec`
  + WEBP 格式时创建 `SkAndroidCodecAdapter`
+ 调用 [SkAndroidCodec.getAndroidPixels()](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L357) 解码

对于这段代码，不妨以 PNG 图片为例来看一下接下来的过程。

```cpp
static jobject doDecode() {
    ...
    SkCodec::Result result = codec->getAndroidPixels(decodeInfo, decodingBitmap.getPixels(),
            decodingBitmap.rowBytes(), &codecOptions);
                
    ...
}
```

对于 PNG 图片，这里的 `codec` 是 `SkSampledCodec`。而 `SkSampledCodec` 实际使用的解码器是 `SkPngCodec` (见 `SkAndroidCodec.fCodec` 字段)

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-decode.png)

最终会调用到 `SkPngCodec.onGetPixels()` 方法。该方法使用 libpng 库解码 PNG 图片。

```cpp
SkCodec::Result SkPngCodec::onGetPixels(const SkImageInfo& dstInfo, void* dst,
                                        size_t rowBytes, const Options& options,
                                        int* rowsDecoded) {
    Result result = this->initializeXforms(dstInfo, options);
    if (kSuccess != result) {
        return result;
    }

    if (options.fSubset) {
        return kUnimplemented;
    }

    this->allocateStorage(dstInfo);
    this->initializeXformParams();
    return this->decodeAllRows(dst, rowBytes, rowsDecoded);
}
```

解码结果保存在 `void* dst` 指针指向的内存块。而这块内存，也正是上一步中分配的内存。注意这里的 `void* dst` 参数来自 `decodingBitmap.getPixels()`。

## 创建Java对象
解码完成后得到 Native 层的 `SkBitmap` 对象，最后一步工作是将其封装成 Java 层可以使用的 `Bitmap` 对象。这一步的工作相对简单。

+ `BitmapFactory.doDecode()` 调用 `Bitmap.createBitmap()`
+ `Bitmap.createBitmap()` 调用 Java Bitmap 的构造方法
+ Java Bitmap 的构造方法中保存 `mNativePtr` (像素数据内存块的地址)

```cpp
// BitmapFactory.cpp
static jobject doDecode() {
    SkBitmap decodingBitmap;    
    ...
    SkCodec::Result result = ...
    SkBitmap outputBitmap;
    outputBitmap.swap(decodingBitmap);                
    ...
    // now create the java bitmap
    return bitmap::createBitmap(env, defaultAllocator.getStorageObjAndReset(),
            bitmapCreateFlags, ninePatchChunk, ninePatchInsets, -1);    
}

// Bitmap.cpp
jobject createBitmap(JNIEnv* env, Bitmap* bitmap,
        int bitmapCreateFlags, jbyteArray ninePatchChunk, jobject ninePatchInsets,
        int density) {
    bool isMutable = bitmapCreateFlags & kBitmapCreateFlag_Mutable;
    bool isPremultiplied = bitmapCreateFlags & kBitmapCreateFlag_Premultiplied;
    // The caller needs to have already set the alpha type properly, so the
    // native SkBitmap stays in sync with the Java Bitmap.
    assert_premultiplied(bitmap->info(), isPremultiplied);
    BitmapWrapper* bitmapWrapper = new BitmapWrapper(bitmap);
    jobject obj = env->NewObject(gBitmap_class, gBitmap_constructorMethodID,
            reinterpret_cast<jlong>(bitmapWrapper), bitmap->width(), bitmap->height(), density,
            isMutable, isPremultiplied, ninePatchChunk, ninePatchInsets);
    if (env->ExceptionCheck() != 0) {
        ALOGE("*** Uncaught exception returned from Java call!\n");
        env->ExceptionDescribe();
    }
    return obj;
}
```

```java
// Bitmap.java
public final class Bitmap implements Parcelable {
    // Convenience for JNI access
    private final long mNativePtr;    
    /**
     * Private constructor that must received an already allocated native bitmap
     * int (pointer).
     */
    // called from JNI
    Bitmap(long nativeBitmap, int width, int height, int density,
            boolean isMutable, boolean requestPremultiplied,
            byte[] ninePatchChunk, NinePatch.InsetStruct ninePatchInsets) {
        if (nativeBitmap == 0) {
            throw new RuntimeException("internal error: native bitmap is 0");
        }
        ...
        mNativePtr = nativeBitmap;
        ...        
    }
}
```

Java 层的 `Bitmap` 对象在内存中大致是这样：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-ref-relationship.png)

# 回收
上一节讲的是 Bitmap 的生，这一节谈谈 Bitmap 的死。跟多数普通 Java 对象不一样，Bitmap 的像素数据在 native heap 中。而我们知道 native heap 并不被 JVM 管理，那如何保证 Bitmap 实例本身被 GC 后 native heap 中的内存不会泄漏呢？

## recycle
首先想到的是代码主动调用 [Bitmap.recycle()](https://developer.android.com/reference/android/graphics/Bitmap.html) 来释放 native 内存。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-free-mem.png)

上图给出了主要流程。来详细看一下。

```java
    /**
     * Free the native object associated with this bitmap, and clear the
     * reference to the pixel data. This will not free the pixel data synchronously;
     * it simply allows it to be garbage collected if there are no other references.
     * The bitmap is marked as "dead", meaning it will throw an exception if
     * getPixels() or setPixels() is called, and will draw nothing. This operation
     * cannot be reversed, so it should only be called if you are sure there are no
     * further uses for the bitmap. This is an advanced call, and normally need
     * not be called, since the normal GC process will free up this memory when
     * there are no more references to this bitmap.
     */
    public void recycle() {
        if (!mRecycled && mNativePtr != 0) {
            if (nativeRecycle(mNativePtr)) {
                // return value indicates whether native pixel object was actually recycled.
                // false indicates that it is still in use at the native level and these
                // objects should not be collected now. They will be collected later when the
                // Bitmap itself is collected.
                mNinePatchChunk = null;
            }
            mRecycled = true;
        }
    }
```

Java 层的 `recycle()` 调用 Native 层的 `Bitmap_recycle()`，最终调用到 `SkBitmap.reset()`。

```cpp
// Bitmap.cpp https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Bitmap.cpp#872
static jboolean Bitmap_recycle(JNIEnv* env, jobject, jlong bitmapHandle) {
    LocalScopedBitmap bitmap(bitmapHandle);
    bitmap->freePixels();
    return JNI_TRUE;
}

// Convenience class that does not take a global ref on the pixels, relying
// on the caller already having a local JNI ref
class LocalScopedBitmap {
public:
    explicit LocalScopedBitmap(jlong bitmapHandle)
            : mBitmapWrapper(reinterpret_cast<BitmapWrapper*>(bitmapHandle)) {}
    BitmapWrapper* operator->() {
        return mBitmapWrapper;
    }
    void* pixels() {
        return mBitmapWrapper->bitmap().pixels();
    }
    bool valid() {
        return mBitmapWrapper && mBitmapWrapper->valid();
    }
private:
    BitmapWrapper* mBitmapWrapper;
};

class BitmapWrapper {
public:
    BitmapWrapper(Bitmap* bitmap)
        : mBitmap(bitmap) { }
    void freePixels() {
        mInfo = mBitmap->info();
        mHasHardwareMipMap = mBitmap->hasHardwareMipMap();
        mAllocationSize = mBitmap->getAllocationByteCount();
        mRowBytes = mBitmap->rowBytes();
        mGenerationId = mBitmap->getGenerationID();
        mIsHardware = mBitmap->isHardware();
        mBitmap.reset();
    }
}
```

最终，[SkBitmap.reset()](https://github.com/google/skia/blob/master/include/core/SkBitmap.h#L334) 会将 `fPixelRef` 置为 `nullptr`。如果 `fPixelRef` 原来指向的那个 `SkPixelRef`。

```cpp
    /** Resets to its initial state; all fields are set to zero, as if SkBitmap had
        been initialized by SkBitmap().
        Sets width, height, row bytes to zero; pixel address to nullptr; SkColorType to
        kUnknown_SkColorType; and SkAlphaType to kUnknown_SkAlphaType.
        If SkPixelRef is allocated, its reference count is decreased by one, releasing
        its memory if SkBitmap is the sole owner.
    */
    void reset() {
        fPixelRef = nullptr;  // Free pixels.
        fPixmap.reset();
        fFlags = 0;        
    }
```

 `fPixelRef` 之前是在如何分配内存过程中被赋值的，代码如下：

```cpp
bool SkBitmap::HeapAllocator::allocPixelRef(SkBitmap* dst) {
    ...

    sk_sp<SkPixelRef> pr = SkMallocPixelRef::MakeAllocate(info, dst->rowBytes());
    if (!pr) {
        return false;
    }

    dst->setPixelRef(std::move(pr), 0, 0);
    ...
}

void SkBitmap::setPixelRef(sk_sp<SkPixelRef> pr, int dx, int dy) {
    fPixelRef = kUnknown_SkColorType != this->colorType() ? std::move(pr) : nullptr;
    ...
}
```

如果 `fPixelRef` 原来指向的那个 `SkMallocPixelRef` 对象引用数为0时，其析构方法被调用，最终触发 [sk_free_releaseproc()](https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L33) 方法回收内存。

注：分配内存过程中，`sk_free_releaseproc` 作为 `SkMallocPixelRef()` 构造方法的 `SkMallocPixelRef::ReleaseProc` 参数

```cpp
// SkMemory_malloc.cpp https://github.com/google/skia/blob/master/src/ports/SkMemory_malloc.cpp#L66
void sk_free(void* p) {
    if (p) {
        free(p);
    }
}

// SkMallocPixelRef.cpp https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L33
// assumes ptr was allocated via sk_malloc
static void sk_free_releaseproc(void* ptr, void*) {
    sk_free(ptr);
}

// SkMallocPixelRef.cpp https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L57
sk_sp<SkPixelRef> SkMallocPixelRef::MakeAllocate(const SkImageInfo& info, size_t rowBytes) {
    ... 
    void* addr = sk_calloc_canfail(size);
    if (nullptr == addr) {
        return nullptr;
    }

    return sk_sp<SkPixelRef>(new SkMallocPixelRef(info, addr, rowBytes,
                                                  sk_free_releaseproc, nullptr));
}

// SkMallocPixelRef.cpp https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L133
SkMallocPixelRef::~SkMallocPixelRef() {
    if (fReleaseProc != nullptr) {
        fReleaseProc(this->pixels(), fReleaseProcContext);
    }
}
```

## 自动释放
官方文档中 `recycle()` 使用说明比较含糊不清，[文档](https://developer.android.com/topic/performance/graphics/manage-memory#recycle)建议在 Android 2.3.3 及以下版本中调用 `recycle()` 方法，没有说其他版本中该怎么做才是正确的。

> On Android 2.3.3 (API level 10) and lower, using recycle() is recommended. If you're displaying large amounts of bitmap data in your app, you're likely to run into OutOfMemoryError errors. The recycle() method allows an app to reclaim memory as soon as possible.

但实际上现在的 Android 应用中多数场景不必代码主动调用 `recycle()` 方法来释放 native 内存，并不会碰到文档中说的 "native memory is not released in a predictable manner" 问题。这是为何？秘密在于 [NativeAllocationRegistry](https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/util/NativeAllocationRegistry.java)

> NativeAllocationRegistry 用于将 native 内存跟 Java 对象关联，并将它们注册到 Java 运行时。注册 Java 对象关联的 native 内存有几个好处：
>
> + Java 运行时在 GC 调度时可考虑 native 内存状态
> + Java 运行时在 Java 对象变得不可达时可以使用用户提供的函数来自动清理 native 内存

来看代码。

```java
    Bitmap(long nativeBitmap, int width, int height, int density,
            boolean isMutable, boolean requestPremultiplied,
            byte[] ninePatchChunk, NinePatch.InsetStruct ninePatchInsets) {
        ...
        NativeAllocationRegistry registry = new NativeAllocationRegistry(
            Bitmap.class.getClassLoader(), nativeGetNativeFinalizer(), nativeSize);
        registry.registerNativeAllocation(this, nativeBitmap);
        ...           
    }             
``` 

注意到 Bitmap 构造方法有如下操作：

+ 向 `NativeAllocationRegistry` 提供 `nativeGetNativeFinalizer()` 方法地址
+ 将当前 Java 对象本身注册到 `NativeAllocationRegistry` ( Java 本身用于引用可达性检查，具体细节本文忽略)
+ 将当前 Java 对象关联的 native 内存地址注册到 `NativeAllocationRegistry`

当 Java 层 Bitmap 对象不可达后关联的 native 内存会由 `nativeGetNativeFinalizer()` 指定的方法来回收。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-free-native-mem.png)

Bitmap 指定由 [Bitmap_destruct()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Bitmap.cpp#864) 方法来回收 native 内存。

```cpp
static void Bitmap_destruct(BitmapWrapper* bitmap) {
    delete bitmap;
}

static jlong Bitmap_getNativeFinalizer(JNIEnv*, jobject) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&Bitmap_destruct));
}
```

[Cleaner](https://android.googlesource.com/platform/libcore/+/49965c1/ojluni/src/main/java/sun/misc/Cleaner.java)

# 总结

TODO 图

# 参考
+ [如何管理 Bitmap 内存](https://developer.android.com/topic/performance/graphics/manage-memory.html)
+ [SkBitmap Reference](https://skia.org/user/api/SkBitmap_Reference)