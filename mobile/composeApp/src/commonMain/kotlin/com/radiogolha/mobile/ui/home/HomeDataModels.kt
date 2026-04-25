package com.radiogolha.mobile.ui.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
internal data class HomeFeedResponse(
    val programs: List<ProgramDto> = emptyList(),
    val singers: List<SingerDto> = emptyList(),
    val dastgahs: List<DastgahDto> = emptyList(),
    val musicians: List<MusicianDto> = emptyList(),
    val topTracks: List<TrackDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val duets: List<DuetPairDto> = emptyList()
)

@Serializable
internal data class CategoryDto(
    val id: Long = 0,
    val title: String = "",
    val episodeCount: Int = 0
)

@Serializable
internal data class ProgramDto(
    val title: String = "",
    val episodeCount: Int = 0
)

@Serializable
internal data class SingerDto(
    val id: Long = 0,
    val name: String = "",
    val avatar: String? = null,
    val programCount: Int = 0
)

@Serializable
internal data class DastgahDto(
    val name: String = ""
)

@Serializable
internal data class MusicianDto(
    val id: Long = 0,
    val name: String = "",
    val instrument: String = "",
    val avatar: String? = null,
    val programCount: Int = 0
)

@Serializable
internal data class TrackDto(
    val id: Long = 0,
    val artistId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val duration: String = "",
    val audioUrl: String? = null,
    val avatar: String? = null,
    val cover: String? = null
)

@Serializable
internal data class CategoryProgramDto(
    val id: Long = 0,
    val title: String? = null,
    val no: Long = 0,
    val artist: String = "",
    val duration: String? = null,
    val mode: String? = null,
    val audioUrl: String? = null,
    val singerAvatars: List<String> = emptyList(),
)

@Serializable
internal data class DuetPairDto(
    @SerialName("singer1") val singer1: String = "",
    @SerialName("singer2") val singer2: String = "",
    @SerialName("singer1Avatar") val singer1Avatar: String? = null,
    @SerialName("singer2Avatar") val singer2Avatar: String? = null,
    @SerialName("trackCount") val trackCount: Int = 0
)

@Serializable
internal data class SearchOptionsResponse(
    val categories: List<SearchOptionDto> = emptyList(),
    val singers: List<SearchOptionDto> = emptyList(),
    val modes: List<SearchOptionDto> = emptyList(),
    val orchestras: List<SearchOptionDto> = emptyList(),
    val instruments: List<SearchOptionDto> = emptyList(),
    val performers: List<SearchOptionDto> = emptyList(),
    val poets: List<SearchOptionDto> = emptyList(),
    val announcers: List<SearchOptionDto> = emptyList(),
    val composers: List<SearchOptionDto> = emptyList(),
    val arrangers: List<SearchOptionDto> = emptyList(),
    val orchestraLeaders: List<SearchOptionDto> = emptyList(),
    val orchestra_leaders: List<SearchOptionDto> = emptyList() // API fallback
)

@Serializable
internal data class SearchOptionDto(
    val id: Long = 0,
    val name: String? = null,
    val titleFa: String? = null
)

@Serializable
internal data class SearchResultsResponse(
    val rows: List<SearchResultDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val total_pages: Int? = null // API fallback
)

@Serializable
internal data class SearchResultDto(
    val id: Long = 0,
    val title: String = "",
    @SerialName("category_name")
    val categoryName: String = "",
    val no: Int = 0,
    @SerialName("sub_no")
    val subNo: String? = null,
    val duration: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    val artist: String? = null
)

@Serializable
internal data class SearchFiltersDto(
    val transcriptQuery: String? = null,
    val page: Int = 1,
    val categoryIds: List<Long> = emptyList(),
    val singerIds: List<Long> = emptyList(),
    val singerMatch: String? = null,
    val modeIds: List<Long> = emptyList(),
    val modeMatch: String? = null,
    val orchestraIds: List<Long> = emptyList(),
    val orchestraMatch: String? = null,
    val instrumentIds: List<Long> = emptyList(),
    val instrumentMatch: String? = null,
    val performerIds: List<Long> = emptyList(),
    val performerMatch: String? = null,
    val poetIds: List<Long> = emptyList(),
    val poetMatch: String? = null,
    val announcerIds: List<Long> = emptyList(),
    val announcerMatch: String? = null,
    val composerIds: List<Long> = emptyList(),
    val composerMatch: String? = null,
    val arrangerIds: List<Long> = emptyList(),
    val arrangerMatch: String? = null,
    val orchestraLeaderIds: List<Long> = emptyList(),
    val orchestraLeaderMatch: String? = null
)

@Serializable
internal data class ArtistDetailResponse(
    val id: Long = 0,
    val name: String = "",
    val type: String = "",
    val avatar: String? = null,
    val bio: String? = null,
    val programs: List<CategoryProgramDto> = emptyList()
)

@Serializable
internal data class ProgramDetailResponse(
    val id: Long = 0,
    val title: String = "",
    val duration: String? = null,
    val audioUrl: String? = null,
    val description: String? = null,
    val singers: List<SingerDto> = emptyList(),
    val performers: List<MusicianDto> = emptyList(),
    val composers: List<SingerDto> = emptyList(),
    val arrangers: List<SingerDto> = emptyList(),
    val orchestras: List<SingerDto> = emptyList()
)

internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
