package com.radiogolha.mobile.ui.home

import com.radiogolha.mobile.RustCoreBridge
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

expect fun requireArchiveDbPath(): String

fun loadHomeUiState(): HomeUiState? {
    return try {
        val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
        if (payload.isBlank()) return null
        
        val response = json.decodeFromString<HomeFeedResponse>(payload)
        
        HomeUiState(
            programs = response.categories.map { ProgramUiModel(it.id, it.title, it.episodeCount) },
            singers = response.singers.map { SingerUiModel(it.id, it.name, it.avatar) },
            dastgahs = response.dastgahs.map { DastgahUiModel(it.name) },
            musicians = response.musicians.map { MusicianUiModel(it.id, it.name, it.instrument, it.avatar) },
            topTracks = response.topTracks.map { it.toTrackUiModel() },
            bottomNavItems = emptyList()
        )
    } catch (e: Exception) {
        println("ERROR_GOLHA: loading home state: ${e.message ?: "unknown error"}")
        null
    }
}

fun loadTopTracks(): List<TrackUiModel> {
    val payload = RustCoreBridge.getTopTracksJson(requireArchiveDbPath())
    val tracks = json.decodeFromString<List<TrackDto>>(payload)
    return tracks.map { it.toTrackUiModel() }
}

fun loadProgramsByIds(ids: List<Long>): List<CategoryProgramUiModel> {
    if (ids.isEmpty()) return emptyList()
    // Using a simple JSON array string for IDs as Rust bridge expects it
    val idsJson = ids.joinToString(prefix = "[", postfix = "]", separator = ",")
    val payload = RustCoreBridge.getProgramsByIdsJson(requireArchiveDbPath(), idsJson)
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
}

fun loadProgramsByMode(modeId: Long): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getProgramsByModeJson(requireArchiveDbPath(), modeId)
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
}

fun loadDuetPairsConfig(): List<DuetPairUiModel> {
    val payload = RustCoreBridge.getDuetPairsConfigJson(requireArchiveDbPath())
    val response = json.decodeFromString<List<DuetPairDto>>(payload)
    return response.map { 
        DuetPairUiModel(
            singer1 = it.singer1,
            singer2 = it.singer2,
            singer1Avatar = it.singer1Avatar,
            singer2Avatar = it.singer2Avatar,
            trackCount = it.trackCount
        )
    }
}

fun loadOrderedModes(): List<String> {
    val payload = RustCoreBridge.getOrderedModesJson(requireArchiveDbPath())
    return json.decodeFromString<List<String>>(payload)
}

fun loadDuetPrograms(singer1: String, singer2: String): List<CategoryProgramUiModel> {
    val payload = RustCoreBridge.getDuetProgramsJson(requireArchiveDbPath(), singer1, singer2)
    val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
    return response.map { it.toCategoryProgramUiModel() }
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
