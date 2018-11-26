
`ThreadLocal`实现了线程本地变量这样一个概念。不好理解是吧？看个实际例子。

我们知道Android中有裸线程之说。裸线程没有`Looper`，所以不能用于处理`Message`。新起的工作线程是裸线程，要调用`Looper.prepare()`后为其准备`Looper`后才能处理`Message`。

但是，一个线程只能有一个`Looper`，不能有多个。比如，主线程不是裸线程，系统已经为其添加了`Looper`，如果我们再次调用`Looper.prepare()`，马上报错`RuntimeException: Only one Looper may be created per thread`。

一个线程只能有一个`Looper`这个限制，正是使用`ThreadLocal`来实现的。关键代码如下：

```java
public final class Looper {
    // sThreadLocal.get() will return null unless you've called prepare().
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
	
    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }	
}
```

`ThreadLocal`如此神奇，怎么实现的？

# ThreadLocal介绍

> 这个类用于提供thread-local变量。thread-local变量跟普通变量不同，每个线程可以访问各自的thread-local变量，各个线程的thread-local变量是相互独立的。thread-local变量通常是private static的，用于表示线程状态，比如user ID或Transaction ID

注释文档中给出的一个例子：

```java
public class ThreadId {
	// Atomic integer containing the next thread ID to be assigned
	private static final AtomicInteger nextId = new AtomicInteger(0);
	// Thread local variable containing each thread's ID
	private static final ThreadLocal&lt;Integer> threadId =
	    new ThreadLocal<Integer>() {
	        @Override protected Integer initialValue() {
	            return nextId.getAndIncrement();
		}
	};
	
	// Returns the current thread's unique ID, assigning it if necessary
	public static int get() {
	    return threadId.get();
	}
}
```

# ThreadLocal源码简析
分析`ThreadLocal`源码前先简单思考下，如何实现每个线程有各自独立的变量呢？最直观的理解就是利用这样一个`Map`：这个`Map`中线程本身作为key，thread-local变量作为value。

正如我们理解的那样，确实有这样的一个`Map`，那就是`ThreadLocalMap`。

`ThreadLocalMap`是`ThreadLocal`的内部私有的自定义hash map，用于操作thread local变量。

```java
static class ThreadLocalMap {
	
	private Entry[] table;
	
	private Entry getEntry(ThreadLocal key) {}
	
	private void set(ThreadLocal key, Object value) {}
	
	private void remove(ThreadLocal key) {}
	
}
```

注意：`ThreadLocalMap.Entry`比较特殊。通常的Entry会继承/实现`Map.Entry`，而`ThreadLocalMap.Entry`是继承自`WeakReference<ThreadLocal>`，它是一个弱引用！

```java
public class ThreadLocal<T> {

 	// 返回当前线程的thread-local变量初始值。当使用`get()`方法第一次访问thread-local变量时会调用这个方法，除非之前调用过`set()`。
	// 正常情况下`initialValue()`只为每个调用调用一次。但如果`remove`后再次`get`，仍然会导致`initialValue()`被调用
	protected T initialValue() {
        return null;
    }

	// 返回当线程线程的thread-local变量。如果变量没有值，会先调用`initialValue()`进行初始化
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null)
                return (T)e.value;
        }
        return setInitialValue();
    }
	
	// 设置当前线程的thread-local变量
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }
	
	// 清除当前线程的thread-local变量
    public void remove() {
        ThreadLocalMap m = getMap(Thread.currentThread());
        if (m != null)
            m.remove(this);
    }

	// 获取thread-local的map
	ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

	// 创建thread-local的map
	void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }
}
```