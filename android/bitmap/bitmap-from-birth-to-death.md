

Bitmap 是内存大户，所以了解其内存是如何被分配和销毁是必要的。本文试图理清创建和销毁 Bitmap 过程的主脉络，忽略一些细节，重点聚焦内存分配与回收。如有疏漏，欢迎批评指正。

<!--more-->

Bitmap 占内存多是因为其像素数据(pixels)大。Bitmap 像素数据的存储在不同 Android 版本之间有所不同，具体来说：

+ Android 2.3 (API Level 10) 以及之前 - 像素数据保存在 native heap
+ Android 3.0 到 Android 7.1 (API Level 11-26) - 像素数据保存在 java heap
+ Android 8.0 以及之后 - 像素数据保存在 native heap

这种变化导致不同 Android 版本间 Bitmap 相关的代码可能有较大差异。本文基于 [Android 8.0 源码](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Bitmap.cpp)分析。

# 总览 
先从整体上看一下 Bitmap。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-arch.png)

+ 用户在手机屏幕上看到的是一张张图片
+ App 中这些图片实际上是 BitmapDrawable
+ BitmapDrawable 是对 Bitmap 的包装
+ Bitmap 是对 SkBitmap 的包装。具体说来， Bitmap 的具体实现包括 Java 层和 JNI 层，JNI 层依赖 [Skia](https://github.com/google/skia)。
+ SkBitmap 本质上可简单理解为内存中的一个字节数组

所以说 Bitmap 其实是一个字节数组。创建 Bitmap 是在内存中分配一个字节数组，销毁 Bitmap 则是回收这个字节数组。

# 创建
创建 Bitmap 的方式很多，

+ 可以通过 SDK 提供的 API 来创建 Bitmap
+ 加载某些布局或资源时会创建 Bitmap
+ [Glide](https://github.com/bumptech/glide) 等第三方图片库会创建 Bitmap

先说通过 API 创建 Bitmap。SDK 中创建 Bitmap 的 API 很多，分成三大类：

+  **创建** Bitmap - `Bitmap.createBitmap()` 方法在内存中从无到有地创建 Bitmap
+  **拷贝** Bitmap - `Bitmap.copy()` 从已有的 Bitmap 拷贝出一个新的 Bitmap
+  **解码** - 从文件或字节数组等资源解码得到 Bitmap，这是最常见的创建方式
  + BitmapFactory.decodeResource()
  + [ImageDecoder.decodeBitmap](https://developer.android.com/reference/android/graphics/ImageDecoder)。ImageDecoder 是 Android 9.0 新加的类

假如 `resId` 对应的是一张图片。加载如下布局文件时会创建一个 Bitmap：

```xml
<ImageView android:src="@drawable/resId">
```

代码中加载资源也会创建出一个 Bitmap：

```java
Drawable drawable = Resources.getDrawable(resId)
```

实际项目中往往不是直接调用 API 来创建 Bitmap，而是使用 [Glide](https://github.com/bumptech/glide) 或 [Picosso](https://github.com/square/picasso) 等第三方图片库。这些库成熟稳定，接口易用，开发中处理 Bitmap 变得轻松。

```java
Glide.with(this).load("http://goo.gl/gEgYUd").into(imageView);
```

以 Glide 为例，只要一行代码可以很方便地从网络上加载一张图片。但另一方面，Glide 也让 Bitmap 的创建更加多样和复杂。

太多的创建 Bitmap 的方式，简直让人头大。但好在无论哪种创建方式，最终殊途同归。见下图：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-overview.png)

Java 层的创建 Bitmap 的**所有 API** 进入到 Native 层后，全都会走这四个步骤：

+ 资源转换 - 这一步将 Java 层传来的不同类型的资源转换成解码器可识别的数据类型
+ 内存分配 - 这一步是分配内存，分配时会是否复用 Bitmap 等因素
+ 图片解码 - 实际的解码工作由第三方库，解码结果填在上一步分配的内存中。注，`Bitmap.createBitmap()` 和 `Bitmap.copy()` 创建的 Bitmap 不需要进行图片解码
+ 创建对象 - 这一步创建 Java 对象，将包含解码数据的内存块包装成 Java 层的 `android.graphics.Bitmap` 对象

内存分配和图片解码由 [BitmapFactory.doDecode()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#233) 函数是是创建 Bitmap 的核心，它负责内存分配和图片解码，其关键步骤包括：

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

我们先简单说说资源转换，稍后详细讨论内存分配和图片解码(包括第7, 9, 10步)。

## 资源转换
解码前的第一项工作是资源转换。Java 层的待解码资源包括：

+ File
+ Resource
+ ByteArray
+ Stream
+ FileDescriptor

第一步，在 JNI 层将待解码的资源重新划分成四种，包括：

+ FileDescriptor
+ Asset
+ ByteArray
+ DecodeAsset

第二步，`BitmapFactory` 提供四个方法对资源进行转换，所有的资源都会转换成与 `SkStreamRewindable` 兼容的数据

```cpp
nativeDecodeFileDescriptor()
nativeDecodeAsset()
nativeDecodeByteArray()
nativeDecodeAsset()
```

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-convert-resource.png)

最后，[BitmapFactory.doDecode()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#233) 统一解码处理 `SkStreamRewindable`。 

### 内存分配
解码前的第二项工作是内存分配。

首先是选择 decodeAllocator (见上文提到的 `BitmapFactory.doDecode()` 的第7步)。有以下几种 Allocator 可供选择：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-createion-allocator-classes.png)

选择 Allocator 时考虑的因素包括：是否复用已有 Bitmap，是否会缩放 Bitmap，是否是 Hardware Bitmap。选择策略总结如下：

|是否复用已有 Bitmap|是否会缩放 Bitmap|是否是 Hardware Bitmap|Allocator类型|
|--------------|--------------|-------------|------------|
|是            |是            |-            |[ScaleCheckingAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#144)|
|是            |否            |-            |[RecyclingPixelAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/GraphicsJNI.h#171)|
|否            |是            |是           |[SkBitmap::HeapAllocator](https://github.com/google/skia/blob/master/include/core/SkBitmap.h#L1119)|
|-             |-            |-            |[HeapAllocator](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/GraphicsJNI.h#125) (缺省的Allocator)|

接下来，使用选定的 Allocator 分配内存。

```
BitmapFactory.doDecode() ->
	SkBitmap.tryAllocPixels() ->
    		Allocator.allocPixelRef()
```

代码如下：

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

Allocator 的类型有四种，我们只看其中的两种。

先看 `SkBitmap::HeapAllocator` 作为 Allocator 进行内存分配的流程。

1. [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
2. [SkBitmap::HeapAllocator::allocPixelRef](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L368)
3. [SkMallocPixelRef::MakeAllocate](https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L57)
4. [sk_calloc_canfail](https://github.com/google/skia/blob/master/include/private/SkMalloc.h#L66)
5. [sk_malloc_flags](https://github.com/google/skia/blob/master/src/ports/SkMemory_malloc.cpp#L66)

对应的代码如下(可以看到最终会调用 `malloc()` 分配指定大小的内存)：

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

再来看 `HeapAllocator` 作为 Allocator 进行内存分配的流程。

1. [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
2. [HeapAllocator::allocPixelRef](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Graphics.cpp#616)
3. [android::Bitmap::allocateHeapBitmap](https://android.googlesource.com/platform/frameworks/base/+/master/libs/hwui/hwui/Bitmap.cpp#79)

对应的代码如下：

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

对于 `RecyclingPixelAllocator` 和 `ScaleCheckingAllocator` 的情况，读者可以自行分析。

无论哪种 Allocator，最终要么调用 `malloc()` 分配内存，要么复用之前分配的内存。分配/复用完成后，由 `SkBitmap` 来持有。

注：

+ 准确来说，`SkBitmap::HeapAllocator` 分配内存由 [SkBitmap 的 SkPixmap](https://github.com/google/skia/blob/master/src/core/SkPixmap.cpp) 持有，而不是 SkBitmap 持有。忽略这个细节
+ 准确来说，`HeapAllocator` 分配的内存是由 `android::Bitmap.mStorage` 持有，而不是 SkBitmap 持有。但 `android::Bitmap` 与 SkBitmap 有某种关联，所以可以忽略这个细节

<!--
注：native 层的 Bitmap 类比较让人疑惑，一直找不到其具体代码，似乎是在 [hwui库](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/libs/hwui/hwui/Bitmap.h#45)中定义的
-->

### 图片解码
在 Skia 中 `SkCodec` 代表解码器，解码器的类层次结构如下：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-sk-codec-class.png)

Skia 将实际的解码工作交由第三方库，不同图片格式有各自对应的解码器。比如 PNG 图片由 `SkPngCodec` 解码，而 `SkPngCodec` 实际上是对 libpng 的封装。

前面提到 `BitmapFactory.doDecode()` 的第2步是创建解码器，第10步是调用该解码器进行解码。

+ [SkCodec::MakeFromStream()](https://github.com/google/skia/blob/master/src/codec/SkCodec.cpp#L70) 根据图片格式选择一个合适的 `SkCodec`，比如为 PNG 图片选择 `SkPngCodec`
+ [SkAndroidCodec::MakeFromStream()](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L78) 创建 `SkAndroidCodec`， 它是上一步创建的 `SkCodec` 的代理。`SkAndroidCodec` 的具体类型跟图片格式有关。PNG，JPEG，GIF，BMP 等格式时其类型是 `SkSampledCodec`，WEBP 格式时是 `SkAndroidCodecAdapter`
+ 调用 [SkAndroidCodec.getAndroidPixels()](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L357) 解码

代码如下：

```cpp
static jobject doDecode() {
    ...
    SkCodec::Result result = codec->getAndroidPixels(decodeInfo, decodingBitmap.getPixels(),
            decodingBitmap.rowBytes(), &codecOptions);
                
    ...
}
```

以 PNG 图片为例来分析。 

首先，对于 PNG 图片 `codec` 是 `SkSampledCodec`， `SkSampledCodec` 使用的解码器是 `SkPngCodec` (见 `SkAndroidCodec.fCodec` 字段)。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-decode.png)

第一步是调用 `codec->getAndroidPixels()` 方法。注意第二个参数正是上一步分配的内存地址。

接下来是一系列函数调用。注意传入的内存地址参数 `dst` 即可，其他细节忽略。

然后会执行到 `SkPngCodec.onGetPixels()` 方法。它使用 [libpng 库](http://www.libpng.org/pub/png/libpng.html)解码 PNG 图片。

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

最终，解码结果保存在 `dst` 指针指向的内存。

## 创建Java对象
解码完成后得到 Native 层的 `SkBitmap` 对象，最后一步工作是将其封装成 Java 层可以使用的 `Bitmap` 对象。

这一步的过程相对简单，分为三步：

```
BitmapFactory.doDecode() ->
	Bitmap.createBitmap() ->
    		Java Bitmap 的构造方法中保存 `mNativePtr` (像素数据内存块的地址)
```

对应的代码如下：

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

至此，Java 层的 `Bitmap` 对象创建完毕，它在内存中大致是这样的：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-ref-relationship.png)

# 销毁
上一节重点是讲如何为 Bitmap 分配内存，这一节重点讲如何在 Bitmap 销毁时回收内存。

Java 层的 Bitmap 对象有点特别，特别之处在于其像素数据保存在 native heap。我们知道， native heap 并不被 JVM 管理，那如何保证 Bitmap 对象本身被 GC 后 native heap 中的内存也能正确回收呢？

## recycle
首先想到的是在代码主动调用 [Bitmap.recycle()](https://developer.android.com/reference/android/graphics/Bitmap.html) 方法来释放 native 内存。

流程如下：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-free-mem.png)

来看具体代码。

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

+ 首先，App 主动调用 `Bitmap.recycle()` 方法
+ 接下来，`Bitmap.recycle()` 调用对应的 native 方法 `Bitmap_recycle()`
+ 然后会进入到 `BitmapWrapper.freePixels()` 方法
+ 最后，[SkBitmap.reset()](https://github.com/google/skia/blob/master/include/core/SkBitmap.h#L334) 将 `fPixelRef` 置空。注：之前分配内存过程中可以看到 `fPixelRef` 是如何被赋值的

`fPixelRef` 原本指向一个 `SkMallocPixelRef` 对象。将 `fPixelRef` 置空后，该对象引用数变成0时，`~SkMallocPixelRef()` 析构方法被调用，并触发 [sk_free_releaseproc()](https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L33) 方法执行，回收内存。

注意这里的 `sk_free_releaseproc`。 它是方法地址，在之前的分配内存过程中作为 `SkMallocPixelRef()` 构造方法的 `SkMallocPixelRef::ReleaseProc` 参数被传进来的。`sk_free_releaseproc` 指向的方法负责最终的内存回收。

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
实际上现在的 Android 应用中多数场景代码不主动调用 `recycle()`， native 内存也能正确回收。这是为何？秘密在于 [NativeAllocationRegistry](https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/util/NativeAllocationRegistry.java)。

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
+ 将当前 Java 对象本身注册到 `NativeAllocationRegistry` ( Java 对象用于引用可达性检查 )
+ 将当前 Java 对象关联的 native 内存地址注册到 `NativeAllocationRegistry`

当 Java 层 Bitmap 对象不可达后关联的 native 内存会由 `nativeGetNativeFinalizer()` 指定的方法来回收，流程如下：

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-free-native-mem.png)

来看 `NativeAllocationRegistry` 的代码：

```java
public class NativeAllocationRegistry {

    public Runnable registerNativeAllocation(Object referent, long nativePtr) {
        CleanerThunk thunk;
        CleanerRunner result;
        try {
            thunk = new CleanerThunk();
            Cleaner cleaner = Cleaner.create(referent, thunk);
            result = new CleanerRunner(cleaner);
            registerNativeAllocation(this.size);
        } catch (VirtualMachineError vme /* probably OutOfMemoryError */) {
            applyFreeFunction(freeFunction, nativePtr);
            throw vme;
        } // Other exceptions are impossible.
        // Enable the cleaner only after we can no longer throw anything, including OOME.
        thunk.setNativePtr(nativePtr);
        ...
    }
    
    private class CleanerThunk implements Runnable {
        private long nativePtr;
        public CleanerThunk() {
            this.nativePtr = 0;
        }
        public void run() {
            if (nativePtr != 0) {
                applyFreeFunction(freeFunction, nativePtr);
                registerNativeFree(size);
            }
        }
        public void setNativePtr(long nativePtr) {
            this.nativePtr = nativePtr;
        }
    }
    private static class CleanerRunner implements Runnable {
        private final Cleaner cleaner;
        public CleanerRunner(Cleaner cleaner) {
            this.cleaner = cleaner;
        }
        public void run() {
            cleaner.clean();
        }
    }
    

    /**
     * Calls <code>freeFunction</code>(<code>nativePtr</code>).
     * Provided as a convenience in the case where you wish to manually free a
     * native allocation using a <code>freeFunction</code> without using a
     * NativeAllocationRegistry.
     */
    @libcore.api.CorePlatformApi
    public static native void applyFreeFunction(long freeFunction, long nativePtr);  
}          
```

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-free-native-mem2.png)

对 Bitmap 而言，[Bitmap_destruct()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Bitmap.cpp#864) 方法被指定用来回收 native 内存。这个方法超级简单，相信你一眼能看明白。

```cpp
static void Bitmap_destruct(BitmapWrapper* bitmap) {
    delete bitmap;
}

static jlong Bitmap_getNativeFinalizer(JNIEnv*, jobject) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&Bitmap_destruct));
}
```

如果想了解更多细节，可以看 [Cleaner](https://android.googlesource.com/platform/libcore/+/49965c1/ojluni/src/main/java/sun/misc/Cleaner.java) 源码。

# 总结
通过一步步分析，最终不难发现 Bitmap 本质上就是内存中的一块数据。所谓创建 Bitmap，不过是调用 `malloc()` 分配一块内存。而回收 Bitmap，不过是调用 `free()` 将之前的内存释放掉。

# 参考
+ [如何管理 Bitmap 内存](https://developer.android.com/topic/performance/graphics/manage-memory.html)
+ [SkBitmap Reference](https://skia.org/user/api/SkBitmap_Reference)
