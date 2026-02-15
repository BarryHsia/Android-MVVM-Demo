package com.example.mvvmdemo.data.repository

import com.example.mvvmdemo.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户数据仓库实现
 * 
 * @Inject constructor: 告诉 Hilt 如何创建这个类的实例
 * @Singleton: 确保整个应用只有一个实例
 * 
 * Repository 层负责数据获取，是 ViewModel 和数据源之间的中介
 * 
 * 职责：
 * - 决定数据来源（网络、数据库、缓存等）
 * - 提供干净的 API 给 ViewModel
 * - 处理数据转换和缓存逻辑
 * - 使用 Flow 提供响应式数据流
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    // 未来可以注入 ApiService、Database 等
    // private val apiService: ApiService,
    // private val userDao: UserDao
) : UserRepository {
    
    // 模拟内存缓存
    private var cachedUsers: List<User>? = null
    
    /**
     * 获取用户列表
     * 使用 Flow 返回响应式数据流
     */
    override fun getUsers(): Flow<Result<List<User>>> = flow {
        try {
            // 如果有缓存，先发送缓存数据
            cachedUsers?.let {
                emit(Result.success(it))
            }
            
            // 模拟网络延迟
            delay(1000)
            
            // 获取新数据
            val users = fetchUsersFromNetwork()
            cachedUsers = users
            
            // 发送新数据
            emit(Result.success(users))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * 根据 ID 获取用户
     */
    override suspend fun getUserById(id: Int): Result<User?> {
        return try {
            delay(500)
            val user = cachedUsers?.find { it.id == id }
                ?: fetchUsersFromNetwork().find { it.id == id }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 刷新用户数据
     */
    override suspend fun refreshUsers(): Result<List<User>> {
        return try {
            delay(1000)
            val users = fetchUsersFromNetwork()
            cachedUsers = users
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 模拟从网络获取数据
     * 实际项目中，这里会调用 Retrofit API
     */
    private fun fetchUsersFromNetwork(): List<User> {
        return listOf(
            User(1, "张三", "zhangsan@example.com"),
            User(2, "李四", "lisi@example.com"),
            User(3, "王五", "wangwu@example.com"),
            User(4, "赵六", "zhaoliu@example.com"),
            User(5, "孙七", "sunqi@example.com")
        )
    }
}
