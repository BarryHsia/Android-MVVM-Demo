package com.example.mvvmdemo.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmdemo.databinding.FragmentUserDatabindingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 使用 DataBinding 的用户列表 Fragment
 * 
 * DataBinding 的优势：
 * - XML 中直接绑定 ViewModel，减少样板代码
 * - UI 自动响应数据变化
 * - 支持双向绑定
 * - 更符合 MVVM 架构理念
 */
@AndroidEntryPoint
class UserFragmentDataBinding : Fragment() {
    
    // DataBinding
    private var _binding: FragmentUserDatabindingBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel - 使用 UserViewModel 而不是 UserViewModelDataBinding
    private val viewModel: UserViewModel by viewModels()
    
    // RecyclerView 适配器
    private lateinit var adapter: UserAdapterDataBinding
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDatabindingBinding.inflate(inflater, container, false)
        
        // 关键：设置 lifecycleOwner，让 DataBinding 能够观察 LiveData
        binding.lifecycleOwner = viewLifecycleOwner
        
        // 绑定 ViewModel 到布局
        binding.viewModel = viewModel
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeUiState()
    }
    
    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = UserAdapterDataBinding(object : UserClickListener {
            override fun onUserClick(user: com.example.mvvmdemo.data.model.User) {
                // 处理用户点击
                // Toast.makeText(context, "点击了: ${user.name}", Toast.LENGTH_SHORT).show()
            }
        })
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UserFragmentDataBinding.adapter
        }
    }
    
    /**
     * 观察 UI 状态
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UserUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.errorLayout.visibility = View.GONE
                            binding.emptyLayout.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                        }
                        is UserUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorLayout.visibility = View.GONE
                            binding.emptyLayout.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            adapter.submitList(state.users)
                        }
                        is UserUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorLayout.visibility = View.VISIBLE
                            binding.emptyLayout.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            binding.textError.text = state.message
                        }
                        is UserUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorLayout.visibility = View.GONE
                            binding.emptyLayout.visibility = View.VISIBLE
                            binding.swipeRefresh.isRefreshing = false
                        }
                    }
                }
            }
        }
        
        // 设置下拉刷新监听
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
        }
        
        // 设置重试按钮监听
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * 用户点击监听器接口
 */
interface UserClickListener {
    fun onUserClick(user: com.example.mvvmdemo.data.model.User)
}
