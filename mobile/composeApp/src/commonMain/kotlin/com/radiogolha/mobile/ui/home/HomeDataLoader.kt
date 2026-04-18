package com.radiogolha.mobile.ui.home

import com.radiogolha.mobile.RustCoreBridge
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

expect fun requireArchiveDbPath(): String
expect fun requireUserDbPath(): String


fun loadHomeUiState(): HomeUiState? {
    return try {
        val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
        if (payload.isBlank()) return null
        
        val response = json.decodeFromString<HomeFeedResponse>(payload)
        
        HomeUiState(
            programs = response.categories.map { ProgramUiModel(it.id, it.title, it.episodeCount) },
            singers = response.singers.map { SingerUiModel(it.id, it.name, it.avatar, it.programCount) },
            musicians = response.musicians.map { MusicianUiModel(it.id, it.name, it.instrument, it.avatar, it.programCount) },
            dastgahs = response.dastgahs.map { DastgahUiModel(it.name) },
            topTracks = response.topTracks.map { it.toTrackUiModel() },
            duets = response.duets.map { 
                DuetPairUiModel(
                    singer1 = it.singer1,
                    singer2 = it.singer2,
                    singer1Avatar = it.singer1Avatar,
                    singer2Avatar = it.singer2Avatar,
                    trackCount = it.trackCount
                )
            },
            bottomNavItems = emptyList()
        )
    } catch (e: Exception) {
        println("ERROR_GOLHA: loading home state: ${e.message ?: "unknown error"}")
        null
    }
}

fun loadTopTracks(): List<TrackUiModel> {
    return try {
        val payload = RustCoreBridge.getTopTracksJson(requireArchiveDbPath())
        if (payload.isBlank()) return emptyList()
        val tracks = json.decodeFromString<List<TrackDto>>(payload)
        tracks.map { it.toTrackUiModel() }
    } catch (e: Exception) {
        println("ERROR loadTopTracks: ${e.message}")
        emptyList()
    }
}

fun loadProgramsByIds(ids: List<Long>): List<CategoryProgramUiModel> {
    if (ids.isEmpty()) return emptyList()
    return try {
        val idsJson = ids.joinToString(prefix = "[", postfix = "]", separator = ",")
        val payload = RustCoreBridge.getProgramsByIdsJson(requireArchiveDbPath(), idsJson)
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
        response.map { it.toCategoryProgramUiModel() }
    } catch (e: Exception) {
        println("ERROR loadProgramsByIds: ${e.message}")
        emptyList()
    }
}

fun loadProgramsByMode(modeId: Long): List<CategoryProgramUiModel> {
    return try {
        val payload = RustCoreBridge.getProgramsByModeJson(requireArchiveDbPath(), modeId)
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
        response.map { it.toCategoryProgramUiModel() }
    } catch (e: Exception) {
        println("ERROR loadProgramsByMode: ${e.message}")
        emptyList()
    }
}

fun loadDuetPairsConfig(): List<DuetPairUiModel> {
    return try {
        val payload = RustCoreBridge.getDuetPairsConfigJson(requireArchiveDbPath())
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<List<DuetPairDto>>(payload)
        response.map { 
            DuetPairUiModel(
                singer1 = it.singer1,
                singer2 = it.singer2,
                singer1Avatar = it.singer1Avatar,
                singer2Avatar = it.singer2Avatar,
                trackCount = it.trackCount
            )
        }
    } catch (e: Exception) {
        println("ERROR loadDuetPairsConfig: ${e.message}")
        emptyList()
    }
}

fun loadOrderedModes(): List<String> {
    return try {
        val payload = RustCoreBridge.getOrderedModesJson(requireArchiveDbPath())
        if (payload.isBlank()) return emptyList()
        json.decodeFromString<List<String>>(payload)
    } catch (e: Exception) {
        println("ERROR loadOrderedModes: ${e.message}")
        emptyList()
    }
}


fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel> {
    return try {
        val payload = RustCoreBridge.getDuetProgramsJson(requireArchiveDbPath(), singer1, singer2)
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
        response.map { it.toCategoryProgramUiModel() }
    } catch (e: Exception) {
        println("ERROR loadDuetPrograms: ${e.message}")
        emptyList()
    }
}

fun loadRecentlyPlayedIds(limit: Long): List<Long> {
    return try {
        val payload = RustCoreBridge.getRecentlyPlayedIds(requireUserDbPath(), limit)
        if (payload.isBlank()) return emptyList()
        json.decodeFromString<List<Long>>(payload)
    } catch (e: Exception) {
        println("ERROR loadRecentlyPlayedIds: ${e.message}")
        emptyList()
    }
}

fun loadSavedPlaylists(): List<SavedPlaylistUiModel> {
    return try {
        val payload = RustCoreBridge.getManualPlaylists(requireUserDbPath())
        if (payload.isBlank()) return emptyList()
        val playlists = json.decodeFromString<List<PlaylistDto>>(payload)
        playlists.map { SavedPlaylistUiModel(it.id, it.name) }
    } catch (e: Exception) {
        println("ERROR loadSavedPlaylists: ${e.message}")
        emptyList()
    }
}

fun loadFavoriteSingers(): List<SingerListItemUiModel> {
    return try {
        val payload = RustCoreBridge.getFavoriteArtistIds(requireUserDbPath(), "artist")
        if (payload.isBlank()) return emptyList()
        val ids = json.decodeFromString<List<Long>>(payload)
        if (ids.isEmpty()) return emptyList()
        val allSingersPayload = RustCoreBridge.getSingersJson(requireArchiveDbPath())
        val singers = json.decodeFromString<List<SingerDto>>(allSingersPayload)
        singers.filter { ids.contains(it.id) }.map {
            SingerListItemUiModel(artistId = it.id, name = it.name, imageUrl = it.avatar, programCount = it.programCount)
        }
    } catch (e: Exception) {
        println("ERROR loadFavoriteSingers: ${e.message}")
        emptyList()
    }
}

fun loadFavoriteMusicians(): List<MusicianListItemUiModel> {
    return try {
        val payload = RustCoreBridge.getFavoriteArtistIds(requireUserDbPath(), "musician")
        if (payload.isBlank()) return emptyList()
        val ids = json.decodeFromString<List<Long>>(payload)
        if (ids.isEmpty()) return emptyList()
        val allMusiciansPayload = RustCoreBridge.getMusiciansJson(requireArchiveDbPath())
        val musicians = json.decodeFromString<List<MusicianDto>>(allMusiciansPayload)
        musicians.filter { ids.contains(it.id) }.map {
            MusicianListItemUiModel(artistId = it.id, name = it.name, instrument = it.instrument, imageUrl = it.avatar, programCount = it.programCount)
        }
    } catch (e: Exception) {
        println("ERROR loadFavoriteMusicians: ${e.message}")
        emptyList()
    }
}

fun loadMostPlayedIds(limit: Long): List<Long> {
    return try {
        val payload = RustCoreBridge.getMostPlayedIds(requireUserDbPath(), limit)
        if (payload.isBlank()) return emptyList()
        json.decodeFromString<List<Long>>(payload)
    } catch (e: Exception) {
        println("ERROR loadMostPlayedIds: ${e.message}")
        emptyList()
    }
}

internal fun TrackDto.toTrackUiModel() = TrackUiModel(
    id = id,
    artistId = artistId,
    title = title,
    artist = artist.split(" - ")[0],
    duration = duration,
    audioUrl = audioUrl,
    coverUrl = avatar ?: cover,
    artistImages = if (avatar != null) listOf(avatar) else if (cover != null) listOf(cover) else emptyList()
)


internal fun CategoryProgramDto.toCategoryProgramUiModel() = CategoryProgramUiModel(
    id = id,
    title = title ?: "برنامه $no",
    categoryName = null,
    programNumber = no.toString(),
    singer = artist,
    duration = duration,
    dastgah = mode,
    audioUrl = audioUrl
)

@kotlinx.serialization.Serializable
internal data class PlaylistDto(
    val id: Long,
    val name: String
)
