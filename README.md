# 作业花园防御战 · Android 版

微信小程序「作业打卡 × 植物大战僵尸」的安卓 App 版本。
采用 **WebView 壳 + 内置 HTML 游戏** 方案：游戏逻辑（作业↔防御联动、僵尸入侵、樱桃炸弹补救等）与小程序版一一对应，核心函数名保持一致（`finishSubject` / `checkUnfinished` / `goRepair` / `gameTick`）。

- 完全离线运行，无需网络权限
- 数据存 WebView localStorage，卸载前不丢失
- minSdk 24（Android 7.0+）

## 目录结构

```
pvz-study-android/
├── .github/workflows/build-apk.yml   # GitHub Actions 自动编译 APK
├── settings.gradle / build.gradle    # Gradle 工程配置
├── gradle.properties
└── app/
    ├── build.gradle                  # app 模块（纯 Java，无第三方重依赖）
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/jerry/pvzstudy/MainActivity.java   # WebView 主界面
        ├── assets/index.html         # ★ 游戏本体（改玩法就改这个文件）
        └── res/                      # 图标 / 主题 / 应用名
```

## 在 GitHub 上编译 APK（三步）

本机**不需要装 Android Studio / SDK / Gradle**，全部云端完成。

### 第 1 步：创建 GitHub 仓库

登录 GitHub → 右上角 `+` → New repository → 名字随意（如 `pvz-study-android`）→ **不要**勾选任何初始化文件 → Create。

### 第 2 步：推送本项目

在本目录打开终端执行（把 `<你的用户名>` 换掉）：

```bash
git remote add origin https://github.com/<你的用户名>/pvz-study-android.git
git branch -M main
git push -u origin main
```

> 首次推送会弹 GitHub 登录窗口（或要求输入 Personal Access Token）。

### 第 3 步：下载 APK

推送后 GitHub 自动开始编译（约 3~5 分钟）：

1. 打开仓库页面 → **Actions** 标签
2. 点最新一次 `Build Android APK` 运行记录
3. 页面底部 **Artifacts** → 下载 `pvz-study-apk`（zip 内含 debug + release 两个 APK）
4. 把 APK 传到手机安装（需允许「安装未知来源应用」）

### 发正式版（可选）

```bash
git tag v1.0.0
git push origin v1.0.0
```

推 tag 会自动创建 GitHub Release 并把 APK 挂在 Release 页，别人可直接下载。

## 常见修改

| 想改什么 | 改哪里 |
|---------|--------|
| 游戏玩法 / 联动规则 / 僵尸强度 | `app/src/main/assets/index.html`（内含中文注释） |
| 应用名称 | `app/src/main/res/values/strings.xml` |
| 桌面图标 | `app/src/main/res/drawable/ic_launcher.xml`（矢量，可换 PNG） |
| 包名 | `app/build.gradle` 的 `applicationId` + Java 包路径 |
| 版本号 | `app/build.gradle` 的 `versionCode` / `versionName` |
| 正式签名 | `app/build.gradle` 的 `signingConfigs`（当前 release 用 debug 签名，仅供测试） |

## 本地编译（可选）

装了 Android Studio 的话，直接 `File → Open` 本目录，点绿色 ▶ 即可真机运行；
或命令行：`gradle assembleDebug`（需 Gradle 8.7 + JDK 17）。
