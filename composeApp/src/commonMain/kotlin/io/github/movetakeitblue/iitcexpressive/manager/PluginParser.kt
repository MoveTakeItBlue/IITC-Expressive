package io.github.movetakeitblue.iitcexpressive.manager

import io.github.movetakeitblue.iitcexpressive.model.Plugin

object PluginParser {
    private val HEADER_REGEX = Regex("==UserScript==\\s*([\\s\\S]*?)//\\s*==/UserScript==", RegexOption.MULTILINE)
    private val ENTRY_REGEX = Regex("//\\s*@(\\S+)\\s+(.*)$", RegexOption.MULTILINE)

    fun parseMeta(code: String, uidOverride: String? = null, isUser: Boolean = false): Plugin? {
        val headerMatch = HEADER_REGEX.find(code) ?: return null
        val header = headerMatch.groupValues[1]

        val metaMap = mutableMapOf<String, String>()
        for (match in ENTRY_REGEX.findAll(header)) {
            val key = match.groupValues[1].trim()
            var value = match.groupValues[2].trim()
            if (key.equals("name", ignoreCase = true)) {
                value = value.replace("IITC plugin: ", "").replace("IITC Plugin: ", "")
            }
            metaMap[key.lowercase()] = value
        }

        val name = metaMap["name"] ?: "Unknown plugin"
        val author = metaMap["author"] ?: ""
        val category = metaMap["category"] ?: "Misc"
        val version = metaMap["version"] ?: ""
        val description = metaMap["description"] ?: ""
        val homepage = metaMap["homepageurl"] ?: metaMap["homepage"] ?: ""
        val updateUrl = metaMap["updateurl"] ?: ""
        val downloadUrl = metaMap["downloadurl"] ?: ""
        val id = metaMap["id"] ?: ""

        val uid = uidOverride ?: run {
            val uniqueSuffix = when {
                homepage.isNotBlank() -> homepage
                updateUrl.isNotBlank() -> updateUrl
                downloadUrl.isNotBlank() -> downloadUrl
                id.isNotBlank() -> id
                else -> name
            }
            "$name+$uniqueSuffix"
        }

        return Plugin(
            uid = uid,
            name = name,
            author = author,
            category = category,
            version = version,
            description = description,
            homepageURL = homepage.ifBlank { null },
            updateURL = updateUrl.ifBlank { null },
            downloadURL = downloadUrl.ifBlank { null },
            user = isUser,
            code = code
        )
    }
}
