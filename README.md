# Muzify - Offline Music Player

A complete offline-first music player app for Android, built with modern Android development practices.

## Features

### 🎵 Core Features
- **Local Audio Scanning**: Automatically scans and indexes all music files on your device
- **Metadata Extraction**: Extracts title, artist, album, year, and cover art from audio files
- **Offline Playlists**: Create and manage custom playlists
- **Recently Played**: Tracks your listening history
- **Liked Songs**: Favorite tracks collection
- **Full-Screen Player**: Spotify-like player interface with dynamic gradients
- **Mini Player**: Bottom-anchored player for quick access
- **Background Playback**: Continues playing when app is in background
- **Notification Controls**: Media controls in notification panel
- **Lock Screen Controls**: Control playback from lock screen

### 🎨 UI Features
- **Material 3 Design**: Modern, beautiful UI following Material Design 3
- **Dynamic Colors**: Gradient backgrounds extracted from album artwork using Palette API
- **Tile/List View Toggle**: Switch between grid and list views
- **Smooth Animations**: Transitions between mini and full player
- **Undo Delete**: 3-second undo timer for playlist deletion

### 🎛️ Player Features
- **Playback Controls**: Play, pause, next, previous
- **Seek Controls**: 10-second skip forward/backward
- **Loop Modes**: None, One, All
- **Like/Unlike**: Quick favorite toggle
- **Add to Playlist**: Add current track to any playlist
- **Queue Management**: Play entire playlists or song collections

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room Database
- **Media Player**: ExoPlayer with MediaSession
- **Image Loading**: Coil
- **Color Extraction**: Palette API
- **Navigation**: Navigation Compose
- **Async Operations**: Coroutines + Flow

## Project Structure

```
app/
├── data/
│   ├── database/          # Room entities and DAOs
│   ├── model/             # Data models
│   ├── repository/        # Data repositories
│   └── scanner/           # MediaStore scanner
├── domain/
│   └── usecase/           # Business logic use cases
├── player/
│   └── MusicService.kt    # ExoPlayer service
├── ui/
│   ├── components/        # Reusable UI components
│   ├── navigation/        # Navigation setup
│   ├── screens/           # All app screens
│   ├── theme/             # Material theme
│   ├── util/              # UI utilities (Palette helper)
│   └── viewmodel/         # ViewModels
└── di/                    # Dependency injection modules
```

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator (API 26+)

## Permissions

The app requires the following permissions:
- `READ_MEDIA_AUDIO` (Android 13+)
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` for background playback

## Usage

1. **First Launch**: The app will automatically scan for music files
2. **Browse Library**: View all songs, liked songs, and playlists
3. **Create Playlist**: Tap the FAB in Library screen
4. **Play Music**: Tap any song to start playback
5. **Manage Playlists**: Add/remove songs, delete playlists (with undo)
6. **Rescan Media**: Go to Profile → Rescan Media

## Architecture

The app follows clean architecture principles:

- **Data Layer**: Room database, repositories, data sources
- **Domain Layer**: Use cases (business logic)
- **Presentation Layer**: ViewModels, UI (Compose)

All data flows through repositories using Kotlin Flow for reactive updates.

## License

This project is open source and available for educational purposes.

