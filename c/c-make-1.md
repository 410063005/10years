[A Simple Makefile Tutorial](http://www.cs.colby.edu/maxwell/courses/tutorials/maketutor/)

Makefile 是用于C代码编译的一种简单方式。

# 示例
假设有三个文件

+ `hellomake.c`
+ `hellofunc.c`
+ `hellomake.h`

```c
// hellomake.c	
#include <hellomake.h>

int main() {
  // call a function in another file
  myPrintHelloMake();

  return(0);
}

// hellofunc.c	
#include <stdio.h>
#include <hellomake.h>

void myPrintHelloMake(void) {

  printf("Hello makefiles!\n");

  return;
}

// hellomake.h
/*
example include file
*/

void myPrintHelloMake(void);
```

你会执行以下命令来编译：

```
gcc -o hellomake hellomake.c hellofunc.c -I.

```

这个命令编译两个 .c 文件，生成的可执行文件名字是 `hellomake`。`-I.` 是让 gcc 在当前目录中找到头文件 `hellomake.h`。

如果不使用 makefile，test/modify/debug 周期一个典型做法是在终端中使用 up 箭头来重复执行上一条命令。这种做法有两个问题：

+ 重新输入命令很低效，尤其是完全从头输入的话
+ 如果只修改了一个 .c 文件却要重新编译所有文件会很耗时

# 脚本一
可以创建一个简单来的 makefile 文件来解决这个问题：

```
hellomake: hellomake.c hellofunc.c
     gcc -o hellomake hellomake.c hellofunc.c -I.
```

文件名是 Makefile 或 makefile。`make` 命令会执行文件中的规则。`make` 命令不带参数时会执行文件中的第一条规则。

`:` 后面是编译 `hellomake` 的依赖文件。其中任何一个发生变化时都会重新执行 `hellomake` 这条规则。

这个 makefile 解决了反复使用 up 箭头来重新执行上一个命令来编译的问题。但这个脚本仍然不高效。

# 脚本二

```
CC=gcc
CFLAGS=-I.

hellomake: hellomake.o hellofunc.o
     $(CC) -o hellomake hellomake.o hellofunc.o
```

将 .o 文件在作为规则的依赖，`make` 知道要先将 .c 编译成 .o 文件，然后再编译 `hellomake` 可执行文件。

这个脚本可以应对大部分小规模项目。

但这个脚本有个问题：如果你修改了 `hellomake.h` 文件，make 不会重新编译 .c 文件(即使它应该编译)。为了解决问题，我们要告诉 make .c 文件依赖哪些 .h 文件。

# 脚本三

```
CC=gcc
CFLAGS=-I.
DEPS = hellomake.h

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

hellomake: hellomake.o hellofunc.o 
	$(CC) -o hellomake hellomake.o hellofunc.o 
```

+ `DEPS` - 宏，定义了 .c 依赖哪些 .h
+ `-c flag` - generate the object file
+ `-o $@ ` - 将编译生成结果保存到指定的文件中，比如说 `a.c` 的编译结果保存到 `a.o`
+ `$<` - the first item in the dependencies list
+ `$@` - : 左边的部分
+ `$^` - : 右边的部分

# 脚本四
让脚本三更通用化一些：

```
CC=gcc
CFLAGS=-I.
DEPS = hellomake.h
OBJ = hellomake.o hellofunc.o 

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

hellomake: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)
```

这个脚本比脚本三通用了，但仍然存在很多问题：

+ 能不能把头文件放在 `include` 目录，源文件放在 `src` 目录，本地库文件放到 `lib` 目录？
+ 那些数量极多的 .o 到处都是，能不能放到同一个子目录下

答案是可以的，见脚本五

# 脚本五

```
IDIR =../include
CC=gcc
CFLAGS=-I$(IDIR)

ODIR=obj
LDIR =../lib

LIBS=-lm

_DEPS = hellomake.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = hellomake.o hellofunc.o 
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))


$(ODIR)/%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

hellomake: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS) $(LIBS)

.PHONY: clean

clean:
	rm -f $(ODIR)/*.o *~ core $(INCDIR)/*~ 
```

+ makefile 自己放在 src 目录
+ 头文件放在 include 目录
+ 源文件放在 src 目录
+ 本地库文件放在 lib 目录

这个脚本足以应对小规模到中等规模的软件项目。
