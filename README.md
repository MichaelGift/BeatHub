# BeatHub

An offline music player for Android that plays the audio files stored on your device.

## Features

- Library scanned from MediaStore, with search and *All / Playlists / Favorites / Recently played* filters
- Playlists: create, rename, delete, add/remove songs, play or shuffle
- Embedded cover art in the app, the media notification and the lock screen
- Play next / add to queue from any song
- Three swipeable pages: **Queue · Now playing · Library**, plus a mini-player
- Background playback with a media notification, lock-screen and Bluetooth controls, and audio focus handling
- Shuffle, repeat (all / one), seeking, queue jumping and removal
- Favorites and play history that survive rescans
- Portrait and landscape layouts; light and dark themes

## Design

Minimal and monochrome: neutral greys, solid ink/paper controls, and type doing the hierarchy.
The only color comes from album art. Palette extracts it to tint the Now playing backdrop,
the cover's shadow and the mini-player's progress line, so each track gets its own mood.

- Custom slim seek bar (tap or drag, accessible via semantics) instead of the stock slider
- Artwork eases back when paused; play/pause morphs between states
- Animated equalizer bars mark the playing track in every list
- Played tracks fade in the queue so it reads as a timeline
- Light and dark themes, adaptive and themed launcher icon

## Tech stack

| Area | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 |
| Playback | Media3 ExoPlayer + `MediaSessionService` |
| Persistence | Room (exported schemas in `app/schemas`) |
| DI | Hilt |
| Images | Coil, AndroidX Palette |
| Build | Kotlin 2.1, AGP 8.9, Gradle version catalog |

## Architecture

```
data/       MediaStoreScanner → SongRepository ← Room (SongDao, PlaylistDao)
playback/   PlaybackService (owns ExoPlayer + MediaSession)
            PlaybackConnection (MediaController → StateFlow for the UI)
ui/         Compose screens + ViewModels (Library, Player, Queue)
```

Songs are keyed by their MediaStore id, so rescans update rows in place; `mergeScan` keeps user
state (favorites, history) while taking fresh metadata from the device.

## Building

Requires JDK 17 or 21 and the Android SDK (compileSdk 35).

```bash
./gradlew assembleDebug testDebugUnitTest
```

## Roadmap

- README screenshots
- Drag-to-reorder in playlists and the queue
- Practice tools: A-B looping, tempo change without pitch shift, waveform view
