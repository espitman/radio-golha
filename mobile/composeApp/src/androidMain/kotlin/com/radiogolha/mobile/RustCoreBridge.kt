package com.radiogolha.mobile

object RustCoreBridge {
    init {
        System.loadLibrary("radiogolha_android")
    }

    external fun getCategoriesJson(dbPath: String): String
    external fun getHomeFeedJson(dbPath: String): String
    external fun getTopTracksJson(dbPath: String): String
    external fun getSingersJson(dbPath: String): String
    external fun getMusiciansJson(dbPath: String): String
    external fun getArtistDetailJson(dbPath: String, artistId: Long): String
    external fun getProgramsByCategoryJson(dbPath: String, categoryId: Long): String
    external fun getProgramDetailJson(dbPath: String, programId: Long): String
    external fun getOrchestrasJson(dbPath: String): String
    external fun getProgramsByOrchestraJson(dbPath: String, orchestraId: Long): String
    external fun getProgramsByIdsJson(dbPath: String, idsJson: String): String
    external fun getDuetPairsConfigJson(dbPath: String): String
    external fun getProgramsByModeJson(dbPath: String, modeId: Long): String
    external fun getOrderedModesJson(dbPath: String): String
    external fun getConfigJson(dbPath: String, key: String): String
    external fun getSearchOptionsJson(dbPath: String): String
    external fun searchProgramsJson(dbPath: String, filtersJson: String): String
    external fun getDuetProgramsJson(dbPath: String, singer1: String, singer2: String): String

    // User Data (Playlists)
    external fun getAllPlaylists(userDbPath: String): String
    external fun getPlaylist(userDbPath: String, id: Long): String
    external fun createPlaylist(userDbPath: String, requestJson: String): String
    external fun renamePlaylist(userDbPath: String, id: Long, name: String): String
    external fun deletePlaylist(userDbPath: String, id: Long): String
    external fun addTrackToPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
    external fun removeTrackFromPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
}
