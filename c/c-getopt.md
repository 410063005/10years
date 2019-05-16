简单总结一下 `getopt()` 的用法。

参考自[这里](https://randu.org/tutorials/c/io.php)。

命令行程序使用 `getopt()` 方法接收参数。该方法在 `getopt.h` 头文件中。其原型为：

```c
  /* This section of our program is for Function Prototypes */
  int getopt(int argc, char * const argv[], const char *optstring);
  extern char *optarg;
  extern int optind, opterr, optopt;
```

`getopt()` 接收的参数来自 `main()` 方法的参数：

```c
  int main(int argc, char **argv)
```

`getopt()` 方法的一个例子如下：

```c
  int ich;

  while ((ich = getopt (argc, argv, "ab:c")) != EOF) {
    switch (ich) {
      case 'a': /* Flags/Code when -a is specified */
        break;
      case 'b': /* Flags/Code when -b is specified */
                /* The argument passed in with b is specified */
		/* by optarg */
        break;
      case 'c': /* Flags/Code when -c is specified */
        break;
      default: /* Code when there are no parameters */
        break;
    }
  }

  if (optind < argc) {
    printf ("non-option ARGV-elements: ");
    while (optind < argc)
      printf ("%s ", argv[optind++]);
    printf ("\n");
  }
```

`ab:c` 指定了允许 a, b, c 三个*选项*。而 `b` 后面的冒号表示选项允许带*参数*。比如

```
-b gradient
```

当 `getopt()` 解析到 `b` 选项时指定的 `gradient` 参数会被保存在 `optarg` 中。

`optind` 表示当前被解析到的参数的索引。

`if (optind < argc)` 用于检查是否传入了多余的参数。

假设程序名是 `junk`，经过以下调用：

```
./junk -b gradient yeehaw
```

`getopt()` 解析后的结果如下：

```
   Variable               Contains
   ------------------     ----------
   argc                   4
   argv[0]                "./junk"
   argv[1]                "-b"
   argv[2]                "gradient"
   argv[3]                "yeehaw"
   optarg at case 'b'     "gradient"
   optind after while     3
     getopt loop
```