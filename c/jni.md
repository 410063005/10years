
# 介绍

JNI defines two key data structures, "JavaVM" and "JNIEnv" (本质上都是 pointers to pointers to function tables)

+ JavaVM provides the "invocation interface" functions
+ JNIEnv provides most of the JNI functions

**JNIEnv is used for thread-local storage. For this reason, you cannot share a JNIEnv between threads.**

+ `FindClass` - Get the class object reference for the class
+ `GetFieldID` - Get the field ID for the field
+ `GetIntField` - Get the contents of the field with something appropriate



---


# 参考
[JNI tips  |  Android NDK  |  Android Developers](https://developer.android.com/training/articles/perf-jni)