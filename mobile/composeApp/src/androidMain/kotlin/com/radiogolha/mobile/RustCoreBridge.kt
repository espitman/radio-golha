package com.radiogolha.mobile

actual object RustCoreBridge {
    init {
        System.loadLibrary("radiogolha_android")
    }

    actual external fun getCategoriesJson(dbPath: String): String
    actual external fun getHomeFeedJson(dbPath: String): String
    actual external fun getTopTracksJson(dbPath: String): String
    actual external fun getSingersJson(dbPath: String): String
    actual external fun getMusiciansJson(dbPath: String): String
    actual external fun getArtistDetailJson(dbPath: String, artistId: Long): String
    actual external fun getProgramsByCategoryJson(dbPath: String, categoryId: Long): String
    actual external fun getProgramDetailJson(dbPath: String, programId: Long): String
    actual external fun getOrchestrasJson(dbPath: String): String
    actual external fun getProgramsByOrchestraJson(dbPath: String, orchestraId: Long): String
    actual external fun getProgramsByIdsJson(dbPath: String, idsJson: String): String
    actual external fun getDuetPairsConfigJson(dbPath: String): String
    actual external fun getProgramsByModeJson(dbPath: String, modeId: Long): String
    actual external fun getOrderedModesJson(dbPath: String): String
    actual external fun getConfigJson(dbPath: String, key: String): String
    actual external fun getSearchOptionsJson(dbPath: String): String
    actual external fun searchProgramsJson(dbPath: String, filtersJson: String): String
    actual external fun getDuetProgramsJson(dbPath: String, singer1: String, singer2: String): String

    // User Data (Playlists)
    actual external fun getAllPlaylists(userDbPath: String): String
    actual external fun getPlaylist(userDbPath: String, id: Long): String
    actual external fun createPlaylist(userDbPath: String, requestJson: String): String
    actual external fun renamePlaylist(userDbPath: String, id: Long, name: String): String
    actual external fun deletePlaylist(userDbPath: String, id: Long): String
    actual external fun addTrackToPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
    actual external fun removeTrackFromPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
    actual external fun getManualPlaylists(userDbPath: String): String

    // Favorite Artists
    actual external fun addFavoriteArtist(userDbPath: String, artistId: Long, artistType: String): String
    actual external fun removeFavoriteArtist(userDbPath: String, artistId: Long): String
    actual external fun isFavoriteArtist(userDbPath: String, artistId: Long): String
    actual external fun getFavoriteArtistIds(userDbPath: String, artistType: String): String

    // Playback History
    actual external fun recordPlayback(userDbPath: String, trackId: Long): String
    actual external fun getRecentlyPlayedIds(userDbPath: String, limit: Long): String
    actual external fun getMostPlayedIds(userDbPath: String, limit: Long): String
}
