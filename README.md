# 围棋游戏 (GoGame)

一个使用 Kotlin + Jetpack Compose + Material 3 开发的 Android 围棋游戏。

## 功能特性

- **完整围棋规则**：落子、提子、禁着点检测、劫争检测
- **多种棋盘**：9×9、13×13、19×19
- **游戏模式**：双人对弈、人机对弈（AI）
- **AI 难度**：简单、普通、困难
- **数子计分**：中国规则数子，含贴目
- **操作功能**：悔棋、虚手(Pass)、数子、新局
- **设置选项**：坐标显示、步数显示、音效、震动、贴目调整
- **Material 3 设计**：SPEC 2025 配色方案，支持动态取色（Android 12+）
- **深色模式**：完整支持深色/浅色主题

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **设计系统**：Material 3 (Material You)
- **架构**：MVVM (ViewModel + StateFlow)
- **最低 SDK**：Android 8.0 (API 26)
- **目标 SDK**：Android 14 (API 34)

## 编译运行

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 步骤

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮或使用 `./gradlew assembleDebug` 编译

### 命令行编译

```bash
# 编译 Debug APK
./gradlew assembleDebug

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
GoGame/
├── app/
│   └── src/main/
│       ├── java/com/gogame/
│       │   ├── MainActivity.kt          # 主 Activity
│       │   ├── game/
│       │   │   ├── GoBoard.kt           # 棋盘数据模型
│       │   │   ├── GoEngine.kt          # 围棋规则引擎
│       │   │   └── GoAI.kt              # AI 对手
│       │   ├── data/
│       │   │   └── GameSettings.kt      # 游戏设置
│       │   └── ui/
│       │       ├── Color.kt             # SPEC 2025 配色
│       │       ├── Theme.kt             # Material 3 主题
│       │       ├── Typography.kt        # 排版
│       │       ├── Shape.kt             # 形状
│       │       ├── GoBoard.kt           # 棋盘 Composable
│       │       ├── GameScreen.kt        # 主游戏界面
│       │       └── GameViewModel.kt     # ViewModel
│       └── res/                         # 资源文件
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/
```

## 围棋规则说明

- **落子**：黑先白后，交替落子
- **提子**：当一方棋子被完全包围（无气）时，被提走
- **禁着点**：落子后己方无气且不能提对方子的位置
- **劫争**：禁止立即提回劫的着法，需先在他处落子
- **虚手**：连续两次虚手，对局结束
- **数子**：中国规则，领地+提子数+贴目(7.5)计算胜负
