package com.radiogolha.mobile.ui.artists

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadArtistDetail(artistId: Long): ArtistDetailUiModel? {
    val payload = RustCoreBridge.getArtistDetailJson(requireArchiveDbPath(), artistId)
    if (payload.isBlank()) return null
    
    val response = json.decodeFromString<ArtistDetailResponse>(payload)

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
        trackCount = response.programs.size,
        tracks = response.programs.map { it.toCategoryProgramUiModel() },
        isFavorite = try {
            RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), artistId) == "true"
        } catch (e: Exception) {
            false
        }
    )
}
