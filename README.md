# To-Do List Android App

A modern, offline-first To-Do List Android application built with Java and Android Jetpack libraries. Features MVVM architecture, Room database, Hilt dependency injection, and Material Design 3.

## � Download

**Repository**: [https://github.com/Sachink122/To-Do-for-android](https://github.com/Sachink122/To-Do-for-android)

Get the latest release from the [Releases](https://github.com/Sachink122/To-Do-for-android/releases) page.

## �📱 Features

### Core Features
- ✅ **Task Management** - Create, edit, delete, and complete tasks
- 📋 **Subtasks** - Break down tasks into smaller actionable items
- 🏷️ **Categories** - Organize tasks with custom color-coded categories
- ⭐ **Important Tasks** - Star tasks to mark them as important
- 🎯 **Priority Levels** - Set High, Medium, Low, or No priority

### Reminders & Scheduling
- ⏰ **Reminders** - Set date and time reminders for tasks
- 🔄 **Repeating Tasks** - Daily, Weekly, Monthly, or Yearly repeats
- 📅 **Calendar View** - View tasks by date in calendar format
- 📊 **Daily Summary** - Optional daily notification with pending tasks

### User Experience
- 🌓 **Dark/Light Theme** - System-following or manual theme selection
- 📲 **Home Screen Widget** - Quick access to pending tasks
- 🔍 **Search & Filter** - Find tasks quickly with search and filters
- ↔️ **Swipe Actions** - Swipe to complete or delete tasks
- 📴 **Offline-First** - Works without internet connection

### Data Management
- 💾 **Local Database** - Room database with migrations
- 📤 **Export/Import** - Backup data to JSON files
- 🔄 **Auto-Sync** - Widget updates automatically

## 🏗️ Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture with Repository pattern:

```
┌─────────────────────────────────────────────────────────┐
│                         UI Layer                         │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐ │
│  │   Activities  │  │   Fragments   │  │   Adapters   │ │
│  └───────┬───────┘  └───────┬───────┘  └──────────────┘ │
│          │                  │                            │
│          ▼                  ▼                            │
│  ┌─────────────────────────────────────────────────────┐│
│  │                    ViewModels                        ││
│  │  (TaskListVM, AddEditTaskVM, TaskDetailVM, etc.)    ││
│  └───────────────────────────┬─────────────────────────┘│
└──────────────────────────────┼──────────────────────────┘
                               │
┌──────────────────────────────┼──────────────────────────┐
│                         Data Layer                       │
│                              ▼                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │                   TaskRepository                     ││
│  └───────────────────────────┬─────────────────────────┘│
│                              │                           │
│  ┌───────────────────────────┴─────────────────────────┐│
│  │                    Room Database                     ││
│  │  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  ││
│  │  │  TaskDao │  │SubtaskDao│  │   CategoryDao     │  ││
│  │  └──────────┘  └──────────┘  └───────────────────┘  ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 8+ |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | MVVM + Repository |
| **DI** | Hilt 2.48 |
| **Database** | Room 2.6.0 |
| **Async** | LiveData, Executors |
| **Navigation** | Navigation Component 2.7.5 |
| **Background Work** | WorkManager 2.9.0 |
| **UI** | Material Components 1.10.0 |
| **View Binding** | Enabled |

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/todoapp/
│   │   ├── data/
│   │   │   ├── db/          # Database setup
│   │   │   ├── dao/         # Data Access Objects
│   │   │   ├── model/       # Entity classes
│   │   │   └── repository/  # Repository classes
│   │   ├── di/              # Hilt modules
│   │   ├── notification/    # Notification & reminders
│   │   ├── ui/
│   │   │   ├── adapters/    # RecyclerView adapters
│   │   │   ├── calendar/    # Calendar fragment
│   │   │   ├── categories/  # Categories fragment
│   │   │   ├── settings/    # Settings fragment
│   │   │   └── tasks/       # Task list, add/edit, detail
│   │   ├── util/            # Utility classes
│   │   ├── viewmodel/       # ViewModels
│   │   ├── widget/          # Home screen widget
│   │   └── TodoApplication.java
│   └── res/
│       ├── layout/          # XML layouts
│       ├── menu/            # Menu resources
│       ├── navigation/      # Navigation graph
│       ├── drawable/        # Icons and shapes
│       ├── values/          # Strings, colors, themes
│       └── xml/             # Widget info
└── src/test/                # Unit tests
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/todo-android.git
   cd todo-android
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory

3. **Sync Gradle**
   - Android Studio will automatically sync dependencies
   - Or manually: File → Sync Project with Gradle Files

4. **Run the app**
   - Select an emulator or connected device
   - Click Run (▶️) or press Shift+F10

### Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Development build with debugging enabled |
| `release` | Production build with ProGuard minification |

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test
```

## 📖 Usage Guide

### Creating a Task
1. Tap the **+** FAB button
2. Enter task title (required)
3. Optionally add:
   - Description
   - Due date and time
   - Priority level
   - Category
   - Reminder
   - Repeat schedule
   - Subtasks
   - Notes
4. Tap **Save**

### Managing Tasks
- **Complete**: Tap checkbox or swipe right
- **Delete**: Swipe left
- **Edit**: Tap task to view, then tap Edit
- **Star**: Tap star icon to mark important

### Filtering Tasks
- **All**: Show all tasks
- **Today**: Tasks due today
- **Week**: Tasks due this week
- **Important**: Starred tasks
- **Completed**: Finished tasks

### Using Categories
1. Go to Categories tab
2. Tap **+** to add category
3. Enter name and select color
4. Assign categories to tasks

### Setting Reminders
1. Edit a task
2. Enable "Set Reminder" switch
3. Pick date and time
4. Optionally enable repeat

### Widget Setup
1. Long press on home screen
2. Select Widgets
3. Find "Todo Tasks" widget
4. Drag to home screen

## 🚀 Building & Deployment

### Development Build

```bash
# Clone the repository
git clone https://github.com/Sachink122/To-Do-for-android.git
cd To-Do-for-android

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Production Build

**For detailed deployment instructions, see [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)**

#### Quick Build Commands

```powershell
# Build release APK (for testing)
./build-release.ps1

# Build release AAB (for Play Store)
./build-aab.ps1
```

#### Manual Build

```bash
# Clean project
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Build release AAB (recommended for Play Store)
./gradlew bundleRelease
```

**Output locations:**
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

### Deployment Checklist

Before deploying to production:
- ✅ Release signing configured
- ✅ ProGuard rules optimized
- ✅ Generate keystore for signing
- ✅ Update version code/name
- ✅ Add SHA-1 to Firebase Console
- ✅ Test release build thoroughly
- ✅ Upload to Play Console

See [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) for the complete checklist.

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

Tests cover:
- Entity classes (Task, Category, Subtask)
- ViewModels with mocked repositories
- Utility functions (DateUtils)

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

Tests cover:
- Database operations
- DAO queries
- Repository integration

## 🔧 Configuration

### Notification Channels
The app creates two notification channels:
- **Task Reminders**: Individual task notifications
- **Daily Summary**: Morning task overview

### Preferences
Managed via `PreferencesManager`:
- Theme mode (system/light/dark)
- Default priority
- Default reminder setting
- Daily summary enabled
- Reminders enabled

## 📱 Screenshots

| Task List | Add Task | Calendar | Settings |
|-----------|----------|----------|----------|
| *Main screen with filters* | *Task creation form* | *Calendar view* | *App settings* |

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Material Design](https://material.io/) for design guidelines
- [Android Jetpack](https://developer.android.com/jetpack) for architecture components
- [Google Fonts](https://fonts.google.com/) for typography
- All open-source contributors

## 📞 Support

If you have questions or run into issues:
- Open a [GitHub Issue](https://github.com/Sachink122/To-Do-for-android/issues)
- Check existing issues for solutions
- Read the [Deployment Guide](DEPLOYMENT_GUIDE.md) for setup help

## 🔗 Links

- **Repository**: [https://github.com/Sachink122/To-Do-for-android](https://github.com/Sachink122/To-Do-for-android)
- **Issues**: [Report a bug or request a feature](https://github.com/Sachink122/To-Do-for-android/issues)
- **Releases**: [Download latest version](https://github.com/Sachink122/To-Do-for-android/releases)

---

**Made with ❤️ for productive people**
