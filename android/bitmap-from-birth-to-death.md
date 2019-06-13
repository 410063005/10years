
http://km.oa.com/articles/show/408363?kmref=home_headline

# 生

+ Bitmap.createBitmap()
+ Bitmap.copy()
+ BitmapFactory.decodeResource()
+ 加载布局文件 BitmapDrawable
+ 加载 drawable 资源文件 Resources.getDrawable()

流程图

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-overview.png)

## 图片解码
[BitmapFactory.doDecode()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#233)

```cpp
BitmapFactory.nativeDecodeFileDescriptor()
BitmapFactory.nativeDecodeAsset()
BitmapFactory.nativeDecodeByteArray()
BitmapFactory.nativeDecodeAsset()
```

待解码的资源种类一共有四种：

+ FileDescriptor
+ Asset
+ ByteArray
+ DecodeAsset

上面的四个方法先进行资源转换成与 `SkStreamRewindable` 兼容的类型，然后统一交由 `deDecode()` 解码。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-convert-resource.png)

`deDecode()` 流程可以总结为：

+ Update with options supplied by the client.
+ Create the codec.
+ Handle sampleSize. (BitmapFactory.Options.inSampleSize)
+ Set the decode colorType.
+ Handle scale.  (BitmapFactory.Options.inScaled)
+ Handle reuseBitmap (BitmapFactory.Options.inBitmap)
+ Choose decodeAllocator
+ Construct a color table
+ AllocPixels 
+ Use SkAndroidCodec to perform the decode.
+ Create the java bitmap

### 分配内存

如何 Choose decodeAllocator。考虑因素包括：

+ 是否复用 Bitmap - inBitmap
+ 是否缩放 Bitmap - inScaled
+ 是否GPU中的 Bitmap - isHardware

SkBitmap::Allocator* decodeAllocator。可选的 Allocator 包括：

+ ScaleCheckingAllocator - 复用 Bitmap 且需要缩放
+ RecyclingPixelAllocator - 复用 Bitmap 但不需要缩放
+ SkBitmap::HeapAllocator - 不复用 Bitmap，但需要缩放或者是GPU Bitmap
+ HeapAllocator - 如果不满足上述几种情况，就使用缺省的 Allocator

decodeAllocator 和 color table 准备好了之后，调用 `SkBitmap.tryAllocPixels()` 为 Bitmap 分配内存。[源码链接](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/BitmapFactory.cpp#444)：

```cpp
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
```

[SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213) 源码如下：

```cpp
bool SkBitmap::tryAllocPixels(Allocator* allocator) {
    HeapAllocator stdalloc;

    if (nullptr == allocator) {
        allocator = &stdalloc;
    }
    return allocator->allocPixelRef(this);
}
```

以 `SkBitmap::HeapAllocator` 为例，`allocator->allocPixelRef()` 流程如下：

+ [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
+ [SkBitmap::HeapAllocator::allocPixelRef](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L368)
+ [SkMallocPixelRef::MakeAllocate](https://github.com/google/skia/blob/master/src/core/SkMallocPixelRef.cpp#L57)
+ [sk_calloc_canfail](https://github.com/google/skia/blob/master/include/private/SkMalloc.h#L66)
+ [sk_malloc_flags](https://github.com/google/skia/blob/master/src/ports/SkMemory_malloc.cpp#L66)

最终会调用 `malloc()` 分配指定大小的内存。部分关键代码如下：

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

如果沿着缺省的 `HeapAllocator` 走，则`allocator->allocPixelRef()` 流程如下：

+ [SkBitmap::tryAllocPixels](https://github.com/google/skia/blob/master/src/core/SkBitmap.cpp#L213)
+ [HeapAllocator::allocPixelRef](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/core/jni/android/graphics/Graphics.cpp#616)
+ [android::Bitmap::allocateHeapBitmap](https://android.googlesource.com/platform/frameworks/base/+/master/libs/hwui/hwui/Bitmap.cpp#79)

可以看到最终也会分配指定大小的内存：

```cpp
static sk_sp<Bitmap> allocateHeapBitmap(size_t size, const SkImageInfo& info, size_t rowBytes) {
    void* addr = calloc(size, 1);
    if (!addr) {
        return nullptr;
    }
    return sk_sp<Bitmap>(new Bitmap(addr, size, info, rowBytes));
}
```

### 解码

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-sk-codec-class.png)

+ [SkAndroidCodec::MakeFromStream()](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L78) 根据图片格式选择一个合适的 `SkCodec`，比如为 PNG 图片选择 `SkPngCodec`
+ `SkAndroidCodec::MakeFromStream()` 创建 `SkAndroidCodec`，`SkAndroidCodec` 是上一步创建的 `SkCodec` 的代理
  + PNG，JPEG，GIF，BMP 等格式时创建 `SkSampledCodec`
  + WEBP 格式时创建 `SkAndroidCodecAdapter`
+ 调用 `SkAndroidCodec.getAndroidPixels()` 解码

```cpp
static jobject doDecode() {
    ...
    SkCodec::Result result = codec->getAndroidPixels(decodeInfo, decodingBitmap.getPixels(),
            decodingBitmap.rowBytes(), &codecOptions);
                
    ...
}
```

这里不妨以 PNG 图片为例来看一下接下来的过程。

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmap-creation-decode.png)

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

## 创建Java对象


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
```

```cpp
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

TODO

如何理解 defaultAllocator.getStorageObjAndReset

# 死

+ Bitmap.recycle()
+ NativeAllocationRegistry

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

# 参考
[core/jni/android/graphics/BitmapFactory.cpp - platform/frameworks/base.git - Git at Google](https://android.googlesource.com/platform/frameworks/base.git/+/master/core/jni/android/graphics/BitmapFactory.cpp)


[(No Title)](https://medium.freecodecamp.com/how-jpg-works-a4dbd2316f35#.p7oto2nw1)

[(No Title)](https://medium.com/@duhroach/how-png-works-f1174e3cc7b7#.84h8fi7d7)

[(No Title)](https://medium.com/@duhroach/how-webp-works-lossly-mode-33bd2b1d0670#.q44inoo38)

https://developer.android.com/topic/performance/graphics/manage-memory.html