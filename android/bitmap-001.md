聊聊 Android 系统是如何缓存 Drawable，以及缓存机制对创建 Bitmap 的影响。

---
如果在一个布局文件直接引用 drawable 资源，类似这样：

```xml
<ImageView android:src="@drawable/demo">
```

Android 会按下面的时序图进行处理：

![load-drawable-for-layout.png](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-for-layout.png)

![load-drawable-from-cache](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-from-cache.png)

其中有几个重要方法包括：

+ LayoutInflator.inflate()
+ TypedArray.getValueForDensity()
+ Resources.loadDrawable()

我们主要看 `Resources.loadDrawable()`。

+ `ResoucesImpl` 是 `Resources` 的实现类
+ `ResoucesImpl` 使用 `DrawableCache` 缓存数据
+ 缓存过程发生在 `ResoucesImpl` 的 `loadDrawable()` 和 `cacheDrawable()` 方法中

# DrawableCache 类
先上类图：

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/drawable-cache-class.png" width="60%" height="60%" >

部分代码如下：

```java
/**
 * Class which can be used to cache Drawable resources against a theme.
 */
class DrawableCache extends ThemedResourceCache<Drawable.ConstantState> {
    /**
     * If the resource is cached, creates and returns a new instance of it.
     *
     * @param key a key that uniquely identifies the drawable resource
     * @param resources a Resources object from which to create new instances.
     * @param theme the theme where the resource will be used
     * @return a new instance of the resource, or {@code null} if not in
     *         the cache
     */
    public Drawable getInstance(long key, Resources resources, Resources.Theme theme) {
        final Drawable.ConstantState entry = get(key, theme);
        if (entry != null) {
            return entry.newDrawable(resources, theme);
        }

        return null;
    }
}

/**
 * Data structure used for caching data against themes.
 *
 * @param <T> type of data to cache
 */
abstract class ThemedResourceCache<T> {
    private ArrayMap<ThemeKey, LongSparseArray<WeakReference<T>>> mThemedEntries;
    private LongSparseArray<WeakReference<T>> mUnthemedEntries;
    private LongSparseArray<WeakReference<T>> mNullThemedEntries;
        
    public void put();
    
    public T get();
}
```

缓存策略：

+ 被 DrawableCache 缓存的对象是 Drawable.ConstantState 而不是 Drawable 本身
+ 只持有被缓存对象的 WeakReference
+ 无缓存数量限制
+ 单独缓存 ColorDrawable 
+ 缓存类型细分为
  + Preloaded cache - 包括 PreloadedDrawables 和 PreloadedColorDrawables
  + DrawableCache - 包括 not themed, null theme, theme-specific

由于 preload 过程只在 zygote 进程启动时发生一次，所以接下来的分析中我们几乎可以忽略 preloaded cache 而只需要关注 drawable cache。

# loadDrawable 方法

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-flow.png" width="60%" height="60%" >

+ `useCache` 为 true 时，从 DrawableCache 中获取 Drawable，获取成功则返回，否则下一步
+ 从 Preloaded cache 中获取 ConstantState，获取成功则由 ConstantState 创建 Drawable，否则由 `loadDrawableForCookie()` 加载 Drawable
+ `useCache` 为 true 时，将新创建的 Drawable 保存到 DrawableCache

`loadDrawableForCookie()` 是这里的关键。如果 

+ DrawableCache 和 Preloaded cache 中都找不到我们的目标 Drawable，
+ 并且目标 Drawable 也不是 ColorDrawable (ColorDrawable 非常简单，直接 `new` 创建就行了)

就该 `loadDrawableForCookie()` 方法出场了。它的代码如下：

```java
public class ResourcesImpl {
    // A stack of all the resourceIds already referenced when parsing a resource. This is used to
    // detect circular references in the xml.
    // Using a ThreadLocal variable ensures that we have different stacks for multiple parallel
    // calls to ResourcesImpl
    private final ThreadLocal<LookupStack> mLookupStack =
            ThreadLocal.withInitial(() -> new LookupStack());
            
    private Drawable loadDrawableForCookie(@NonNull Resources wrapper, @NonNull TypedValue value,
            int id, int density) {
        ...
        LookupStack stack = mLookupStack.get();
        try {
            // Perform a linear search to check if we have already referenced this resource before.
            if (stack.contains(id)) {
                throw new Exception("Recursive reference in drawable");
            }
            stack.push(id);
            try {
                if (file.endsWith(".xml")) {
                    final XmlResourceParser rp = loadXmlResourceParser(
                            file, id, value.assetCookie, "drawable");
                    dr = Drawable.createFromXmlForDensity(wrapper, rp, density, null);
                    rp.close();
                } else {
                    final InputStream is = mAssets.openNonAsset(
                            value.assetCookie, file, AssetManager.ACCESS_STREAMING);
                    AssetInputStream ais = (AssetInputStream) is;
                    dr = decodeImageDrawable(ais, wrapper, value);
                }
            } finally {
                stack.pop();
            }
        } catch (Exception | StackOverflowError e) {
            ...
        }            
    }               

    private static class LookupStack {

        // Pick a reasonable default size for the array, it is grown as needed.
        private int[] mIds = new int[4];
        private int mSize = 0;

        public void push(int id) {
            mIds = GrowingArrayUtils.append(mIds, mSize, id);
            mSize++;
        }

        public boolean contains(int id) {
            for (int i = 0; i < mSize; i++) {
                if (mIds[i] == id) {
                    return true;
                }
            }
            return false;
        }

        public void pop() {
            mSize--;
        }
    }
}
```

`loadDrawableForCookie()` 要解决的问题有三个：

+ 如何防止循环引用问题
+ 如何加载 XML 文件中定义的 Drawable
+ 如何加载 XML 文件以外的 Drawable

TODO 如何将如何防止循环引用问题解释清楚

后两个问题比较常规，这里只给出一个流程。

```
XML -> ||XML Parser|| -> Drawable

Assets -> AssetInputStream -> ImageDecoder.Source -> ||ImageDecoder|| -> Drawable
```

# cacheDrawable 方法

+ `mPreloading` 为 true 时，即预加载阶段将通过校验的数据缓存到 preloaded cache
+ `mPreloading` 为 false 时，数据缓存到 drawable cache

# BitmapDrawable

`BitmapDrawable` 继承自 `Drawable`，`BitmapState` 继承自 `ConstantState`。

```java
public class BitmapDrawable extends Drawable {
    private BitmapState mBitmapState;
    
    final static class BitmapState extends ConstantState {
        Bitmap mBitmap = null;    
        
        BitmapState(Bitmap bitmap) {
            mBitmap = bitmap;
            mPaint = new Paint(DEFAULT_PAINT_FLAGS);
        }        
    }        
}
```

`BitmapDrawable` 作为一种 Drawable，也由上述缓存来管理。如果一个布局中多次加载一个图片资源时，只会产生一个 `Bitmap` 而不是多个 `Bitmap`。下面来验证并分析一下。

布局如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <ImageView
        android:id="@+id/image1"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:scaleType="centerCrop"
        android:src="@drawable/are_u_kidding" />

    <ImageView
        android:id="@+id/image2"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_marginTop="10dp"
        android:scaleType="centerCrop"
        android:src="@drawable/are_u_kidding" />
</LinearLayout>
```

UI 上看到了两个 ImageView 均加载了图片：

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/layout-with-two-imageview.jpg" width="40%" height="40%">

使用 BitmapProfiler 分析发现实际上创建了一个 Bitmap 实例：

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/layout-load-only-one-bitmap.jpg" width="40%" height="40%">

结合之前分析来看一下 BitmapDrawable 和 Bitmap 的创建过程。

对于第一个 ImageView：

+ 经过一系列调用，最终调用到 `ResoucesImpl.loadDrawable()`
+ 查找 DrawableCache，未找到
+ 查找 Preloaded cache，未找到
+ 是否 ColorDrawable？
  + 是，直接创建 ColorDrawable
  + 否，调用 loadDrawableForCookie() 创建 Drawable。注意：**这一步创建了一个 BitmapDrawable 以及一个 Bitmap**
+ 缓存上一步创建的 Drawable
+ 返回 Drawable

对于第二个 ImageView：

+ 经过一系列调用，最终调用到 `ResoucesImpl.loadDrawable()`
+ 查找 DrawableCache，找到。注意这一步找到的是 ConstantState
+ 由 ConstantState 创建 Drawable。注意：**这一步创建了一个 BitmapDrawable，但是并没有创建 Bitmap**
+ 返回 Drawable

最终你在 UI 上看到了两个 BitmapDrawable，但背后只有一个 Bitmap。用图画出来，它们关系如下：

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmapdrawable-bitmap-relationship.png" width="60%" height="60%" >

另外要注意的就是 UI 销毁引起对象之间引用关系的变化。UI 销毁后再也没有对 BitmapState 的强引用，而 DrawableCache 只持有 BitmapState 的弱引用，所以下次 GC 时 BitmapState 及 Bitmap 也会被回收。

<img src="https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/bitmapdrawable-gc.png" width="80%" height="80%" >
