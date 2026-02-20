# Android MVVM 架构示例

这是一个使用 Kotlin 和 Google 最新推荐的 MVVM 架构模式的示例项目。

## 技术栈

- **Kotlin 1.9.25** - 现代化的 Android 开发语言
- **MVVM 架构** - 清晰的架构分层
- **StateFlow** - 替代 LiveData 的现代响应式方案
- **Hilt 2.51.1** - 依赖注入框架
- **Kotlin Coroutines 1.9.0** - 异步编程
- **KSP** - 替代 kapt 的注解处理器，编译更快
- **ViewBinding** - 类型安全的视图绑定
- **Version Catalog** - 统一的依赖管理
- **Repository 模式** - 数据层抽象

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

使用现代的 Kotlin Flow API，提供更强大的数据流操作和更好的协程集成。

```kotlin
private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
```

### 2. Hilt 依赖注入

自动管理依赖，减少样板代码，提高可测试性。

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel()
```

### 3. 密封类表示 UI 状态

类型安全的状态管理，编译时检查所有分支。

```kotlin
sealed class UserUiState {
    object Loading : UserUiState()
    object Empty : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    data class Error(val message: String) : UserUiState()
}
```

### 4. Repository 模式

依赖倒置原则，便于测试和扩展。

```kotlin
interface UserRepository {
    fun getUsers(): Flow<Result<List<User>>>
    suspend fun refreshUsers(): Result<List<User>>
}
```

### 5. KSP 替代 kapt

使用 Kotlin Symbol Processing (KSP) 替代 kapt，编译速度提升 2 倍以上。

### 6. Version Catalog

使用 Gradle Version Catalog 统一管理依赖版本，避免版本冲突。

## 依赖管理

项目使用 **Version Catalog** 统一管理依赖版本，配置文件位于 `gradle/libs.versions.toml`。

### 主要依赖版本

```toml
[versions]
kotlin = "1.9.25"
agp = "8.2.2"
ksp = "1.9.25-1.0.20"
hilt = "2.51.1"
lifecycle = "2.8.7"
coroutines = "1.9.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### 使用 KSP 替代 kapt

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)  // 使用 KSP
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)  // 使用 ksp 而不是 kapt
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

## 编译和运行

### 环境要求

- JDK 11 或更高版本（推荐 JDK 21）
- Android Studio Hedgehog 或更高版本
- Android SDK 34

### 编译项目

```bash
# Windows
.\gradlew.bat build

# 编译 Debug APK
.\gradlew.bat assembleDebug

# 编译 Release APK
.\gradlew.bat assembleRelease
```

### 安装到设备

```bash
# 安装 Debug 版本
adb install app\build\outputs\apk\debug\app-debug.apk

# 启动应用
adb shell am start -n com.example.mvvmdemo/.MainActivity
```

## 常见问题

### Q1: 为什么使用 StateFlow 而不是 LiveData？

StateFlow 是 Kotlin Flow 的一部分，提供：
- 更强大的操作符（map, filter, combine 等）
- 更好的协程集成
- 类型安全，编译时检查
- 更现代的 API 设计

### Q2: 为什么使用 KSP 而不是 kapt？

KSP (Kotlin Symbol Processing) 相比 kapt：
- 编译速度提升 2 倍以上
- 更好的 Kotlin 支持
- 更低的内存占用
- Google 官方推荐的注解处理方案

### Q3: 如何添加网络请求？

1. 在 `libs.versions.toml` 中添加 Retrofit 依赖
2. 创建 ApiService 接口
3. 在 AppModule 中提供 Retrofit 实例
4. 在 Repository 中注入 ApiService

### Q4: 如何添加数据库支持？

1. 添加 Room 依赖
2. 创建 Entity、DAO 和 Database
3. 在 Repository 中实现缓存逻辑

## 项目文档

- [README.md](./README.md) - 项目概述和快速入门
- [MVVM架构学习指南.md](./MVVM架构学习指南.md) - 详细的 MVVM 架构学习文档

## 学习资源

- [Android 官方架构指南](https://developer.android.com/topic/architecture)
- [ViewModel 概览](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow 和 SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Flow 官方文档](https://kotlinlang.org/docs/flow.html)
- [DataBinding 官方文档](https://developer.android.com/topic/libraries/data-binding)
