package com.example.mvvmdemo.data.repository

import com.example.mvvmdemo.data.model.User
import kotlinx.coroutines.delay

/**
 * 用户数据仓库
 * Repository 层负责数据获取，是 ViewModel 和数据源之间的中介
 * 
 * 职责：
 * - 决定数据来源（网络、数据库、缓存等）
 * - 提供干净的 API 给 ViewModel
 * - 处理数据转换和缓存逻辑
 */
class UserRepository {
    
    /**
     * 获取用户列表
     * 实际项目中，这里会从网络 API 或数据库获取数据
     * 这里使用模拟数据演示
     */
    suspend fun getUsers(): List<User> {
        // 模拟网络延迟
        delay(1000)
        
        // 返回模拟数据
        return listOf(
            User(1, "张三", "zhangsan@example.com"),
            User(2, "李四", "lisi@example.com"),
            User(3, "王五", "wangwu@example.com"),
            User(4, "赵六", "zhaoliu@example.com")
        )
    }
    
    /**
     * 根据 ID 获取用户
     */
    suspend fun getUserById(id: Int): User? {
        delay(500)
        return getUsers().find { it.id == id }
    }
}
