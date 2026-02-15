# Android MVVM 架构示例

这是一个使用 Kotlin 和 Google 推荐的 MVVM 架构模式的简单示例项目。

## 项目结构

```
app/src/main/java/com/example/mvvmdemo/
├── data/
│   ├── model/          # 数据模型
│   └── repository/     # 数据仓库层
├── ui/
│   └── user/          # UI 层（View + ViewModel）
└── MainActivity.kt     # 主活动
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

## 依赖项

在 `build.gradle.kts` 中添加以下依赖：

```kotlin
dependencies {
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // Fragment
    implementation("androidx.fragment:fragment-ktx:1.6.2")
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

1. **Fragment 观察数据**:
```kotlin
viewModel.users.observe(viewLifecycleOwner) { users ->
    // 更新 UI
}
```

2. **触发数据加载**:
```kotlin
viewModel.loadUsers()
```

3. **ViewModel 自动保留数据**: 屏幕旋转时数据不会丢失

## 最佳实践

1. ViewModel 不应持有 View、Activity 或 Context 的引用
2. 使用 LiveData 或 StateFlow 暴露数据
3. Repository 负责决定数据来源
4. 保持 ViewModel 的可测试性
5. 使用 ViewModelFactory 传递参数（如果需要）

## 学习资源

- [Android 官方架构指南](https://developer.android.com/topic/architecture)
- [ViewModel 概览](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData 概览](https://developer.android.com/topic/libraries/architecture/livedata)
