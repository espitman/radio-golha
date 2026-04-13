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
}
