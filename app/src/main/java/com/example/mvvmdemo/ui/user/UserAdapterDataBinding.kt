package com.example.mvvmdemo.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmdemo.data.model.User
import com.example.mvvmdemo.databinding.ItemUserDatabindingBinding

/**
 * 使用 DataBinding 的用户列表适配器
 * 
 * DataBinding 的优势：
 * - 在 ViewHolder 中直接绑定数据，无需手动设置
 * - 支持点击事件绑定
 * - 代码更简洁
 */
class UserAdapterDataBinding(
    private val clickListener: UserClickListener
) : ListAdapter<User, UserAdapterDataBinding.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserDatabindingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
    
    class UserViewHolder(
        private val binding: ItemUserDatabindingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User, clickListener: UserClickListener) {
            // DataBinding 的核心：直接设置变量，UI 自动更新
            binding.user = user
            binding.clickListener = clickListener
            
            // 立即执行绑定（可选，但推荐）
            binding.executePendingBindings()
        }
    }
    
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
