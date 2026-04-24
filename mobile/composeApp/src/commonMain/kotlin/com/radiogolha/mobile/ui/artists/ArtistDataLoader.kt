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

    val responseInstrument = response.instrument?.takeIf { it.isNotBlank() }
    val instrumentFromMusicians = if (responseInstrument == null) {
        runCatching {
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
    } else null

    val typeLabel = when (response.type.lowercase()) {
        "musician", "performer" -> "نوازنده"
        "singer" -> "خواننده"
        else -> "هنرمند"
    }
    
    return ArtistDetailUiModel(
        artistId = response.id,
        name = response.name,
        imageUrl = response.avatar,
        instrument = responseInstrument ?: instrumentFromMusicians ?: typeLabel,
        trackCount = maxOf(programs.size, response.trackCount.toInt()),
        tracks = programs.map { it.toCategoryProgramUiModel() },
        isFavorite = try {
            RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), artistId) == "true"
        } catch (e: Exception) {
            false
        },
        categoryCounts = response.categoryCounts.map {
            ArtistCategoryCountUiModel(title = it.title, count = it.count.toInt())
        },
        collaborators = response.collaborators.map {
            ArtistCollaboratorUiModel(
                id = it.id,
                name = it.name,
                role = it.role,
                imageUrl = it.avatar,
            )
        },
        topModes = response.topModes.take(4),
    )
}

@Serializable
private data class ArtistDetailBridgeResponse(
    val id: Long = 0,
    val name: String = "",
    val type: String = "",
    val avatar: String? = null,
    val instrument: String? = null,
    val bio: String? = null,
    val programs: List<CategoryProgramDto> = emptyList(),
    val tracks: List<CategoryProgramDto> = emptyList(),
    val trackCount: Long = 0,
    val categoryCounts: List<ArtistCategoryCountDto> = emptyList(),
    val collaborators: List<ArtistCollaboratorDto> = emptyList(),
    val topModes: List<String> = emptyList(),
)

@Serializable
private data class ArtistCategoryCountDto(
    val categoryId: Long = 0,
    val title: String = "",
    val count: Long = 0,
)

@Serializable
private data class ArtistCollaboratorDto(
    val id: Long = 0,
    val name: String = "",
    val avatar: String? = null,
    val kind: String = "",
    val role: String = "",
    val sharedCount: Long = 0,
)
