package com.radiogolha.mobile.ui.search

data class SearchOptionUiModel(
    val id: Long,
    val name: String,
)

enum class SearchFilterType(val label: String) {
    Category("دسته برنامه"),
    Singer("خواننده"),
    Mode("دستگاه"),
    Orchestra("ارکستر"),
    Instrument("ساز"),
    Performer("نوازنده"),
    Poet("شاعر"),
    Announcer("گوینده"),
    Composer("آهنگساز"),
    Arranger("تنظیم‌کننده"),
    OrchestraLeader("رهبر ارکستر"),
}

data class SearchOptionsUiState(
    val categories: List<SearchOptionUiModel> = emptyList(),
    val singers: List<SearchOptionUiModel> = emptyList(),
    val modes: List<SearchOptionUiModel> = emptyList(),
    val orchestras: List<SearchOptionUiModel> = emptyList(),
    val instruments: List<SearchOptionUiModel> = emptyList(),
    val performers: List<SearchOptionUiModel> = emptyList(),
    val poets: List<SearchOptionUiModel> = emptyList(),
    val announcers: List<SearchOptionUiModel> = emptyList(),
    val composers: List<SearchOptionUiModel> = emptyList(),
    val arrangers: List<SearchOptionUiModel> = emptyList(),
    val orchestraLeaders: List<SearchOptionUiModel> = emptyList(),
) {
    fun optionsFor(type: SearchFilterType): List<SearchOptionUiModel> = when (type) {
        SearchFilterType.Category -> categories
        SearchFilterType.Singer -> singers
        SearchFilterType.Mode -> modes
        SearchFilterType.Orchestra -> orchestras
        SearchFilterType.Instrument -> instruments
        SearchFilterType.Performer -> performers
        SearchFilterType.Poet -> poets
        SearchFilterType.Announcer -> announcers
        SearchFilterType.Composer -> composers
        SearchFilterType.Arranger -> arrangers
        SearchFilterType.OrchestraLeader -> orchestraLeaders
    }
}

data class ActiveFilters(
    val transcriptQuery: String = "",
    val categoryIds: Set<Long> = emptySet(),
    val singerIds: Set<Long> = emptySet(),
    val singerMatch: MatchMode = MatchMode.Any,
    val modeIds: Set<Long> = emptySet(),
    val modeMatch: MatchMode = MatchMode.Any,
    val orchestraIds: Set<Long> = emptySet(),
    val orchestraMatch: MatchMode = MatchMode.Any,
    val instrumentIds: Set<Long> = emptySet(),
    val instrumentMatch: MatchMode = MatchMode.Any,
    val performerIds: Set<Long> = emptySet(),
    val performerMatch: MatchMode = MatchMode.Any,
    val poetIds: Set<Long> = emptySet(),
    val poetMatch: MatchMode = MatchMode.Any,
    val announcerIds: Set<Long> = emptySet(),
    val announcerMatch: MatchMode = MatchMode.Any,
    val composerIds: Set<Long> = emptySet(),
    val composerMatch: MatchMode = MatchMode.Any,
    val arrangerIds: Set<Long> = emptySet(),
    val arrangerMatch: MatchMode = MatchMode.Any,
    val orchestraLeaderIds: Set<Long> = emptySet(),
    val orchestraLeaderMatch: MatchMode = MatchMode.Any,
) {
    fun idsFor(type: SearchFilterType): Set<Long> = when (type) {
        SearchFilterType.Category -> categoryIds
        SearchFilterType.Singer -> singerIds
        SearchFilterType.Mode -> modeIds
        SearchFilterType.Orchestra -> orchestraIds
        SearchFilterType.Instrument -> instrumentIds
        SearchFilterType.Performer -> performerIds
        SearchFilterType.Poet -> poetIds
        SearchFilterType.Announcer -> announcerIds
        SearchFilterType.Composer -> composerIds
        SearchFilterType.Arranger -> arrangerIds
        SearchFilterType.OrchestraLeader -> orchestraLeaderIds
    }

    fun withToggled(type: SearchFilterType, id: Long): ActiveFilters {
        fun toggle(set: Set<Long>) = if (id in set) set - id else set + id
        return when (type) {
            SearchFilterType.Category -> copy(categoryIds = toggle(categoryIds))
            SearchFilterType.Singer -> copy(singerIds = toggle(singerIds))
            SearchFilterType.Mode -> copy(modeIds = toggle(modeIds))
            SearchFilterType.Orchestra -> copy(orchestraIds = toggle(orchestraIds))
            SearchFilterType.Instrument -> copy(instrumentIds = toggle(instrumentIds))
            SearchFilterType.Performer -> copy(performerIds = toggle(performerIds))
            SearchFilterType.Poet -> copy(poetIds = toggle(poetIds))
            SearchFilterType.Announcer -> copy(announcerIds = toggle(announcerIds))
            SearchFilterType.Composer -> copy(composerIds = toggle(composerIds))
            SearchFilterType.Arranger -> copy(arrangerIds = toggle(arrangerIds))
            SearchFilterType.OrchestraLeader -> copy(orchestraLeaderIds = toggle(orchestraLeaderIds))
        }
    }

    fun clearType(type: SearchFilterType): ActiveFilters = when (type) {
        SearchFilterType.Category -> copy(categoryIds = emptySet())
        SearchFilterType.Singer -> copy(singerIds = emptySet())
        SearchFilterType.Mode -> copy(modeIds = emptySet())
        SearchFilterType.Orchestra -> copy(orchestraIds = emptySet())
        SearchFilterType.Instrument -> copy(instrumentIds = emptySet())
        SearchFilterType.Performer -> copy(performerIds = emptySet())
        SearchFilterType.Poet -> copy(poetIds = emptySet())
        SearchFilterType.Announcer -> copy(announcerIds = emptySet())
        SearchFilterType.Composer -> copy(composerIds = emptySet())
        SearchFilterType.Arranger -> copy(arrangerIds = emptySet())
        SearchFilterType.OrchestraLeader -> copy(orchestraLeaderIds = emptySet())
    }

    fun matchModeFor(type: SearchFilterType): MatchMode = when (type) {
        SearchFilterType.Category -> MatchMode.Any
        SearchFilterType.Singer -> singerMatch
        SearchFilterType.Mode -> modeMatch
        SearchFilterType.Orchestra -> orchestraMatch
        SearchFilterType.Instrument -> instrumentMatch
        SearchFilterType.Performer -> performerMatch
        SearchFilterType.Poet -> poetMatch
        SearchFilterType.Announcer -> announcerMatch
        SearchFilterType.Composer -> composerMatch
        SearchFilterType.Arranger -> arrangerMatch
        SearchFilterType.OrchestraLeader -> orchestraLeaderMatch
    }

    fun withMatchMode(type: SearchFilterType, mode: MatchMode): ActiveFilters = when (type) {
        SearchFilterType.Category -> this
        SearchFilterType.Singer -> copy(singerMatch = mode)
        SearchFilterType.Mode -> copy(modeMatch = mode)
        SearchFilterType.Orchestra -> copy(orchestraMatch = mode)
        SearchFilterType.Instrument -> copy(instrumentMatch = mode)
        SearchFilterType.Performer -> copy(performerMatch = mode)
        SearchFilterType.Poet -> copy(poetMatch = mode)
        SearchFilterType.Announcer -> copy(announcerMatch = mode)
        SearchFilterType.Composer -> copy(composerMatch = mode)
        SearchFilterType.Arranger -> copy(arrangerMatch = mode)
        SearchFilterType.OrchestraLeader -> copy(orchestraLeaderMatch = mode)
    }

    val activeFilterCount: Int get() = listOf(
        categoryIds, singerIds, modeIds, orchestraIds, instrumentIds, performerIds,
        poetIds, announcerIds, composerIds, arrangerIds, orchestraLeaderIds
    ).count { it.isNotEmpty() }

    val hasAnyFilter: Boolean get() = transcriptQuery.isNotBlank() || activeFilterCount > 0
}

enum class MatchMode(val label: String, val value: String) {
    Any("هرکدام", "any"),
    All("همه", "all"),
}

data class SearchResultUiModel(
    val id: Long,
    val title: String,
    val categoryName: String,
    val no: Int,
    val subNo: String?,
    val duration: String?,
)

data class SearchResultsUiState(
    val results: List<SearchResultUiModel> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
)
