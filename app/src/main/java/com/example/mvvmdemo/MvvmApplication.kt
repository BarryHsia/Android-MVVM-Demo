package com.example.mvvmdemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 类
 * 使用 @HiltAndroidApp 注解启用 Hilt 依赖注入
 * 
 * 这是 Hilt 的入口点，必须在 AndroidManifest.xml 中注册
 */
@HiltAndroidApp
class MvvmApplication : Application()
