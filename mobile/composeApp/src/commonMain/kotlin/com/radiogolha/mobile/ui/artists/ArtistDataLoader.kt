package com.radiogolha.mobile.ui.artists

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.decodeFromString

fun loadArtistDetail(artistId: Long): ArtistDetailUiState {
    val payload = RustCoreBridge.getArtistDetailJson(requireArchiveDbPath(), artistId)
    val response = json.decodeFromString<ArtistDetailResponse>(payload)
    
    return ArtistDetailUiState(
        id = response.id,
        name = response.name,
        type = response.type,
        avatar = response.avatar,
        bio = response.bio,
        programs = response.programs.map { it.toCategoryProgramUiModel() }
    )
}
