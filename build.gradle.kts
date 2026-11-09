// Top-level build file where you can add configuration options common to all sub-projects/modules.

//此文件声明构建插件及版本
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    //指定需要使用的google services插件  4.5.0为插件版本  暂时不启用

}