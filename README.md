# WhisperGames 🎮🎤

An innovative Android game application that uses microphone input to create unique gameplay experiences. Control games through whispers, claps, and sound levels in this collection of audio-based mini-games.

## 🎯 Features

- **4 Unique Audio-Based Games**:
  - **Whisper Line**: Navigate by controlling your voice volume
  - **Dead Silence**: Stay completely silent to win
  - **Blow Balloon**: Blow into the mic to inflate a balloon
  - **Clap Catch**: Catch objects by clapping

- **Real-time Audio Processing**: Advanced microphone volume detection and processing
- **Leaderboard System**: Track high scores across all games
- **Sound Effects & Haptic Feedback**: Immersive gaming experience with audio and vibration
- **Microphone Test Tool**: Built-in mic calibration for optimal performance
- **Smooth Animations**: Polished UI with custom transitions and visual effects
- **Settings**: Customize sound effects and vibration preferences

## 🛠️ Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Android Views with custom animations
- **Audio Processing**: Android AudioRecord API
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL

## 📋 Prerequisites

- Android Studio Otter | 2025.2.1 or later
- JDK 8 or higher
- Android SDK
- Physical Android device with microphone (recommended)

## 🚀 Setup

1. **Clone the repository**:
```bash
git clone https://github.com/SHRESHTHBEHAL/WhisperGames.git
cd WhisperGames
```

2. **Open the project in Android Studio**

3. **Sync Gradle files**

4. **Build and run**:
```bash
./gradlew installDebug
```

Or use the Run button in Android Studio.

## 🔑 Permissions

The app requires the following permissions:

- **RECORD_AUDIO**: For microphone input detection
- **MODIFY_AUDIO_SETTINGS**: For audio processing optimization
- **VIBRATE**: For haptic feedback

All permissions are requested at runtime with proper explanations.

## 📁 Project Structure

```
WhisperGames/
├── app/
│   └── src/
│       └── main/
│           ├── kotlin/com/whispergames/app/
│           │   ├── audio/
│           │   │   ├── MicVolumeEngine.kt
│           │   │   └── SoundManager.kt
│           │   ├── data/
│           │   │   └── ScoreManager.kt
│           │   ├── ui/
│           │   │   ├── SplashActivity.kt
│           │   │   ├── HomeActivity.kt
│           │   │   ├── GameActivity.kt
│           │   │   ├── GameOverActivity.kt
│           │   │   ├── InstructionsActivity.kt
│           │   │   ├── SettingsActivity.kt
│           │   │   └── MicTestActivity.kt
│           │   ├── utils/
│           │   │   ├── PermissionHelper.kt
│           │   │   ├── SettingsManager.kt
│           │   │   └── VibrationHelper.kt
│           │   ├── ads/
│           │   │   └── AdManager.kt
│           │   └── WhisperGamesApp.kt
│           └── res/
│               ├── anim/
│               ├── drawable/
│               ├── layout/
│               └── values/
├── build.gradle.kts
└── README.md
```

## 🎮 How to Play

1. **Launch the app** and grant microphone permissions
2. **Select a game** from the home screen
3. **Read the instructions** for each game
4. **Test your microphone** if needed
5. **Play and compete** for high scores!

### Game Tips

- **Whisper Line**: Find the sweet spot between too quiet and too loud
- **Dead Silence**: Cover the mic or stay in a quiet room
- **Blow Balloon**: Blow steadily to avoid popping the balloon
- **Clap Catch**: Time your claps to catch falling objects

## 🔧 Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
```

## 🐛 Known Issues

- Emulator testing is limited due to microphone input requirements
- Some devices may require microphone sensitivity calibration
- Background noise can affect gameplay (use mic test to check)


## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Shreshth Behal**
- GitHub: [@SHRESHTHBEHAL](https://github.com/SHRESHTHBEHAL)

---

**Note**: For the best experience, use a physical Android device with a working microphone. Emulators have limited microphone support and may not provide the full gameplay experience.

Made with ❤️ by Shreshth Behal for Google Admob x IIT Bombay Hackathon

