# DataBinding vs ViewBinding 使用说明

## 为什么在 MVVM 中 DataBinding 更好？

### 1. 真正的数据驱动 UI

**ViewBinding:**
```kotlin
// Fragment 中需要手动更新 UI
viewModel.users.observe(viewLifecycleOwner) { users ->
    adapter.submitList(users)
}

viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}
```

**DataBinding:**
```xml
<!-- XML 中直接绑定，自动更新 -->
<ProgressBar
    android:visibility="@{viewModel.isLoading ? View.VISIBLE : View.GONE}" />
```

### 2. 减少样板代码

**ViewBinding:** 需要在 Fragment 中写大量的观察者代码

**DataBinding:** 大部分逻辑在 XML 中声明式完成

### 3. 双向绑定支持

```xml
<!-- 双向绑定：EditText 的变化自动更新 ViewModel -->
<EditText
    android:text="@={viewModel.searchQuery}" />
```

## 项目中的实现

### 1. 启用 DataBinding

```kotlin
// app/build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
        dataBinding = true  // 启用 DataBinding
    }
}
```

### 2. 布局文件结构

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    
    <data>
        <!-- 声明变量 -->
        <variable
            name="viewModel"
            type="com.example.mvvmdemo.ui.user.UserViewModel" />
    </data>
    
    <!-- 实际布局 -->
    <ConstraintLayout>
        <!-- 使用 @{} 绑定数据 -->
        <TextView
            android:text="@{viewModel.userName}" />
    </ConstraintLayout>
</layout>
```

### 3. Fragment 中使用

```kotlin
class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreateView(...): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        
        // 关键：设置 lifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        
        // 绑定 ViewModel
        binding.viewModel = viewModel
        
        return binding.root
    }
}
```

### 4. ViewModel 设计

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    // 使用 LiveData 供 DataBinding 观察
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    // 计算属性
    val hasUsers: LiveData<Boolean> = users.map { it.isNotEmpty() }
}
```

## DataBinding 表达式

### 1. 基本绑定

```xml
<!-- 文本绑定 -->
<TextView android:text="@{user.name}" />

<!-- 可见性绑定 -->
<View android:visibility="@{viewModel.isLoading ? View.VISIBLE : View.GONE}" />

<!-- 启用状态绑定 -->
<Button android:enabled="@{viewModel.canSubmit}" />
```

### 2. 方法引用

```xml
<!-- 点击事件 -->
<Button android:onClick="@{() -> viewModel.loadData()}" />

<!-- 带参数的方法 -->
<Button android:onClick="@{() -> viewModel.deleteUser(user)}" />
```

### 3. 双向绑定

```xml
<!-- 使用 @={} 实现双向绑定 -->
<EditText android:text="@={viewModel.searchQuery}" />

<CheckBox android:checked="@={viewModel.isChecked}" />
```

### 4. 字符串格式化

```xml
<TextView android:text="@{@string/user_info(user.name, user.age)}" />
```

### 5. 空值处理

```xml
<!-- 使用 ?? 运算符 -->
<TextView android:text="@{user.name ?? @string/default_name}" />
```

## BindingAdapter 自定义绑定

```kotlin
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    Glide.with(view.context)
        .load(url)
        .into(view)
}

// XML 中使用
<ImageView app:imageUrl="@{user.avatarUrl}" />
```

## 最佳实践

### 1. 始终设置 lifecycleOwner

```kotlin
binding.lifecycleOwner = viewLifecycleOwner
```

这样 LiveData 才能正确工作。

### 2. 避免在 XML 中写复杂逻辑

❌ 不好：
```xml
<TextView android:text="@{user.age > 18 ? (user.gender == 'M' ? '成年男性' : '成年女性') : '未成年'}" />
```

✅ 好：
```kotlin
// ViewModel 中
val userCategory: LiveData<String> = user.map { 
    when {
        it.age > 18 && it.gender == 'M' -> "成年男性"
        it.age > 18 && it.gender == 'F' -> "成年女性"
        else -> "未成年"
    }
}
```

```xml
<TextView android:text="@{viewModel.userCategory}" />
```

### 3. 使用 Observable 字段（可选）

```kotlin
class UserViewModel : ViewModel() {
    // 使用 ObservableField
    val userName = ObservableField<String>()
    val isLoading = ObservableBoolean(false)
    
    fun loadData() {
        isLoading.set(true)
        // ...
    }
}
```

### 4. 清理绑定

```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

## 性能考虑

### DataBinding 的性能影响

1. **编译时间**: DataBinding 会增加编译时间（约 10-20%）
2. **运行时性能**: 几乎没有影响，因为绑定在编译时生成
3. **APK 大小**: 略微增加（约 50-100KB）

### 优化建议

1. 使用 `executePendingBindings()` 立即执行绑定
2. 避免在 XML 中进行复杂计算
3. 合理使用 `@BindingAdapter` 复用逻辑

## 迁移指南

### 从 ViewBinding 迁移到 DataBinding

1. **启用 DataBinding**
```kotlin
android {
    buildFeatures {
        dataBinding = true
    }
}
```

2. **修改布局文件**
```xml
<!-- 添加 <layout> 根标签 -->
<layout>
    <data>
        <variable name="viewModel" type="..." />
    </data>
    <!-- 原有布局 -->
</layout>
```

3. **更新 Fragment**
```kotlin
// 设置 lifecycleOwner 和 viewModel
binding.lifecycleOwner = viewLifecycleOwner
binding.viewModel = viewModel
```

4. **简化观察者代码**
```kotlin
// 删除手动的 observe 代码
// viewModel.data.observe(...) { }
// 改为在 XML 中绑定
```

## 总结

### 何时使用 DataBinding？

✅ **推荐使用：**
- MVVM 架构项目
- 需要双向绑定
- UI 逻辑较多的页面
- 团队熟悉 DataBinding

❌ **不推荐使用：**
- 简单的 UI
- 团队不熟悉 DataBinding
- 对编译时间敏感的项目
- 需要频繁调试 UI 逻辑

### ViewBinding vs DataBinding 选择

| 特性 | ViewBinding | DataBinding |
|------|-------------|-------------|
| 类型安全 | ✅ | ✅ |
| 编译速度 | 快 | 较慢 |
| 学习曲线 | 低 | 中等 |
| MVVM 支持 | 基础 | 完整 |
| 双向绑定 | ❌ | ✅ |
| XML 表达式 | ❌ | ✅ |
| 推荐场景 | 简单项目 | MVVM 项目 |

**结论：** 对于真正的 MVVM 架构，DataBinding 确实更合适，但需要权衡编译时间和学习成本。
