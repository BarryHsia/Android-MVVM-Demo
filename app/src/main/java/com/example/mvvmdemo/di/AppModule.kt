package com.example.mvvmdemo.di

import com.example.mvvmdemo.data.repository.UserRepository
import com.example.mvvmdemo.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 * 
 * @Module: 标记这是一个 Hilt 模块
 * @InstallIn: 指定模块的生命周期范围
 * SingletonComponent: 应用级单例，整个应用生命周期内只有一个实例
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    /**
     * 提供 UserRepository 实现
     * 
     * @Binds: 用于绑定接口和实现类
     * @Singleton: 确保整个应用只有一个实例
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
