package com.radiogolha.mobile.ui.programs

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

fun loadProgramsUiState(): List<ProgramUiModel> {
    return try {
        val payload = RustCoreBridge.getHomeFeedJson(requireArchiveDbPath())
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<HomeFeedResponse>(payload)
        response.categories.map { ProgramUiModel(it.id, it.title, it.episodeCount) }
    } catch (e: Exception) {
        println("ERROR loadProgramsUiState: ${e.message}")
        emptyList()
    }
}

fun loadCategoryPrograms(categoryId: Long): List<CategoryProgramUiModel> {
    return try {
        val payload = RustCoreBridge.getProgramsByCategoryJson(requireArchiveDbPath(), categoryId) 
        if (payload.isBlank()) return emptyList()
        val response = json.decodeFromString<List<CategoryProgramDto>>(payload)
        response.map { it.toCategoryProgramUiModel() }
    } catch (e: Exception) {
        println("ERROR loadCategoryPrograms: ${e.message}")
        emptyList()
    }
}

fun loadProgramEpisodeDetail(programId: Long): ProgramEpisodeDetailUiModel? {
    return try {
        val payload = RustCoreBridge.getProgramDetailJson(requireArchiveDbPath(), programId)
        if (payload.isBlank()) return null
        
        val response = json.decodeFromString<ProgramDetailApiResponse>(payload)
        
        ProgramEpisodeDetailUiModel(
            id = response.id,
            title = response.title,
            categoryName = response.categoryName,
            no = response.no,
            subNo = response.subNo,
            duration = response.duration,
            audioUrl = response.audioUrl,
            singers = response.singers.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            poets = response.poets.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            announcers = response.announcers.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            composers = response.composers.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            arrangers = response.arrangers.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            modes = response.modes,
            orchestras = response.orchestras.map { ArtistCreditUiModel(it.artistId, it.name, it.avatar) },
            orchestraLeaders = response.orchestraLeaders.map { OrchestraLeaderUiModel(it.artistId, it.orchestra, it.name) },
            performers = response.performers.map { PerformerUiModel(it.artistId, it.name, it.avatar, it.instrument) },
            timeline = response.timeline.map { segment ->
                TimelineSegmentUiModel(
                    id = segment.id,
                    startTime = segment.startTime,
                    endTime = segment.endTime,
                    modeName = segment.modeName,
                    singers = segment.singers,
                    poets = segment.poets,
                    announcers = segment.announcers,
                    orchestras = segment.orchestras,
                    orchestraLeaders = segment.orchestraLeaders.map { OrchestraLeaderUiModel(it.artistId, it.orchestra, it.name) },
                    performers = segment.performers.map { PerformerUiModel(it.artistId, it.name, it.avatar, it.instrument) },
                )
            },
            transcript = response.transcript.map { TranscriptVerseUiModel(it.segmentOrder, it.verseOrder, it.text) }
        )
    } catch (e: Exception) {
        println("ERROR loading program detail: ${e.message}")
        null
    }
}

@Serializable
private data class ArtistCreditDto(
    val artistId: Long? = null,
    val name: String = "",
    val avatar: String? = null,
)

@Serializable
private data class PerformerCreditDto(
    val artistId: Long? = null,
    val name: String = "",
    val avatar: String? = null,
    val instrument: String? = null,
)

@Serializable
private data class OrchestraLeaderCreditDto(
    val artistId: Long? = null,
    val orchestra: String = "",
    val name: String = "",
)

@Serializable
private data class TimelineSegmentDto(
    val id: Long = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val modeName: String? = null,
    val singers: List<String> = emptyList(),
    val poets: List<String> = emptyList(),
    val announcers: List<String> = emptyList(),
    val orchestras: List<String> = emptyList(),
    val orchestraLeaders: List<OrchestraLeaderCreditDto> = emptyList(),
    val performers: List<PerformerCreditDto> = emptyList(),
)

@Serializable
private data class TranscriptVerseDto(
    val segmentOrder: Int = 0,
    val verseOrder: Int = 0,
    val text: String = "",
)

@Serializable
private data class ProgramDetailApiResponse(
    val id: Long = 0,
    val title: String = "",
    val categoryName: String = "",
    val no: Int = 0,
    val subNo: String? = null,
    val duration: String? = null,
    val audioUrl: String? = null,
    val singers: List<ArtistCreditDto> = emptyList(),
    val poets: List<ArtistCreditDto> = emptyList(),
    val announcers: List<ArtistCreditDto> = emptyList(),
    val composers: List<ArtistCreditDto> = emptyList(),
    val arrangers: List<ArtistCreditDto> = emptyList(),
    val modes: List<String> = emptyList(),
    val orchestras: List<ArtistCreditDto> = emptyList(),
    val orchestraLeaders: List<OrchestraLeaderCreditDto> = emptyList(),
    val performers: List<PerformerCreditDto> = emptyList(),
    val timeline: List<TimelineSegmentDto> = emptyList(),
    val transcript: List<TranscriptVerseDto> = emptyList(),
)
