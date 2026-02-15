package com.example.mvvmdemo.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmdemo.databinding.FragmentUserBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 用户列表 Fragment
 * 这是 MVVM 中的 View 层
 * 
 * @AndroidEntryPoint: 使 Fragment 可以接收 Hilt 注入的依赖
 * 
 * View 的职责：
 * - 显示数据
 * - 收集 ViewModel 中的 StateFlow
 * - 将用户操作传递给 ViewModel
 * - 不包含业务逻辑
 * 
 * StateFlow 收集最佳实践：
 * - 使用 repeatOnLifecycle(Lifecycle.State.STARTED)
 * - 确保在 Fragment 不可见时停止收集，避免资源浪费
 * - 在 Fragment 重新可见时自动恢复收集
 */
@AndroidEntryPoint
class UserFragment : Fragment() {
    
    // ViewBinding，用于访问布局中的视图
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    
    // 使用 by viewModels() 委托创建 ViewModel
    // Hilt 会自动注入 ViewModel 的依赖
    private val viewModel: UserViewModel by viewModels()
    
    // RecyclerView 适配器
    private lateinit var adapter: UserAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }
    
    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = UserAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UserFragment.adapter
        }
    }
    
    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
        }
        
        // 重试按钮
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }
    
    /**
     * 观察 UI 状态
     * 
     * 使用 repeatOnLifecycle 的好处：
     * - 生命周期感知，Fragment 不可见时自动停止收集
     * - 避免在后台浪费资源
     * - Fragment 重新可见时自动恢复收集
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }
    
    /**
     * 处理不同的 UI 状态
     * 使用 when 表达式处理密封类，编译器会检查是否处理了所有情况
     */
    private fun handleUiState(state: UserUiState) {
        when (state) {
            is UserUiState.Loading -> {
                showLoading()
            }
            is UserUiState.Success -> {
                showSuccess(state.users)
            }
            is UserUiState.Error -> {
                showError(state.message)
            }
            is UserUiState.Empty -> {
                showEmpty()
            }
        }
    }
    
    /**
     * 显示加载状态
     */
    private fun showLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            emptyLayout.visibility = View.GONE
            swipeRefresh.isRefreshing = false
        }
    }
    
    /**
     * 显示成功状态
     */
    private fun showSuccess(users: List<com.example.mvvmdemo.data.model.User>) {
        binding.apply {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
            emptyLayout.visibility = View.GONE
            swipeRefresh.isRefreshing = false
        }
        adapter.submitList(users)
    }
    
    /**
     * 显示错误状态
     */
    private fun showError(message: String) {
        binding.apply {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            textError.text = message
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示空状态
     */
    private fun showEmpty() {
        binding.apply {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
            swipeRefresh.isRefreshing = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
