# IllegalStateException: Duplicate key

开发一个业务模块，使用了 `List.stream()`。遇到一个奇怪的 bug，提示：

```
java.lang.IllegalStateException: Duplicate key xxx
```

一开始没明白原因，后来发现这个 demo 可以复现问题。

```java
    public void collect() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("a");
        Map<String, String> map = list.stream()
                .collect(Collectors.toMap(String::toUpperCase, value -> value));
        System.out.println(map);
    }
```

完整的异常堆栈如下：

```
java.lang.IllegalStateException: Duplicate key a

	at java.util.stream.Collectors.lambda$throwingMerger$0(Collectors.java:133)
	at java.util.HashMap.merge(HashMap.java:1254)
	at java.util.stream.Collectors.lambda$toMap$58(Collectors.java:1320)
	at java.util.stream.ReduceOps$3ReducingSink.accept(ReduceOps.java:169)
	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1382)
	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:481)
	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:471)
	at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:708)
	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:499)
```

从 demo 不以理解问题产生的原因。`list` 中有两个 `a`，所以将 `list` 转换成 `map` 过程中必须出现相同 key，而 `map` 不允许有相同的 key，所以这里抛出 `java.lang.IllegalStateException` 提示 key 冲突。