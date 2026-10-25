package org.exarhteam.iitcprime.storage

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class AppStorage(val settings: Settings = Settings()) {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return settings.getString(key, defaultValue)
    }

    fun setString(key: String, value: String) {
        settings.putString(key, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return settings.getLong(key, defaultValue)
    }

    fun setLong(key: String, value: Long) {
        settings.putLong(key, value)
    }

    inline fun <reified T> getObject(key: String): T? {
        val raw = getString(key, "")
        if (raw.isBlank()) return null
        return try {
            json.decodeFromString<T>(raw)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> setObject(key: String, value: T) {
        try {
            val raw = json.encodeToString(value)
            setString(key, raw)
        } catch (e: Exception) {
            println("Storage error: ${e.message}")
        }
    }

    fun remove(key: String) {
        settings.remove(key)
    }

    fun keys(): Set<String> {
        return settings.keys
    }
}
