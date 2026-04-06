package com.radiogolha.mobile

object RustCoreBridge {
    init {
        System.loadLibrary("radiogolha_android")
    }

    external fun getCategoriesJson(dbPath: String): String
    external fun getHomeFeedJson(dbPath: String): String
    external fun getSingersJson(dbPath: String): String
}
