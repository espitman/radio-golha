package com.radiogolha.mobile.ui.search

expect fun loadSearchOptions(): SearchOptionsUiState

expect fun searchPrograms(filters: ActiveFilters, page: Int): SearchResultsUiState
