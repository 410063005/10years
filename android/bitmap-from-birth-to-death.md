
http://km.oa.com/articles/show/408363?kmref=home_headline

# 总体流程

```
java: BitmapFactory.decodeXXX() ->
cpp:  BitmapFactory.doDecode() ->
      SkCodec::MakeFromStream 
      SkAndroidCodec::MakeFromCodec
      Choose decodeAllocator
      codec->getAndroidPixel

```



#  BitmapFactory.doDecode() 

+ Set default values for the options parameters.
+ Update with options supplied by the client.
+ Create the codec.
+ Determine the output size.
+ Scale
+ Choose decodeAllocator 
+ Use SkAndroidCodec to perform the decode.
+ Create the java bitmap

[BitmapFactory](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/pie-release/core/jni/android/graphics/BitmapFactory.cpp)

# SkBitmap

[SkBitmap](https://android.googlesource.com/platform/external/skia/+/jb-mr1-dev/include/core/SkBitmap.h)

[SkBitmap](https://android.googlesource.com/platform/external/skia/+/7cf0e9e/src/core/SkBitmap.cpp)

# SkCodec 
[codec](https://github.com/google/skia/tree/master/src/codec)

类结构：

```
SkNoncopyable

SkCodec

SkAndroidCodec

SkSampledCodec SkAndroidCodecAdapter
```

SkAndroidCodec 依赖 SkCodec：

```
/**
 *  Abstract interface defining image codec functionality that is necessary for
 *  Android.
 */
class SK_API SkAndroidCodec : SkNoncopyable {
private:
    const SkImageInfo               fInfo;
    const ExifOrientationBehavior   fOrientationBehavior;
    std::unique_ptr<SkCodec>        fCodec;
};
```

[选择 SkCodec](https://github.com/google/skia/blob/master/src/codec/SkCodec.cpp)

+ SkBmpCodec
+ SkGifCodec
+ SkJpegCodec
+ SkPngCodec
+ SkWebpCodec

SkPngCodec 依赖 [libpng](https://android.googlesource.com/platform/external/libpng/+/refs/heads/master)

[选择 SkAndroidCodec](https://github.com/google/skia/blob/master/src/codec/SkAndroidCodec.cpp#L84)

```
std::unique_ptr<SkAndroidCodec> codec;
codec = SkAndroidCodec::MakeFromCodec(std::move(c));
SkCodec::Result result = codec->getAndroidPixels(decodeInfo, decodingBitmap.getPixels(),
            decodingBitmap.rowBytes(), &codecOptions);
```

+ PNG, JPEG, GIF, BMP - 使用 SkSampledCodec
+ WEBP - 使用 SkAndroidCodecAdapter

# SkBitmap::Allocator

+ HeapAllocator - 缺省的 decodeAllocator
+ ScaleCheckingAllocator
+ RecyclingPixelAllocator
+ SkBitmap::HeapAllocator

# recyle

Bitmap.recycle()

# 参考
[core/jni/android/graphics/BitmapFactory.cpp - platform/frameworks/base.git - Git at Google](https://android.googlesource.com/platform/frameworks/base.git/+/master/core/jni/android/graphics/BitmapFactory.cpp)


[(No Title)](https://medium.freecodecamp.com/how-jpg-works-a4dbd2316f35#.p7oto2nw1)

[(No Title)](https://medium.com/@duhroach/how-png-works-f1174e3cc7b7#.84h8fi7d7)

[(No Title)](https://medium.com/@duhroach/how-webp-works-lossly-mode-33bd2b1d0670#.q44inoo38)

https://developer.android.com/topic/performance/graphics/manage-memory.html