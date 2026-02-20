# MVVM 架构学习指南

## 一、MVVM 架构概述

### 什么是 MVVM？

MVVM（Model-View-ViewModel）是一种软件架构模式，特别适合 Android 开发。它将应用程序分为三个核心组件：

```
┌─────────────────────────────────────────┐
│              View (视图层)               │
│    Activity / Fragment / XML Layout     │
│         - 显示数据                       │
│         - 响应用户操作                   │
└──────────────┬──────────────────────────┘
               │ 观察 LiveData
               │ 调用方法
┌──────────────▼──────────────────────────┐
│         ViewModel (视图模型层)           │
│         - 持有 UI 数据                   │
│         - 处理 UI 逻辑                   │
│         - 暴露 LiveData                  │
└──────────────┬──────────────────────────┘
               │ 调用方法
               │ 获取数据
┌──────────────▼──────────────────────────┐
│         Repository (仓库层)              │
│         - 数据获取逻辑                   │
│         - 决定数据来源                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│           Model (模型层)                 │
│         - 数据结构定义                   │
│         - 业务实体                       │
└─────────────────────────────────────────┘
```

### 为什么使用 MVVM？

1. **关注点分离**: UI 和业务逻辑完全解耦
2. **易于测试**: ViewModel 可以独立于 Android 框架进行单元测试
3. **生命周期感知**: ViewModel 在配置更改（如屏幕旋转）时保留数据
4. **响应式编程**: 使用 LiveData 自动更新 UI，减少手动更新代码

## 二、核心组件详解

### 1. Model（模型层）

Model 代表应用的数据和业务逻辑。

**示例：User.kt**
```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

**职责：**
- 定义数据结构
- 表示业务实体
- 不包含 Android 相关代码

### 2. Repository（仓库层）

Repository 是数据层的抽象，负责协调不同数据源。

**示例：UserRepository.kt**
```kotlin
class UserRepository {
    suspend fun getUsers(): List<User> {
        // 可以从网络、数据库或缓存获取数据
        return apiService.getUsers()
    }
}
```

**职责：**
- 决定数据来源（网络、数据库、缓存）
- 提供干净的 API 给 ViewModel
- 处理数据转换和缓存策略
- 不包含 UI 逻辑

### 3. ViewModel（视图模型层）

ViewModel 是 MVVM 的核心，连接 View 和 Model。

**示例：UserViewModel.kt**
```kotlin
class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    
    // 私有可变 LiveData
    private val _users = MutableLiveData<List<User>>()
    // 公开不可变 LiveData
    val users: LiveData<List<User>> = _users
    
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers()
        }
    }
}
```

**职责：**
- 持有和管理 UI 相关的数据
- 处理 UI 逻辑（不是业务逻辑）
- 通过 LiveData 暴露数据给 View
- 在配置更改时保留数据
- 使用 viewModelScope 管理协程

**重要原则：**
- ❌ 不持有 View、Activity、Fragment 或 Context 的引用
- ❌ 不直接操作 UI
- ✅ 使用 LiveData 或 StateFlow 暴露数据
- ✅ 保持可测试性

### 4. View（视图层）

View 负责显示数据和响应用户操作。

**示例：UserFragment.kt**
```kotlin
class UserFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 观察 LiveData
        viewModel.users.observe(viewLifecycleOwner) { users ->
            // 更新 UI
            adapter.submitList(users)
        }
        
        // 触发数据加载
        viewModel.loadUsers()
    }
}
```

**职责：**
- 显示数据
- 观察 ViewModel 中的 LiveData
- 将用户操作传递给 ViewModel
- 不包含业务逻辑

## 三、关键技术点

### 1. LiveData

LiveData 是一个可观察的数据持有类，具有生命周期感知能力。

**特点：**
- 自动管理订阅，避免内存泄漏
- 只在活跃状态（STARTED 或 RESUMED）时更新 UI
- 配置更改时自动重新连接

**使用模式：**
```kotlin
// ViewModel 中
private val _data = MutableLiveData<String>()
val data: LiveData<String> = _data  // 只读

// View 中
viewModel.data.observe(viewLifecycleOwner) { value ->
    textView.text = value
}
```

### 2. ViewModelScope

viewModelScope 是一个与 ViewModel 生命周期绑定的协程作用域。

**特点：**
- ViewModel 被清除时自动取消所有协程
- 避免内存泄漏
- 简化异步操作

**使用示例：**
```kotlin
fun loadData() {
    viewModelScope.launch {
        try {
            val data = repository.getData()
            _data.value = data
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
}
```

### 3. ViewBinding

ViewBinding 提供类型安全的视图访问。

**启用：**
```kotlin
// build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
    }
}
```

**使用：**
```kotlin
private var _binding: FragmentUserBinding? = null
private val binding get() = _binding!!

override fun onCreateView(...): View {
    _binding = FragmentUserBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // 避免内存泄漏
}
```

## 四、数据流详解

### 单向数据流

```
用户点击按钮
    ↓
View 调用 ViewModel 方法
    ↓
ViewModel 调用 Repository
    ↓
Repository 获取数据
    ↓
ViewModel 更新 LiveData
    ↓
View 观察到变化，自动更新 UI
```

### 示例流程

1. **用户操作**：用户点击刷新按钮
2. **View 响应**：`viewModel.refreshUsers()`
3. **ViewModel 处理**：启动协程，调用 Repository
4. **Repository 获取**：从网络或数据库获取数据
5. **ViewModel 更新**：`_users.value = data`
6. **View 自动更新**：LiveData 观察者收到通知，更新 RecyclerView

## 五、最佳实践

### 1. ViewModel 设计

```kotlin
class UserViewModel : ViewModel() {
    // ✅ 使用私有 MutableLiveData
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    // ✅ 使用 viewModelScope
    fun loadUsers() {
        viewModelScope.launch {
            // 异步操作
        }
    }
    
    // ❌ 不要持有 Context
    // private val context: Context
}
```

### 2. Repository 设计

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val database: UserDao
) {
    // ✅ 提供挂起函数
    suspend fun getUsers(): List<User> {
        return try {
            // 先尝试网络
            apiService.getUsers()
        } catch (e: Exception) {
            // 失败时从数据库获取
            database.getUsers()
        }
    }
}
```

### 3. View 设计

```kotlin
class UserFragment : Fragment() {
    // ✅ 使用 by viewModels() 委托
    private val viewModel: UserViewModel by viewModels()
    
    // ✅ 使用 viewLifecycleOwner
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            // 更新 UI
        }
    }
    
    // ❌ 不要在 View 中处理业务逻辑
}
```

### 4. 错误处理

```kotlin
// ViewModel
private val _error = MutableLiveData<String?>()
val error: LiveData<String?> = _error

fun loadUsers() {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            _users.value = repository.getUsers()
            _error.value = null
        } catch (e: Exception) {
            _error.value = "加载失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

## 六、常见问题

### Q1: ViewModel 和 Repository 的区别？

- **ViewModel**: 持有 UI 相关的数据，处理 UI 逻辑
- **Repository**: 负责数据获取，决定数据来源

### Q2: 为什么要区分 MutableStateFlow 和 StateFlow？

- **MutableStateFlow**: 可以修改值，只在 ViewModel 内部使用
- **StateFlow**: 只读，暴露给 View，防止 View 直接修改数据

### Q3: 什么时候使用 viewModelScope？

所有在 ViewModel 中的异步操作都应该使用 viewModelScope，它会在 ViewModel 被清除时自动取消。

### Q4: Fragment 还是 Activity？

推荐使用 Fragment，因为：
- 更灵活的 UI 组件
- 更好的生命周期管理
- 便于复用和导航

### Q5: 为什么使用 StateFlow 而不是 LiveData？

StateFlow 的优势：
- 更强大的操作符（map, filter, combine 等）
- 更好的协程集成
- 类型安全，编译时检查
- 更现代的 API 设计
- 支持冷流和热流的转换

## 七、进阶主题

### 1. StateFlow - 现代化的状态管理

StateFlow 是 Kotlin Flow 的一部分，是 LiveData 的现代替代品。

**优势：**
- 更强大的操作符支持
- 更好的协程集成
- 类型安全
- 支持冷流和热流

**使用示例：**
```kotlin
// ViewModel
private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

// Fragment 中收集
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            when (state) {
                is UserUiState.Loading -> showLoading()
                is UserUiState.Success -> showSuccess(state.users)
                is UserUiState.Error -> showError(state.message)
                is UserUiState.Empty -> showEmpty()
            }
        }
    }
}
```

**重要：** 使用 `repeatOnLifecycle` 确保只在生命周期活跃时收集数据，避免内存泄漏。

### 2. 依赖注入 - Hilt

Hilt 是 Google 推荐的依赖注入框架，基于 Dagger。

**ViewModel 注入：**
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel()
```

**Repository 注入：**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl()
    }
}
```

**Application 配置：**
```kotlin
@HiltAndroidApp
class MvvmApplication : Application()
```

### 3. KSP 替代 kapt

KSP (Kotlin Symbol Processing) 是 Google 推荐的注解处理方案。

**优势：**
- 编译速度提升 2 倍以上
- 更低的内存占用
- 更好的 Kotlin 支持

**配置：**
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
}

dependencies {
    ksp("com.google.dagger:hilt-compiler:2.51.1")
}
```

### 4. Version Catalog

使用 Gradle Version Catalog 统一管理依赖版本。

**gradle/libs.versions.toml：**
```toml
[versions]
kotlin = "1.9.25"
hilt = "2.51.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

**使用：**
```kotlin
dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

## 八、学习路径

1. ✅ 理解 MVVM 基本概念
2. ✅ 掌握 ViewModel 和 Repository
3. ✅ 学习协程和 viewModelScope
4. ✅ 学习 StateFlow 和 Kotlin Flow
5. ✅ 掌握依赖注入（Hilt）
6. ✅ 理解 KSP 和 Version Catalog
7. ⬜ 学习 Room 数据库
8. ⬜ 学习 Retrofit 网络请求
9. ⬜ 掌握单元测试
10. ⬜ 学习 Jetpack Compose（未来趋势）

## 九、常见陷阱和解决方案

### 1. 重复加载问题

**问题：** ViewModel 在 `init` 中自动加载数据，如果多次创建可能导致重复请求。

**解决方案：**
```kotlin
private var isLoading = false

fun loadUsers() {
    if (isLoading) return
    isLoading = true
    
    viewModelScope.launch {
        try {
            // 加载逻辑
        } finally {
            isLoading = false
        }
    }
}
```

### 2. 缓存数据导致 UI 闪烁

**问题：** Repository 先发送缓存数据，再发送网络数据，导致 UI 快速更新两次。

**解决方案：**
```kotlin
override fun getUsers(): Flow<Result<List<User>>> = flow {
    cachedUsers?.let { emit(Result.success(it)) }
    
    val users = fetchUsersFromNetwork()
    
    // 只有数据变化时才发送
    if (cachedUsers != users) {
        cachedUsers = users
        emit(Result.success(users))
    }
}
```

### 3. Fragment 生命周期问题

**问题：** 使用 `lifecycleScope` 而不是 `viewLifecycleOwner.lifecycleScope` 可能导致内存泄漏。

**解决方案：**
```kotlin
// ✅ 正确 - 使用 repeatOnLifecycle
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            handleUiState(state)
        }
    }
}

// ❌ 错误 - 可能导致内存泄漏
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        handleUiState(state)
    }
}
```

**为什么要使用 repeatOnLifecycle？**
- 确保只在生命周期活跃时收集数据
- Fragment 在后台时自动停止收集
- 返回前台时自动恢复收集
- 避免在 Fragment 销毁后更新 UI

### 4. 错误处理不够细致

**问题：** 所有错误都显示相同的消息，用户体验不好。

**解决方案：**
```kotlin
sealed class UserUiState {
    object Loading : UserUiState()
    object Empty : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.UNKNOWN
    ) : UserUiState()
}

enum class ErrorType {
    NETWORK,    // 网络错误
    SERVER,     // 服务器错误
    DATABASE,   // 数据库错误
    UNKNOWN     // 未知错误
}
```

### 5. ViewBinding 内存泄漏

**问题：** 忘记在 `onDestroyView` 中清空 binding。

**解决方案：**
```kotlin
private var _binding: FragmentUserBinding? = null
private val binding get() = _binding!!

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // 必须清空
}
```

## 十、性能优化建议

### 1. 使用 DiffUtil

RecyclerView 使用 `ListAdapter` 自动处理差异计算，提高性能。

### 2. 避免在主线程执行耗时操作

所有数据处理都在协程中执行，使用 `Dispatchers.IO` 处理 IO 操作。

### 3. 合理使用缓存

Repository 层实现缓存策略，减少网络请求。

### 4. 懒加载

只在需要时加载数据，避免不必要的资源消耗。

## 十一、项目实践总结

### 本项目的技术选型

1. **StateFlow 替代 LiveData** - 更现代的响应式方案
2. **KSP 替代 kapt** - 更快的编译速度
3. **Hilt 依赖注入** - 自动化的依赖管理
4. **Version Catalog** - 统一的依赖版本管理
5. **密封类状态管理** - 类型安全的 UI 状态
6. **Repository 模式** - 清晰的数据层抽象

### 编译和运行

**环境要求：**
- JDK 11 或更高版本（推荐 JDK 21）
- Android Studio Hedgehog 或更高版本
- Android SDK 34

**编译命令：**
```bash
# Windows
.\gradlew.bat build

# 编译 Debug APK
.\gradlew.bat assembleDebug
```

**安装到设备：**
```bash
# 启动模拟器
emulator -avd Pixel_9_Pro_XL

# 安装 APK
adb install app\build\outputs\apk\debug\app-debug.apk

# 启动应用
adb shell am start -n com.example.mvvmdemo/.MainActivity
```

## 十二、参考资源

- [Android 官方架构指南](https://developer.android.com/topic/architecture)
- [ViewModel 概览](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow 和 SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Kotlin 协程](https://developer.android.com/kotlin/coroutines)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [KSP 文档](https://kotlinlang.org/docs/ksp-overview.html)
- [Version Catalog](https://docs.gradle.org/current/userguide/platforms.html)
- [Kotlin Flow 官方文档](https://kotlinlang.org/docs/flow.html)
