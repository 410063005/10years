
总是学习不会C和C++，感觉在"Hello World"之类的demo和实际工程之间有一个很大的沟，无法跨过去。学习Java, Javascript, Kotlin时则没有这种无法跨过去的感觉。


# 关键点
对比已经掌握的编程语言，列出C和C++中我需要攻克的几个关键点：

+ 语法层面 - C, 尤其 C++ 语法上有些奇怪的用法，看到就一头雾水。对这些语法不求会写但求能看懂
+ 代码组织方式 - 要习惯头文件和源文件分离的惯例
+ 构建方式

+ 基本IO
+ 文件处理
+ 字符串及格式化
+ 日期和时间
+ 错误处理
+ 日志和调试

+ 数组
+ 集合
+ 其他语言特性
  + C - 指针, 可变参数
  + Java - 多线程
+ 网络调用
+ 进程和线程
+ 异步处理

+ 系统函数和库
+ 第三方库
+ 其他
  + 程序参数
  + 文件处理
  + 字符串及格式化
  + 日期和时间
  + 进程
  + 线程
  + 定时器

+ JNI


+ NDK
  + Android 日志输出
  + Android 相关函数

# 基本IO

+ `int getchar(void)`
+ `int putchar(int c)`

+ `char* gets(char* s)`
+ `int puts(const char* s)`

+ `scanf(const char* format, ...)`
+ `printf(const char* format, ...)`

# 文件处理

```
FILE *fopen( const char * filename, const char * mode );
int fclose( FILE *fp );
int fputc( int c, FILE *fp );
int fputs( const char *s, FILE *fp );
int fgetc( FILE * fp );
char *fgets( char *buf, int n, FILE *fp );
```

mode 包括：

+ r
+ w
+ a
+ r+
+ w+
+ a+

另外，处理二进制文件这样写就行了：

```
"rb", "wb", "ab", "rb+", "r+b", "wb+", "w+b", "ab+", "a+b"
```

两个专门用于读写二进制文件的函数：

```
size_t fread(void *ptr, size_t size_of_elements, size_t number_of_elements, FILE *a_file);

size_t fwrite(const void *ptr, size_t size_of_elements, size_t number_of_elements, FILE *a_file);
```

# 日期和时间

+ clock_t clock(void) - returns the number of clock ticks elapsed since the program was launched
+ char *ctime(const time_t *timer) - returns a string representing the localtime based on the argument timer
+ 

获取当前时间：

```c
#include <stdio.h>
#include <time.h>

int main () {
   time_t curtime;

   time(&curtime);

   printf("Current time = %s", ctime(&curtime));

   return(0);
}
```

# 错误处理
> As such C programming does not provide direct support for error handling but being a system programming language, it provides you access at lower level in the form of return values. Most of the C or even Unix function calls return -1 or NULL in case of any error and sets an error code errno is set which is global variable and indicates an error occurred during any function call. You can find various error codes defined in <error.h> header file.

C不对错误处理提供直接支持。按C惯例是通过返回值来判断是否出错，通常返回 -1 或 NULL 表示出错。

全局变量 `errno` 表示函数调用是否出错。程序初始化时将 `errno` 置为 0。



# 日志和调试

```c
int main(int argc, char* argv[])
{
    printf("%s\n", __DATE__);
    printf("%s\n", __TIME__);
    printf("%s\n", __FILE__);
    printf("%d\n", __LINE__);
    return 0;
}
```

`__FILE__` 和 `__LINE__` 两个宏可以作为调试工具。

# 其他语言特性

C语言如何处理多参数：

```c
#include <stdio.h>
#include <stdarg.h>

double average(int num,...)
{
	va_list valist;
	double sum = 0.0;
	int i;
	/* initialize valist for num number of arguments */
	va_start(valist, num);
	/* access all the arguments assigned to valist */
	for (i = 0; i < num; i++) {
		sum += va_arg(valist, int);
	}
	/* clean memory reserved for valist */
	va_end(valist);
	return sum/num;
}
```

# 知识点
## 变量初始值
> For definition without an initializer: variables with static storage duration are implicitly initialized with NULL allbyteshavethevalue0; the initial value of all other variables is undefined.

## 存储类型
storage class 定义了 variables/functions 的可见性和生命周期。

static - 静态变量
  + 程序运行期间一直存在
  + static用于修饰全局变量时将该全局变量的可见性限制在当前文件
  + static用于类成员时跟Java中有类似含义

> Extern is used to declare a global variable or function in another file.
> The extern modifier is most commonly used when there are two or more files sharing the same global variables or functions.

## 数组

```
double balance[5] = {1000.0, 2.0, 3.4, 7.0, 50.0};
```

## 指针
指针是变量，其值是另一个变量的地址。

```
int a = 19;
int* addr = &a;
printf("address of a is %p\n", addr);
```

## 字符串
C语言中字符串以 `\0` (即 null 结尾)

常用的字符串函数：

+ strcpy
+ strcat
+ strlen
+ strcmp
+ strchr
+ strstr

## typedef

typedef 用于为数据类型定义新的名字，增加程序的可读性。

# 学习心得
作为已有多年开发经验的工程师，不能像新手一样学习。

例一，新手关注的是 `printf()` 的用法，我应该关注 `stdio.h` 里有些什么，而不应该关注 `printf()` 的用法。原因很简单：

+ 各种编程语言的输出函数大同小异
+ 基本用法一看就会，高级用法记不住就去查文档

例二，新手关注的是记住各种数据类型，我应该关注从更高的视角来看数据类型。原因很简单：

+ 各种编程语言的基本数据类型几乎是一致的
+ 我应关注C/C++中相较其他语言更底层/更灵活的数据类型

跟Java类似的数据类型：

+ 枚举
+ struct 类型
+ void 类型

Java中没有的数据类型：

+ union 类型
+ void* 指针类型 - **代表一个对象的地址，而不是对象的类型**。它可以转型成任意类型，很强大很灵活，也很容易出错！
+ 无符号整型(了解即可)


> A union is a special data type available in C that enables you to store different data types in the same memory location. You can define a union with many members, but only one member can contain a value at any given time. Unions provide an efficient way of using the same memory location for multi-purpose.

union 这种数据类型一般编程语言并不提供。

# 参考
[C - Quick Guide](https://www.tutorialspoint.com/cprogramming/pdf/c_quick_guide.pdf)

[C Quick Guide](https://www.tutorialspoint.com/cprogramming/c_quick_guide.htm)


