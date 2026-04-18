package com.radiogolha.mobile.ui.artists

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadArtistDetail(artistId: Long): ArtistDetailUiModel? {
    val payload = RustCoreBridge.getArtistDetailJson(requireArchiveDbPath(), artistId)
    if (payload.isBlank()) return null
    
    val response = json.decodeFromString<ArtistDetailResponse>(payload)
    
    return ArtistDetailUiModel(
        artistId = response.id,
        name = response.name,
        imageUrl = response.avatar,
        instrument = null, // Could be found in bio if needed
        trackCount = response.programs.size,
        tracks = response.programs.map { it.toCategoryProgramUiModel() },
        isFavorite = try {
            RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), artistId) == "true"
        } catch (e: Exception) {
            false
        }
    )
}
