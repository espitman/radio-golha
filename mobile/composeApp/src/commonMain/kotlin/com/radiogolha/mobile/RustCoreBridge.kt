package com.radiogolha.mobile

expect object RustCoreBridge {
    fun getCategoriesJson(dbPath: String): String
    fun getHomeFeedJson(dbPath: String): String
    fun getTopTracksJson(dbPath: String): String
    fun getSingersJson(dbPath: String): String
    fun getMusiciansJson(dbPath: String): String
    fun getArtistDetailJson(dbPath: String, artistId: Long): String
    fun getProgramsByCategoryJson(dbPath: String, categoryId: Long): String
    fun getProgramDetailJson(dbPath: String, programId: Long): String
    fun getOrchestrasJson(dbPath: String): String
    fun getProgramsByOrchestraJson(dbPath: String, orchestraId: Long): String
    fun getProgramsByIdsJson(dbPath: String, idsJson: String): String
    fun getDuetPairsConfigJson(dbPath: String): String
    fun getProgramsByModeJson(dbPath: String, modeId: Long): String
    fun getOrderedModesJson(dbPath: String): String
    fun getConfigJson(dbPath: String, key: String): String
    fun getSearchOptionsJson(dbPath: String): String
    fun searchProgramsJson(dbPath: String, filtersJson: String): String
    fun getDuetProgramsJson(dbPath: String, singer1: String, singer2: String): String
    
    // User Data
    fun getAllPlaylists(userDbPath: String): String
    fun getPlaylist(userDbPath: String, id: Long): String
    fun createPlaylist(userDbPath: String, requestJson: String): String
    fun renamePlaylist(userDbPath: String, id: Long, name: String): String
    fun deletePlaylist(userDbPath: String, id: Long): String
    fun addTrackToPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
    fun removeTrackFromPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String
    fun getManualPlaylists(userDbPath: String): String

    // Favorite Artists
    fun addFavoriteArtist(userDbPath: String, artistId: Long, artistType: String): String
    fun removeFavoriteArtist(userDbPath: String, artistId: Long): String
    fun isFavoriteArtist(userDbPath: String, artistId: Long): String
    fun getFavoriteArtistIds(userDbPath: String, artistType: String): String

    // Playback History
    fun recordPlayback(userDbPath: String, trackId: Long): String
    fun getRecentlyPlayedIds(userDbPath: String, limit: Long): String
    fun getMostPlayedIds(userDbPath: String, limit: Long): String
}
