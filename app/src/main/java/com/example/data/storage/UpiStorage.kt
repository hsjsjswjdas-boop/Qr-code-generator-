package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.QrHistoryItem
import com.example.data.model.SavedProfile
import org.json.JSONArray
import org.json.JSONObject

class UpiStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("upi_qr_maker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROFILES = "saved_profiles"
        private const val KEY_HISTORY = "qr_history"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_QR_COLOR = "selected_qr_color"
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun getQrColorHex(): String {
        return prefs.getString(KEY_QR_COLOR, "#0E7A5F") ?: "#0E7A5F"
    }

    fun setQrColorHex(hex: String) {
        prefs.edit().putString(KEY_QR_COLOR, hex).apply()
    }

    fun getSavedProfiles(): List<SavedProfile> {
        val jsonStr = prefs.getString(KEY_PROFILES, null)
        if (jsonStr.isNullOrBlank()) {
            // Seed with sample initial profile based on user's prompt
            val defaultProfiles = listOf(
                SavedProfile(
                    label = "Poster Studio",
                    vpa = "posterstudio@okaxis",
                    payeeName = "Poster Studio"
                ),
                SavedProfile(
                    label = "ব্যক্তিগত (Personal)",
                    vpa = "myupi@upi",
                    payeeName = "My Name"
                )
            )
            saveProfiles(defaultProfiles)
            return defaultProfiles
        }

        val list = mutableListOf<SavedProfile>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SavedProfile(
                        id = obj.optString("id"),
                        label = obj.optString("label"),
                        vpa = obj.optString("vpa"),
                        payeeName = obj.optString("payeeName")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveProfiles(profiles: List<SavedProfile>) {
        try {
            val jsonArray = JSONArray()
            for (p in profiles) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("label", p.label)
                    put("vpa", p.vpa)
                    put("payeeName", p.payeeName)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_PROFILES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addProfile(profile: SavedProfile) {
        val current = getSavedProfiles().toMutableList()
        current.add(0, profile)
        saveProfiles(current)
    }

    fun deleteProfile(id: String) {
        val filtered = getSavedProfiles().filter { it.id != id }
        saveProfiles(filtered)
    }

    fun getHistory(): List<QrHistoryItem> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<QrHistoryItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    QrHistoryItem(
                        id = obj.optString("id"),
                        vpa = obj.optString("vpa"),
                        payeeName = obj.optString("payeeName"),
                        amount = obj.optString("amount"),
                        note = obj.optString("note"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addToHistory(item: QrHistoryItem) {
        val current = getHistory().toMutableList()
        // Deduplicate recent identical item if generated recently
        current.removeAll { it.vpa == item.vpa && it.amount == item.amount && it.note == item.note }
        current.add(0, item)
        // Keep last 30 items
        val trimmed = if (current.size > 30) current.take(30) else current

        try {
            val jsonArray = JSONArray()
            for (h in trimmed) {
                val obj = JSONObject().apply {
                    put("id", h.id)
                    put("vpa", h.vpa)
                    put("payeeName", h.payeeName)
                    put("amount", h.amount)
                    put("note", h.note)
                    put("timestamp", h.timestamp)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
