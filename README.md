# ARM Recomp Launcher

A small native Android prototype for importing NSP package files into an app-private library. The project is Java-based and currently has no third-party UI dependencies.

> **Current status:** importer/library prototype only. It is not a Nintendo Switch emulator or a game recompilation engine. It does not decrypt protected NSPs, bypass console security, extract encrypted game content, download commercial games, or launch games.

## Features in this prototype

- Android Storage Access Framework file picker for user-selected `.nsp` files
- Basic file-extension validation
- Copy imported files into the app's private `files/game-library` directory
- Display imported package names and file sizes

## Build an APK with GitHub Actions (no Android Studio required)

1. Create a new, empty repository on GitHub.
2. Upload the contents of this project folder to the repository's root (the `.github/workflows/android.yml` file must be included).
3. Open the repository's **Actions** tab.
4. Select **Android APK** and press **Run workflow**, or push a commit to `main`/`master`.
5. When the workflow finishes, open its run and download the `arm-recomp-launcher-debug-apk` artifact.
6. Extract the downloaded artifact ZIP. It contains `app-debug.apk`.

The workflow uses JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, and Android SDK 35.

## Build locally in Android Studio

Requirements: Android Studio, JDK 17, Android SDK Platform 35, and Android Build Tools 35.0.0.

1. Open this project folder in Android Studio.
2. Allow Gradle sync to finish.
3. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

You can also build from a terminal with Gradle 8.9 installed:

```bash
gradle --no-daemon assembleDebug
```

## Project structure

```text
ArmRecompLauncher/
├── .github/workflows/android.yml
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/armrecomp/launcher/MainActivity.java
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

## Roadmap

- [ ] Better package metadata inspection for supported, unencrypted inputs
- [ ] Import queue, cancellation, and clearer error reporting
- [ ] Integration with a compatible, legally usable ARM64 recompilation engine
- [ ] Native runtime and device compatibility testing

Any future engine integration must respect the engine's license and the rights attached to game files. Users must supply game data they are legally entitled to use.
