package com.radiogolha.mobile

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

actual fun loadCategories(): List<CategoryItem> {
    val context = AndroidAppContext.require()
    val dbFile = File(context.filesDir, "golha_database.db")

    if (!dbFile.exists()) {
        context.assets.open("golha_database.db").use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    val payload = RustCoreBridge.getCategoriesJson(dbFile.absolutePath)
    if (payload.trim().startsWith("{")) {
        val error = JSONObject(payload).optString("error")
        if (error.isNotBlank()) {
            throw IllegalStateException(error)
        }
    }

    val rows = JSONArray(payload)
    return buildList {
        for (index in 0 until rows.length()) {
            val item = rows.getJSONObject(index)
            add(
                CategoryItem(
                    id = item.getLong("id"),
                    titleFa = item.getString("title_fa"),
                )
            )
        }
    }
}
