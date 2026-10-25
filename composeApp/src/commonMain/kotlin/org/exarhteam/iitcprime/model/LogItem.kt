package org.exarhteam.iitcprime.model

import kotlinx.serialization.Serializable

@Serializable
data class LogItem(
    val type: String = "log", // "log" | "info" | "warn" | "error"
    val message: String = "",
    val timestamp: String = "",
    val source: String = "webview",
    val id: String = ""
) {
    val time: String
        get() = if (timestamp.contains("T")) {
            timestamp.substringAfter('T').substringBefore('.')
        } else {
            timestamp
        }
}
