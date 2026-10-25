# LibXposed 102 R8 / ProGuard 混淆保留规则
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list

# 保留 Xposed 模块入口类及全部成员，禁止混淆名称以防反射加载失败
-keep class io.github.yilink1.hyperselect.MainHook {
    *;
}

# 保留配置与组件类
-keep class io.github.yilink1.hyperselect.ConfigProvider { *; }
-keep class io.github.yilink1.hyperselect.RuleConfig { *; }

# 保留异常堆栈行号和源文件名，方便排查崩溃
-keepattributes SourceFile,LineNumberTable
