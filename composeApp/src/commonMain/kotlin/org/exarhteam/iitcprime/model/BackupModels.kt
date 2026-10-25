package org.exarhteam.iitcprime.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class BackupContent(
    val data: JsonObject? = null,
    val external_plugins: Map<String, Map<String, String>> = emptyMap()
)

data class BackupInspection(
    val hasSettings: Boolean,
    val hasData: Boolean,
    val hasExternal: Boolean
)
