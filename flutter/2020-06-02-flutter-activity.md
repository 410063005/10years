# FlutterActivity

## 创建 FlutterEngine

在 `FlutterActivity.onCreate()` 生命周期方法中创建 Flutter 引擎。最终会调用到 `delegate.setupFlutterEngine()` 方法

```
FlutterActivity.onCreate ->
 delegate.onAttach ->
  delegate.setupFlutterEngine
```

`delegate.setupFlutterEngine()` 并不一定真的创建 Flutter 引擎，可有可能是复用已有引擎实例。

+ 第一步，检查是否可以从 `FlutterEngineCache` 中得到缓存的 Flutter 引擎
+ 第二步，检查是否可以从 host 得到 Flutter 引擎实例
+ 以上两个检查失败后才自行创建 Flutter 引擎

```java
 /* package */ void setupFlutterEngine() {
    Log.d(TAG, "Setting up FlutterEngine.");

    // First, check if the host wants to use a cached FlutterEngine.
    String cachedEngineId = host.getCachedEngineId();
    if (cachedEngineId != null) {
      flutterEngine = FlutterEngineCache.getInstance().get(cachedEngineId);
      isFlutterEngineFromHost = true;
      if (flutterEngine == null) {
        throw new IllegalStateException("The requested cached FlutterEngine did not exist in the FlutterEngineCache: '" + cachedEngineId + "'");
      }
      return;
    }

    // Second, defer to subclasses for a custom FlutterEngine.
    flutterEngine = host.provideFlutterEngine(host.getContext());
    if (flutterEngine != null) {
      isFlutterEngineFromHost = true;
      return;
    }

    // Our host did not provide a custom FlutterEngine. Create a FlutterEngine to back our
    // FlutterView.
    Log.d(TAG, "No preferred FlutterEngine was provided. Creating a new FlutterEngine for"
        + " this FlutterFragment.");
    flutterEngine = new FlutterEngine(host.getContext(), host.getFlutterShellArgs().toArray());
    isFlutterEngineFromHost = false;
```

注：第一步中使用 Flutter 内置的引擎缓存机制，第二步中则可自定义 Flutter 引擎缓存机制

## 创建 FlutterView

在 `FlutterActivity.onCreate()` 生命周期方法中创建 FlutterView。最终会调用到 `delegate.onCreateView()` 方法 

```
FlutterActivity.onCreate ->
 FlutterActivity.createFlutterView ->
  delegate.onCreateView ->
```

`delegate.onCreateView()` 方法调用 FlutterView 构造方法创建实例。

```java
  @NonNull
  View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Log.v(TAG, "Creating FlutterView.");
    ensureAlive();
    flutterView = new FlutterView(host.getActivity(), host.getRenderMode(), host.getTransparencyMode());
    flutterView.addOnFirstFrameRenderedListener(flutterUiDisplayListener);

    flutterSplashView = new FlutterSplashView(host.getContext());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      flutterSplashView.setId(View.generateViewId());
    } else {
      // TODO(mattcarroll): Find a better solution to this ID. This is a random, static ID.
      // It might conflict with other Views, and it means that only a single FlutterSplashView
      // can exist in a View hierarchy at one time.
      flutterSplashView.setId(486947586);
    }
    flutterSplashView.displayFlutterViewWithSplash(flutterView, host.provideSplashScreen());

    return flutterSplashView;
```

## 关联 FlutterEngine 和 FlutterView

在 `FlutterActivity.onStart()` 生命周期方法中关联 FlutterEngine 和 FlutterView。

```
FlutterActivity.onStart ->
 delegate.onStart ->
  flutterView.attachToFlutterEngine ->
```

```java
  void onStart() {
    Log.v(TAG, "onStart()");
    ensureAlive();

    // We post() the code that attaches the FlutterEngine to our FlutterView because there is
    // some kind of blocking logic on the native side when the surface is connected. That lag
    // causes launching Activitys to wait a second or two before launching. By post()'ing this
    // behavior we are able to move this blocking logic to after the Activity's launch.
    // TODO(mattcarroll): figure out how to avoid blocking the MAIN thread when connecting a surface
    new Handler().post(new Runnable() {
      @Override
      public void run() {
        Log.v(TAG, "Attaching FlutterEngine to FlutterView.");
        flutterView.attachToFlutterEngine(flutterEngine);

        doInitialFlutterViewRun();
      }
    });
  }
```

注：FlutterView 跟 FlutterEngine 关联的本质是 FlutterView.renderSurface 与 FlutterEngine.flutterRenderer 关联

## 取消关联 FlutterEngine 和 FlutterView

在 `FlutterActivity.onStop()` 生命周期方法中取消 FlutterEngine 和 FlutterView 的关联。

```
FlutterActivity.onStop ->
 delegate.onStop ->
  flutterView.detachFromFlutterEngine ->
```

```java
  void onStop() {
    Log.v(TAG, "onStop()");
    ensureAlive();
    flutterEngine.getLifecycleChannel().appIsPaused();
    flutterView.detachFromFlutterEngine();
```

## 清理 FlutterView

onDestroyView 中