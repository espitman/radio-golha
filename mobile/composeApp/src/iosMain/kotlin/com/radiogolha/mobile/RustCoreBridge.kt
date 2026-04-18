@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.radiogolha.mobile

import kotlinx.cinterop.*
import com.radiogolha.mobile.native.*

actual object RustCoreBridge {
    private fun callNative(block: () -> CPointer<ByteVar>?): String {
        val ptr = block()
        val result = ptr?.toKString() ?: ""
        if (ptr != null) {
            radiogolha_free_string(ptr)
        }
        return result
    }

    actual fun getCategoriesJson(dbPath: String): String = ""
    
    actual fun getHomeFeedJson(dbPath: String): String = callNative { 
        get_home_feed_json(dbPath) 
    }
    
    actual fun getTopTracksJson(dbPath: String): String = callNative {
        get_top_tracks_json(dbPath)
    }

    actual fun getSingersJson(dbPath: String): String = callNative {
        get_singers_json(dbPath)
    }

    actual fun getMusiciansJson(dbPath: String): String = callNative {
        get_musicians_json(dbPath)
    }

    actual fun getArtistDetailJson(dbPath: String, artistId: Long): String = callNative {
        get_artist_detail_json(dbPath, artistId)
    }

    actual fun getProgramsByCategoryJson(dbPath: String, categoryId: Long): String = callNative {
        get_programs_by_category_json(dbPath, categoryId)
    }

    actual fun getProgramDetailJson(dbPath: String, programId: Long): String = callNative {
        get_program_detail_json(dbPath, programId)
    }

    actual fun getOrchestrasJson(dbPath: String): String = callNative {
        get_orchestras_json(dbPath)
    }

    actual fun getProgramsByOrchestraJson(dbPath: String, orchestraId: Long): String = callNative {
        get_programs_by_orchestra_json(dbPath, orchestraId)
    }

    actual fun getProgramsByIdsJson(dbPath: String, idsJson: String): String = callNative {
        get_programs_by_ids_json(dbPath, idsJson)
    }

    actual fun getDuetPairsConfigJson(dbPath: String): String = callNative {
        get_duet_pairs_config_json(dbPath)
    }

    actual fun getProgramsByModeJson(dbPath: String, modeId: Long): String = callNative {
        get_programs_by_mode_json(dbPath, modeId)
    }

    actual fun getOrderedModesJson(dbPath: String): String = callNative {
        get_ordered_modes_json(dbPath)
    }

    actual fun getConfigJson(dbPath: String, key: String): String = callNative {
        get_config_json(dbPath, key)
    }

    actual fun getSearchOptionsJson(dbPath: String): String = callNative {
        get_search_options_json(dbPath)
    }

    actual fun searchProgramsJson(dbPath: String, filtersJson: String): String = callNative {
        search_programs_json(dbPath, filtersJson)
    }

    actual fun getDuetProgramsJson(dbPath: String, singer1: String, singer2: String): String = callNative {
        get_duet_programs_json(dbPath, singer1, singer2)
    }
    
    // User Data - Note: These might need a separate UserDataStore implementaton in Rust
    // For now returning defaults to avoid crashes
    actual fun getAllPlaylists(userDbPath: String): String = "[]"
    actual fun getPlaylist(userDbPath: String, id: Long): String = "{}"
    actual fun createPlaylist(userDbPath: String, requestJson: String): String = ""
    actual fun renamePlaylist(userDbPath: String, id: Long, name: String): String = ""
    actual fun deletePlaylist(userDbPath: String, id: Long): String = ""
    actual fun addTrackToPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String = ""
    actual fun removeTrackFromPlaylist(userDbPath: String, playlistId: Long, trackId: Long): String = ""
    actual fun getManualPlaylists(userDbPath: String): String = "[]"

    actual fun addFavoriteArtist(userDbPath: String, artistId: Long, artistType: String): String = ""
    actual fun removeFavoriteArtist(userDbPath: String, artistId: Long): String = ""
    actual fun isFavoriteArtist(userDbPath: String, artistId: Long): String = "false"
    actual fun getFavoriteArtistIds(userDbPath: String, artistType: String): String = "[]"

    actual fun recordPlayback(userDbPath: String, trackId: Long): String = ""
    actual fun getRecentlyPlayedIds(userDbPath: String, limit: Long): String = "[]"
    actual fun getMostPlayedIds(userDbPath: String, limit: Long): String = "[]"
}
