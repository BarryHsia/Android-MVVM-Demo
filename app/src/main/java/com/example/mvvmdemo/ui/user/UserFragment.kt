package com.example.mvvmdemo.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmdemo.databinding.FragmentUserBinding

/**
 * 用户列表 Fragment
 * 这是 MVVM 中的 View 层
 * 
 * View 的职责：
 * - 显示数据
 * - 观察 ViewModel 中的 LiveData
 * - 将用户操作传递给 ViewModel
 * - 不包含业务逻辑
 */
class UserFragment : Fragment() {
    
    // ViewBinding，用于访问布局中的视图
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    
    // 使用 by viewModels() 委托创建 ViewModel
    // Fragment KTX 提供，自动处理生命周期
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
        setupObservers()
        setupListeners()
        
        // 首次加载数据
        viewModel.loadUsers()
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
     * 观察 ViewModel 中的 LiveData
     * 这是 MVVM 的核心：View 观察数据变化并自动更新
     */
    private fun setupObservers() {
        // 观察用户列表
        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = isLoading
        }
        
        // 观察错误信息
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
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
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
