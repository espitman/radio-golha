package com.radiogolha.mobile.ui.search

import com.radiogolha.mobile.RustCoreBridge
import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import org.json.JSONArray
import org.json.JSONObject

actual fun loadSearchOptions(): SearchOptionsUiState {
    val payload = RustCoreBridge.getSearchOptionsJson(requireArchiveDbPath())
    val root = JSONObject(payload)
    return SearchOptionsUiState(
        categories = root.optSearchOptions("categories", nameKey = "titleFa", fallbackNameKey = "title_fa"),
        singers = root.optSearchOptions("singers"),
        modes = root.optSearchOptions("modes"),
        orchestras = root.optSearchOptions("orchestras"),
        instruments = root.optSearchOptions("instruments"),
        performers = root.optSearchOptions("performers"),
        poets = root.optSearchOptions("poets"),
        announcers = root.optSearchOptions("announcers"),
        composers = root.optSearchOptions("composers"),
        arrangers = root.optSearchOptions("arrangers"),
        orchestraLeaders = root.optSearchOptions("orchestraLeaders")
            .takeIf { it.isNotEmpty() }
            ?: root.optSearchOptions("orchestra_leaders"),
    )
}

actual fun searchPrograms(filters: ActiveFilters, page: Int): SearchResultsUiState {
    val filtersJson = buildFiltersJson(filters, page)
    val payload = RustCoreBridge.searchProgramsJson(requireArchiveDbPath(), filtersJson)
    val root = JSONObject(payload)
    val rows = root.optJSONArray("rows") ?: JSONArray()
    val results = buildList {
        for (i in 0 until rows.length()) {
            val item = rows.getJSONObject(i)
            add(
                SearchResultUiModel(
                    id = item.optLong("id"),
                    title = item.optString("title", ""),
                    categoryName = item.optString("categoryName")
                        .takeIf { it.isNotBlank() }
                        ?: item.optString("category_name", ""),
                    no = item.optInt("no", 0),
                    subNo = item.optString("subNo").takeIf { it.isNotBlank() }
                        ?: item.optString("sub_no").takeIf { it.isNotBlank() },
                    duration = item.optString("duration").takeIf { it.isNotBlank() },
                )
            )
        }
    }
    return SearchResultsUiState(
        results = results,
        total = root.optInt("total", 0),
        page = root.optInt("page", 1),
        totalPages = root.optInt("totalPages").takeIf { it > 0 }
            ?: root.optInt("total_pages", 1),
    )
}

private fun JSONObject.optSearchOptions(
    key: String,
    nameKey: String = "name",
    fallbackNameKey: String? = null,
): List<SearchOptionUiModel> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val name = item.optString(nameKey).takeIf { it.isNotBlank() }
                ?: fallbackNameKey?.let { item.optString(it).takeIf { n -> n.isNotBlank() } }
                ?: ""
            if (name.isNotBlank()) {
                add(SearchOptionUiModel(id = item.optLong("id"), name = name))
            }
        }
    }
}

private fun buildFiltersJson(filters: ActiveFilters, page: Int): String {
    val obj = JSONObject()
    if (filters.transcriptQuery.isNotBlank()) obj.put("transcriptQuery", filters.transcriptQuery)
    obj.put("page", page)
    if (filters.categoryIds.isNotEmpty()) obj.put("categoryIds", JSONArray(filters.categoryIds.toList()))
    if (filters.singerIds.isNotEmpty()) obj.put("singerIds", JSONArray(filters.singerIds.toList()))
    if (filters.modeIds.isNotEmpty()) obj.put("modeIds", JSONArray(filters.modeIds.toList()))
    if (filters.orchestraIds.isNotEmpty()) obj.put("orchestraIds", JSONArray(filters.orchestraIds.toList()))
    if (filters.instrumentIds.isNotEmpty()) obj.put("instrumentIds", JSONArray(filters.instrumentIds.toList()))
    if (filters.performerIds.isNotEmpty()) obj.put("performerIds", JSONArray(filters.performerIds.toList()))
    if (filters.poetIds.isNotEmpty()) obj.put("poetIds", JSONArray(filters.poetIds.toList()))
    if (filters.announcerIds.isNotEmpty()) obj.put("announcerIds", JSONArray(filters.announcerIds.toList()))
    if (filters.composerIds.isNotEmpty()) obj.put("composerIds", JSONArray(filters.composerIds.toList()))
    if (filters.arrangerIds.isNotEmpty()) obj.put("arrangerIds", JSONArray(filters.arrangerIds.toList()))
    if (filters.orchestraLeaderIds.isNotEmpty()) obj.put("orchestraLeaderIds", JSONArray(filters.orchestraLeaderIds.toList()))
    return obj.toString()
}
