聊聊 Android 系统是如何缓存 Drawable 的。

---

![load-drawable-for-layout.png](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-for-layout.png)

![load-drawable-from-cache](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-from-cache.png)

cache 过程发生在 `ResoucesImpl` 的两个方法中。

+ loadDrawable
+ cacheDrawable

`ResoucesImpl` 使用 `DrawableCache` 缓存数据。

BitmapDrawable

# DrawableCache 类

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

+ 被缓存对象是 drawable.constantState 而不是 drawable 本身
+ 被缓存对象无数量限制
+ 只持有被缓存对象的 WeakReference
+ 缓存类型细分为
  + preloaded cache - 包括 PreloadedDrawables 和 PreloadedColorDrawables
  + drawable cache - 包括 not themed, null theme, theme-specific

# loadDrawable 方法

![](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/201906/load-drawable-flow.png)

+ `useCache` 为 true 时，从 drawable cache 中获取 drawable，获取成功则返回，否则下一步
+ 从 preloaded cache 中获取 constantState，获取成功则由 constantState 创建 drawable，否则由 `loadDrawableForCookie` 加载
+ `useCache` 为 true 时，将新创建的 drawable 保存到 drawable cache

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

`BitmapDrawable` 作为一种 drawable，也由上述缓存来管理。这可以解释为什么一个布局中多次加载一个图片资源时，只会产生一个 `Bitmap` 而不是多个 `Bitmap`。

