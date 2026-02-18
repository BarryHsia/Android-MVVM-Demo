# Android MVVM 架构示例（现代化版本）

这是一个使用 Kotlin 和 Google 最新推荐的 MVVM 架构模式的示例项目。

## 技术栈

- Kotlin
- MVVM 架构
- StateFlow（替代 LiveData）
- Hilt 依赖注入
- Kotlin Coroutines
- ViewBinding / DataBinding（提供两种实现）
- Repository 模式

## 项目结构

```
app/src/main/java/com/example/mvvmdemo/
├── data/
│   ├── model/              # 数据模型
│   └── repository/         # 数据仓库层
│       ├── UserRepository.kt       # Repository 接口
│       └── UserRepositoryImpl.kt   # Repository 实现
├── di/
│   └── AppModule.kt        # Hilt 依赖注入模块
├── ui/
│   └── user/              # UI 层（View + ViewModel）
│       ├── UserFragment.kt
│       ├── UserViewModel.kt
│       ├── UserAdapter.kt
│       └── UserUiState.kt
├── MvvmApplication.kt     # Application 类
└── MainActivity.kt        # 主活动
```

## MVVM 架构说明

### 什么是 MVVM？

MVVM（Model-View-ViewModel）是一种软件架构模式，将应用分为三个核心组件：

1. **Model（模型）**: 负责数据和业务逻辑
2. **View（视图）**: 负责 UI 显示
3. **ViewModel（视图模型）**: 连接 View 和 Model，处理 UI 逻辑

### 架构优势

- 关注点分离：UI 和业务逻辑解耦
- 易于测试：ViewModel 可以独立测试
- 生命周期感知：ViewModel 在配置更改时保留数据
- 响应式编程：使用 LiveData/StateFlow 自动更新 UI

## 核心组件说明

### 1. Model（数据模型）
- `User.kt`: 定义用户数据结构

### 2. Repository（仓库层）
- `UserRepository.kt`: 负责数据获取，可以从网络、数据库或本地获取
- 作为 ViewModel 和数据源之间的中介

### 3. ViewModel（视图模型）
- `UserViewModel.kt`: 
  - 持有和管理 UI 相关的数据
  - 使用 LiveData 暴露数据给 View
  - 处理用户交互逻辑
  - 在配置更改（如屏幕旋转）时保留数据

### 4. View（视图）
- `UserFragment.kt`: 
  - 观察 ViewModel 中的 LiveData
  - 显示数据
  - 将用户操作传递给 ViewModel

## 核心特性

### 1. StateFlow 替代 LiveData

使用现代的 Kotlin Flow API，提供更强大的数据流操作。

```kotlin
private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
```

### 2. Hilt 依赖注入

自动管理依赖，减少样板代码。

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel()
```

### 3. 密封类表示 UI 状态

类型安全的状态管理。

```kotlin
sealed class UserUiState {
    object Loading : UserUiState()
    object Empty : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(val message: String) : UserUiState()
}
```

### 4. Repository 接口

依赖倒置原则，便于测试和扩展。

```kotlin
interface UserRepository {
    fun getUsers(): Flow<Result<List<User>>>
}
```

### 5. ViewBinding 和 DataBinding 双实现

项目同时提供 ViewBinding 和 DataBinding 两种实现方式：

**ViewBinding（推荐用于简单场景）:**
- 轻量级，编译快
- 类型安全
- 适合简单 UI

**DataBinding（推荐用于 MVVM）:**
- 真正的数据驱动 UI
- XML 中直接绑定 ViewModel
- 支持双向绑定
- 减少样板代码

详细对比请查看 [DataBinding使用说明.md](./DataBinding使用说明.md)

## 依赖项

### Project build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}
```

### App build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

dependencies {
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Lifecycle runtime
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

## 数据流

```
用户操作 → View → ViewModel → Repository → Model
                    ↓
                LiveData
                    ↓
                  View (自动更新)
```

## 使用示例

### 1. Fragment 收集 StateFlow

```kotlin
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

### 2. ViewModel 使用 Hilt 注入

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    // ViewModel 代码
}
```

### 3. Repository 返回 Flow

```kotlin
override fun getUsers(): Flow<Result<List<User>>> = flow {
    try {
        val users = fetchUsersFromNetwork()
        emit(Result.success(users))
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}
```

## 最佳实践

1. ViewModel 不应持有 View、Activity 或 Context 的引用
2. 使用 StateFlow 暴露数据（现代化方式）
3. 使用 Hilt 进行依赖注入
4. Repository 使用接口，便于测试和扩展
5. 使用密封类表示 UI 状态
6. 使用 repeatOnLifecycle 收集 Flow，确保生命周期安全
7. 使用 Result 类型处理成功和失败情况
8. 根据场景选择 ViewBinding 或 DataBinding

## 重构说明

本项目已从 LiveData 升级到 StateFlow，并引入了 Hilt 依赖注入。详细的重构说明请查看 [重构说明.md](./重构说明.md)。

## 注意事项和最佳实践

### 1. 防止重复加载

在 ViewModel 中添加加载标志，防止并发请求：

```kotlin
private var isLoading = false

fun loadUsers() {
    if (isLoading) return
    isLoading = true
    // ... 加载逻辑
}
```

### 2. 缓存策略

Repository 中的缓存数据应该与新数据对比，避免不必要的 UI 更新：

```kotlin
// 只有数据真正变化时才发送
if (cachedUsers != users) {
    emit(Result.success(users))
}
```

### 3. 错误类型区分

建议区分不同类型的错误（网络错误、服务器错误等），提供更好的用户体验。

### 4. 单元测试

项目结构支持单元测试，建议为 ViewModel 和 Repository 添加测试用例。

## 常见问题

### Q1: 为什么使用 StateFlow 而不是 LiveData？

StateFlow 是 Kotlin Flow 的一部分，提供更强大的操作符和更好的协程集成。

### Q2: ViewBinding 和 DataBinding 应该选择哪个？

- **ViewBinding**: 适合简单项目，编译快，学习成本低
- **DataBinding**: 适合 MVVM 项目，真正的数据驱动 UI，支持双向绑定

详细对比请查看 [DataBinding使用说明.md](./DataBinding使用说明.md)

### Q3: 如何添加网络请求？

取消注释 Retrofit 依赖，在 Repository 中注入 ApiService，替换模拟数据。

### Q4: 如何添加数据库支持？

添加 Room 依赖，创建 DAO 和 Database，在 Repository 中实现缓存逻辑。

## 项目文档

- [README.md](./README.md) - 项目概述和快速入门
- [MVVM架构学习指南.md](./MVVM架构学习指南.md) - 详细的 MVVM 架构学习文档
- [重构说明.md](./重构说明.md) - 从 LiveData 到 StateFlow 的重构说明
- [DataBinding使用说明.md](./DataBinding使用说明.md) - DataBinding vs ViewBinding 对比

## 学习资源

- [Android 官方架构指南](https://developer.android.com/topic/architecture)
- [ViewModel 概览](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow 和 SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Flow 官方文档](https://kotlinlang.org/docs/flow.html)
- [DataBinding 官方文档](https://developer.android.com/topic/libraries/data-binding)
