# Program To Artist Linking

This note explains how artist links inside the program detail page are wired in the Android app.

## Goal

When a user opens a program detail page and taps a person card, the app should navigate to that person's artist page without affecting playback or tab state.

The linked roles on the page are:

- singers
- orchestras
- orchestra leaders
- musicians / performers
- poets
- composers
- arrangers
- announcers

## Data Flow

1. `loadProgramEpisodeDetail(...)` loads program JSON from the Rust bridge.
2. The JSON arrays for each credit role are parsed into UI models that carry `artistId`.
3. `ProgramEpisodeDetailScreen(...)` receives `onArtistClick: (Long) -> Unit`.
4. Each `ArtistCarousel` item calls `artist.artistId?.let(onArtistClick)`.
5. On Android, `AndroidApp.kt` maps that callback to:

```kotlin
navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId))
```

## Relevant Files

- `/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/androidMain/kotlin/com/radiogolha/mobile/AndroidApp.kt`
- `/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/commonMain/kotlin/com/radiogolha/mobile/ui/programs/ProgramEpisodeDetailScreen.kt`
- `/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/src/androidMain/kotlin/com/radiogolha/mobile/ui/programs/ProgramsDataLoader.android.kt`

## Important Detail

The UI only navigates when an `artistId` is present:

```kotlin
onClick = { artist.artistId?.let { onArtistClick(it) } }
```

So if a role renders visually but does not navigate, the first thing to check is whether the parsed JSON for that role includes `artist_id` or another mappable ID field.

## Why This Does Not Affect Playback

Playback state is owned at the app root by the shared player manager, not inside the program detail page. That means moving from program detail to artist detail changes navigation only and does not reset the current track.
