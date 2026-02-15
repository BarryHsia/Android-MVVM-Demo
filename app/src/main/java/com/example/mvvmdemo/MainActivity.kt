package com.example.mvvmdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvmdemo.ui.user.UserFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主活动
 * 负责托管 Fragment
 * 
 * @AndroidEntryPoint 注解使 Activity 可以接收 Hilt 注入的依赖
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 首次创建时添加 Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, UserFragment())
                .commit()
        }
    }
}
