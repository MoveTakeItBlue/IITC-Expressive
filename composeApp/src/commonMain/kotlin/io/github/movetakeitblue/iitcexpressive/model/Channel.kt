package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
enum class Channel(val key: String, val defaultHost: String) {
    RELEASE("release", "https://iitc.app/build/release"),
    BETA("beta", "https://iitc.app/build/beta"),
    CUSTOM("custom", "http://localhost:8000");

    companion object {
        fun fromKey(key: String): Channel = entries.find { it.key == key.lowercase() } ?: RELEASE
    }
}
