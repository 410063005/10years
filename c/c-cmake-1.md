[make](c-make-1.md)太复杂，很难掌握很难写，对不对？于是有了 [CMake](https://cmake.org/cmake-tutorial/)。

简单来说 CMake 的作用就是你只需要简单配置一下，由它来生成最终的 Makefile。

# 简单用法
项目 `cddemo3` 只有一个源文件 `hello.c`。

```
+ cddemo3
   +----- hello.c
```

首先为 `cddemo3` 添加 `CMakeLists.text`。

```
+ cddemo3
   +----- hello.c
   +----- CMakeLists.txt
```

`CMakeLists.txt` 文件内容如下：

```
cmake_minimum_required (VERSION 2.6)
project (whatever)
add_executable(hello hello.c)
```

创建 `build` 目录。

```
+ cddemo3
   +----- hello.c
   +----- CMakeLists.txt
   +----- build
```

使用 CMake 的一个注意点。

不是进入源文件目录执行 cmake 

```
cd <source_dir>
cmake .
```

而是进入 build 目录执行 cmake

```
cd <build_dir_different_from_source_dir>
cmake <source_dir>
```

# 子项目
现在增加子目录 MathFunctions 作为子项目。

```
+ cddemo3
   +----- hello.c
   +----- CMakeLists.txt
   +----- build
   +----- MathFunctions
          +----- mysqrt.cxx
          +----- CMakeLists.txt
```

MathFunctions 子项目中也有一个 `CMakeLists.txt`。其内容如下：

```
add_library(MathFunctions mysqrt.cxx)
```

而根目录的 `CMakeLists.txt` 进行如下修改：

```
include_directories ("${PROJECT_SOURCE_DIR}/MathFunctions")
add_subdirectory (MathFunctions) 
 
# add the executable
add_executable (hello hello.c)
target_link_libraries (hello MathFunctions)
```

# 更多内容
原文中包含更多内容，我暂时用不上，忽略之。

+ cmakedefine 的用法
+ Installing and Testing
