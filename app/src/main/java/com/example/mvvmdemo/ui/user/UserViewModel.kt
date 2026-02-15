package com.example.mvvmdemo.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmdemo.data.model.User
import com.example.mvvmdemo.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * 用户 ViewModel
 * 
 * ViewModel 的职责：
 * - 持有和管理 UI 相关的数据
 * - 处理业务逻辑
 * - 通过 LiveData 向 View 暴露数据
 * - 在配置更改（如屏幕旋转）时保留数据
 * 
 * 重要原则：
 * - 不持有 View、Activity 或 Context 的引用
 * - 使用 viewModelScope 处理协程，自动取消
 */
class UserViewModel : ViewModel() {
    
    // Repository 实例
    private val repository = UserRepository()
    
    // 私有的 MutableLiveData，只能在 ViewModel 内部修改
    private val _users = MutableLiveData<List<User>>()
    // 公开的 LiveData，View 只能观察不能修改
    val users: LiveData<List<User>> = _users
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    /**
     * 加载用户列表
     * 使用 viewModelScope 启动协程，当 ViewModel 被清除时自动取消
     */
    fun loadUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 从 Repository 获取数据
                val userList = repository.getUsers()
                
                // 更新 LiveData，View 会自动收到通知
                _users.value = userList
                
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refreshUsers() {
        loadUsers()
    }
}
