package com.example.mvvmdemo.data.repository

import com.example.mvvmdemo.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据仓库接口
 * 
 * 使用接口的好处：
 * - 便于单元测试（可以创建 Mock 实现）
 * - 符合依赖倒置原则
 * - 便于切换不同的实现
 */
interface UserRepository {
    
    /**
     * 获取用户列表
     * 返回 Flow 以支持响应式数据流
     */
    fun getUsers(): Flow<Result<List<User>>>
    
    /**
     * 根据 ID 获取用户
     */
    suspend fun getUserById(id: Int): Result<User?>
    
    /**
     * 刷新用户数据
     */
    suspend fun refreshUsers(): Result<List<User>>
}
