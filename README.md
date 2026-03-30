# CodeLearn IDE - Compose Multiplatform

A professional multi-language coding IDE built with Kotlin Multiplatform (KMP) and Compose Multiplatform for learning purposes.

## Features
- Multi-language support: Kotlin, Java, C#, C++, C, Ruby, Dart, VB
- Syntax highlighting with language-specific color themes
- Autocomplete with IntelliSense-style suggestions
- Debugging panel with breakpoints
- Multi-file project management
- Git integration
- AI hints system
- LeetCode-style quiz system
- Fully offline

## Project Structure
```
CodeLearnIDE/
├── composeApp/           # Shared UI + logic (KMP)
│   └── src/
│       ├── commonMain/   # Shared Kotlin code
│       ├── androidMain/  # Android-specific
│       └── desktopMain/  # Desktop-specific
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```
