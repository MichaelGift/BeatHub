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

## Releases

Pushing a version tag publishes a signed APK as a GitHub Release
([`release.yml`](.github/workflows/release.yml)):

```bash
git tag v2.0.0 && git push origin v2.0.0
```

The tag must point at a commit on `master`. Tags with a suffix (`v2.1.0-beta.1`) become pre-releases.
The version name comes from the tag; the version code from the workflow run number.

One-time setup: create an upload keystore and keep it somewhere safe (losing it means
users can't update over existing installs).

```bash
keytool -genkeypair -v -keystore beathub-release.jks -alias beathub -keyalg RSA -keysize 2048 -validity 10000
```

Then add these repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -w0 beathub-release.jks` |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | `beathub` (or the alias you chose) |
| `KEY_PASSWORD` | the key password |

Local `assembleRelease` builds still work without these; they're just unsigned.

## Roadmap

- README screenshots
- Drag-to-reorder in playlists and the queue
- Practice tools: A-B looping, tempo change without pitch shift, waveform view
