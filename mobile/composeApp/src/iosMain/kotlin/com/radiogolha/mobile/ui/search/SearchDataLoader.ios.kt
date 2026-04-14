package com.radiogolha.mobile.ui.search

actual fun loadSearchOptions(): SearchOptionsUiState = SearchOptionsUiState()

actual fun searchPrograms(filters: ActiveFilters, page: Int): SearchResultsUiState = SearchResultsUiState()
