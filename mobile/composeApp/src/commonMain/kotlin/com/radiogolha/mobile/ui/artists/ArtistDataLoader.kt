package com.radiogolha.mobile.ui.artists

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

fun loadArtistDetail(artistId: Long): ArtistDetailUiModel? {
    val payload = RustCoreBridge.getArtistDetailJson(requireArchiveDbPath(), artistId)
    if (payload.isBlank()) return null
    
    val response = json.decodeFromString<ArtistDetailBridgeResponse>(payload)
    val programs = when {
        response.programs.isNotEmpty() -> response.programs
        response.tracks.isNotEmpty() -> response.tracks
        else -> emptyList()
    }

    val instrumentFromMusicians = runCatching {
        val musiciansPayload = RustCoreBridge.getMusiciansJson(requireArchiveDbPath())
        if (musiciansPayload.isBlank()) null
        else {
            val musicians = json.decodeFromString<List<MusicianDto>>(musiciansPayload)
            musicians
                .firstOrNull { it.id == response.id }
                ?.instrument
                ?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    val typeLabel = when (response.type.lowercase()) {
        "musician", "performer" -> "نوازنده"
        "singer" -> "خواننده"
        else -> "هنرمند"
    }
    
    return ArtistDetailUiModel(
        artistId = response.id,
        name = response.name,
        imageUrl = response.avatar,
        instrument = instrumentFromMusicians ?: typeLabel,
        trackCount = programs.size,
        tracks = programs.map { it.toCategoryProgramUiModel() },
        isFavorite = try {
            RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), artistId) == "true"
        } catch (e: Exception) {
            false
        }
    )
}

@Serializable
private data class ArtistDetailBridgeResponse(
    val id: Long = 0,
    val name: String = "",
    val type: String = "",
    val avatar: String? = null,
    val bio: String? = null,
    val programs: List<CategoryProgramDto> = emptyList(),
    val tracks: List<CategoryProgramDto> = emptyList(),
)
