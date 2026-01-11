# HabitForge

A habit tracking Android app built with Kotlin and Jetpack Compose for the Mobile Development course (II.3510).

## Features

- **Habit Tracking**: Add, edit, and manage daily/weekly habits
- **Streak Calculation**: Track consecutive completions
- **Journal**: Reflective journal entries tied to habits or days
- **Dashboard**: Progress charts and daily motivational quotes
- **Community Feed**: Share anonymized milestones with others
- **Notifications**: Habit reminders via WorkManager
- **Offline-First**: Works without internet, syncs when online
- **Multi-language**: English, French, Hindi

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM
- **Local Database**: Room
- **Remote Database**: Firebase Firestore
- **API**: Retrofit (ZenQuotes API for quotes)
- **Dependency Injection**: Hilt
- **Background Tasks**: WorkManager
- **Charts**: Vico

## Project Structure

```
app/src/main/java/com/habitforge/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room entities
│   │   └── HabitForgeDatabase.kt
│   ├── remote/
│   │   ├── api/          # Retrofit API
│   │   └── firebase/     # Firestore service
│   └── repository/       # Repositories
├── di/                   # Hilt modules
├── ui/
│   ├── navigation/       # Navigation setup
│   ├── screens/          # App screens
│   └── theme/            # Theme, colors, typography
├── util/                 # Utility classes
├── worker/               # WorkManager workers
├── HabitForgeApp.kt      # Application class
└── MainActivity.kt       # Main activity
```

## Setup

1. Clone the repository
2. Add `google-services.json` from Firebase Console to `app/` folder
3. Open in Android Studio
4. Sync Gradle and run

## Building the APK

### Debug APK
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Signed Release APK
1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Create or use existing keystore
4. Select **release** build variant
5. APK location: `app/build/outputs/apk/release/app-release.apk`

## Running Tests

### Unit Tests
```bash
./gradlew test
```

### UI Tests (requires connected device/emulator)
```bash
./gradlew connectedAndroidTest
```

## Team

- **Backend**: Data models, Room, repositories, Firebase sync, Retrofit, tests
- **Frontend**: Compose screens, navigation, animations, charts, UI state

## License

MIT License
