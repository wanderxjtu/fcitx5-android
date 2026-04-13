# disable obfuscation
-dontobfuscate

# preserve the line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# remove kotlin null checks
-processkotlinnullchecks remove

# 绝对不能混淆 fcitx 相关的所有包名和类名
-keep class org.fcitx.fcitx5.android.** { *; }
-keep interface org.fcitx.fcitx5.android.** { *; }

# 保留所有 R 类，确保资源 ID 不会被删除或内联
-keep class **.R$* {
    <fields>;
}

# 保留 JNI 使用的类和方法
-keepclasseswithmembernames class * {
    native <methods>;
}
