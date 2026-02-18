package com.example.mvvmdemo.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmdemo.data.model.User
import com.example.mvvmdemo.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 使用 DataBinding 的 ViewModel
 * 
 * 与 ViewBinding 版本的区别：
 * - 暴露 LiveData 供 XML 直接绑定
 * - 提供计算属性（如 isLoading, hasError）
 * - 更符合 MVVM 的数据驱动理念
 */
@HiltViewModel
class UserViewModelDataBinding @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI 状态
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    // 为 DataBinding 提供的 LiveData 属性
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _hasError = MutableLiveData(false)
    val hasError: LiveData<Boolean> = _hasError
    
    private val _isEmpty = MutableLiveData(false)
    val isEmpty: LiveData<Boolean> = _isEmpty
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    init {
        loadUsers()
    }
    
    /**
     * 加载用户列表
     */
    fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers()
                .onStart {
                    _uiState.value = UserUiState.Loading
                    updateUiFlags(UserUiState.Loading)
                }
                .catch { exception ->
                    val errorState = UserUiState.Error(exception.message ?: "未知错误")
                    _uiState.value = errorState
                    updateUiFlags(errorState)
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { userList ->
                            val state = if (userList.isEmpty()) {
                                UserUiState.Empty
                            } else {
                                UserUiState.Success(userList)
                            }
                            _uiState.value = state
                            updateUiFlags(state)
                        },
                        onFailure = { exception ->
                            val errorState = UserUiState.Error(exception.message ?: "加载失败")
                            _uiState.value = errorState
                            updateUiFlags(errorState)
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
            updateUiFlags(UserUiState.Loading)
            
            val result = userRepository.refreshUsers()
            
            result.fold(
                onSuccess = { userList ->
                    val state = if (userList.isEmpty()) {
                        UserUiState.Empty
                    } else {
                        UserUiState.Success(userList)
                    }
                    _uiState.value = state
                    updateUiFlags(state)
                },
                onFailure = { exception ->
                    val errorState = UserUiState.Error(exception.message ?: "刷新失败")
                    _uiState.value = errorState
                    updateUiFlags(errorState)
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
    
    /**
     * 更新 UI 标志位
     * 这些标志位用于 DataBinding 在 XML 中控制视图可见性
     */
    private fun updateUiFlags(state: UserUiState) {
        when (state) {
            is UserUiState.Loading -> {
                _isLoading.value = true
                _hasError.value = false
                _isEmpty.value = false
            }
            is UserUiState.Success -> {
                _isLoading.value = false
                _hasError.value = false
                _isEmpty.value = false
                _users.value = state.users
            }
            is UserUiState.Error -> {
                _isLoading.value = false
                _hasError.value = true
                _isEmpty.value = false
                _errorMessage.value = state.message
            }
            is UserUiState.Empty -> {
                _isLoading.value = false
                _hasError.value = false
                _isEmpty.value = true
            }
        }
    }
}
