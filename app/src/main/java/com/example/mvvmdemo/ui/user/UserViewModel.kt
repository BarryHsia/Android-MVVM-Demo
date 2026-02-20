package com.example.mvvmdemo.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmdemo.data.model.User
import com.example.mvvmdemo.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户 ViewModel
 * 
 * @HiltViewModel: 标记这是一个 Hilt 管理的 ViewModel
 * @Inject constructor: Hilt 会自动注入 UserRepository
 * 
 * ViewModel 的职责：
 * - 持有和管理 UI 相关的数据
 * - 处理业务逻辑
 * - 通过 StateFlow 向 View 暴露数据
 * - 在配置更改（如屏幕旋转）时保留数据
 * 
 * StateFlow vs LiveData:
 * - StateFlow 是 Kotlin Flow 的一部分，更现代
 * - 支持更强大的操作符（map, filter, combine 等）
 * - 更好的协程集成
 * - 类型安全，编译时检查
 * 
 * 重要原则：
 * - 不持有 View、Activity 或 Context 的引用
 * - 使用 viewModelScope 处理协程，自动取消
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI 状态封装
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    init {
        // ViewModel 创建时自动加载数据
        loadUsers()
    }
    
    /**
     * 加载用户列表
     * 使用 viewModelScope 启动协程，当 ViewModel 被清除时自动取消
     */
    fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers()
                .onStart {
                    // 开始加载时显示加载状态
                    _uiState.value = UserUiState.Loading
                }
                .catch { exception ->
                    // 捕获异常并更新错误状态
                    _uiState.value = UserUiState.Error(
                        exception.message ?: "未知错误"
                    )
                }
                .collect { result ->
                    // 收集数据并更新 UI 状态
                    result.fold(
                        onSuccess = { users ->
                            _uiState.value = if (users.isEmpty()) {
                                UserUiState.Empty
                            } else {
                                UserUiState.Success(users)
                            }
                        },
                        onFailure = { exception ->
                            _uiState.value = UserUiState.Error(
                                exception.message ?: "加载失败"
                            )
                        }
                    )
                }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refreshUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            val result = userRepository.refreshUsers()
            
            result.fold(
                onSuccess = { users ->
                    _uiState.value = if (users.isEmpty()) {
                        UserUiState.Empty
                    } else {
                        UserUiState.Success(users)
                    }
                },
                onFailure = { exception ->
                    _uiState.value = UserUiState.Error(
                        exception.message ?: "刷新失败"
                    )
                }
            )
        }
    }
    
    /**
     * 重试加载
     */
    fun retry() {
        loadUsers()
    }
}

/**
 * UI 状态密封类
 * 
 * 使用密封类的好处：
 * - 类型安全，编译时检查所有分支
 * - 清晰表达所有可能的 UI 状态
 * - 便于处理不同状态的 UI 展示
 */
sealed class UserUiState {
    object Loading : UserUiState()
    object Empty : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(val message: String) : UserUiState()
}
