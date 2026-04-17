package com.radiogolha.mobile

import kotlinx.cinterop.*
import com.radiogolha.mobile.native.*

actual object RustCoreBridge {
    actual fun getCategoriesJson(dbPath: String): String = ""
    
    actual fun getHomeFeedJson(dbPath: String): String {
        val ptr = get_home_feed_json(dbPath)
        val result = ptr?.toKString() ?: ""
        radiogolha_free_string(ptr)
        return result
    }
    
    actual fun getTopTracksJson(dbPath: String): String = "[]"
    actual fun getSingersJson(dbPath: String): String = "[]"
    actual fun getMusiciansJson(dbPath: String): String = "[]"
    actual fun getArtistDetailJson(dbPath: String, artistId: Long): String = "{}"
    actual fun getProgramsByCategoryJson(dbPath: String, categoryId: Long): String = "[]"
    actual fun getProgramDetailJson(dbPath: String, programId: Long): String = "{}"
    actual fun getOrchestrasJson(dbPath: String): String = "[]"
    actual fun getProgramsByOrchestraJson(dbPath: String, orchestraId: Long): String = "[]"
    actual fun getProgramsByIdsJson(dbPath: String, idsJson: String): String = "[]"
    actual fun getDuetPairsConfigJson(dbPath: String): String = "[]"
    actual fun getProgramsByModeJson(dbPath: String, modeId: Long): String = "[]"
    actual fun getOrderedModesJson(dbPath: String): String = "[]"
    actual fun getConfigJson(dbPath: String, key: String): String = ""
    actual fun getSearchOptionsJson(dbPath: String): String = "{}"
    actual fun searchProgramsJson(dbPath: String, filtersJson: String): String = "{}"
    actual fun getDuetProgramsJson(dbPath: String, singer1: String, singer2: String): String = "[]"
    
    // User Data
    actual fun getAllPlaylists(userDbPath: String): String = "[]"
    actual fun getPlaylist(userDbPath: String, id: Long): String = "{}"
    actual fun createPlaylist(userDbPath: String, requestJson: String): String = ""
    actual fun renamePlaylist(userDbPath: String, id: Long, name: String): String = ""
    actual fun deletePlaylist(userDbPath: String, id: Long): String = ""
    actual fun addTrackToPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String = ""
    actual fun removeTrackFromPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String = ""
    actual fun getManualPlaylists(userDbPath: String): String = "[]"

    // Favorite Artists
    actual fun addFavoriteArtist(userDbPath: String, artistId: Long, artistType: String): String = ""
    actual fun removeFavoriteArtist(userDbPath: String, artistId: Long): String = ""
    actual fun isFavoriteArtist(userDbPath: String, artistId: Long): String = "false"
    actual fun getFavoriteArtistIds(userDbPath: String, artistType: String): String = "[]"

    // Playback History
    actual fun recordPlayback(userDbPath: String, trackId: Long): String = ""
    actual fun getRecentlyPlayedIds(userDbPath: String, limit: Long): String = "[]"
    actual fun getMostPlayedIds(userDbPath: String, limit: Long): String = "[]"
}
