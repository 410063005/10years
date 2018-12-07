
`ConcurrentHashMap`看起来很复杂。这里只是简单了解一下，试图回答下面这个问题：

`ConcurrentHashMap`与`Collections.synchronizedMap()`有何不同？

先来看`Collections.synchronizedMap()`。这个方法的返回值是`SynchronizedMap`的实例。`SynchronizedMap`是`Collections`中的私有内部类。`SynchronizedMap`是原始map的代理。

```java
private static class SynchronizedMap<K,V>
    implements Map<K,V>, Serializable {
    private final Map<K,V> m;     // Backing Map
    final Object      mutex;        // Object on which to synchronize
    public int size() {
        synchronized (mutex) {return m.size();}
    }
	
    public boolean isEmpty() {
        synchronized (mutex) {return m.isEmpty();}
    }

    ...
}
```

再来看`ConcurrentHashMap`。先上注释文档：

> 它是支持完全并发读和高并发写的hash table。这个类遵守`java.util.Hashtable`的一些功能规范，并且有每个方法的对应方法。虽然`ConcurrentHashMap`的每个方法都是线程安全的，但读操作并不需要加锁，而且也不支持锁定整个table以防止任何的访问操作。可以将`ConcurrentHashMap`理解为`Hashtable`的另一个版本，它也是线程安全的，但并不需要锁。

[

[ref]([Java7/8 中的 HashMap 和 ConcurrentHashMap 全解析 - ImportNew](http://www.importnew.com/28263.html))

[ref2](https://javadoop.com/post/hashmap)
