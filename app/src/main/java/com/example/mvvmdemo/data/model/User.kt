package com.example.mvvmdemo.data.model

/**
 * 用户数据模型
 * 这是 MVVM 中的 Model 层，定义数据结构
 */
data class User(
    val id: Int,
    val name: String,
    val email: String
)
