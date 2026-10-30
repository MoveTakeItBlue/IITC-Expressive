package io.github.movetakeitblue.iitcexpressive.plugin.api

import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class CheckpointScore(
    val cpNumber: Int,
    val timeMillis: Long,
    val enlScore: Long,
    val resScore: Long
)

@Serializable
data class TopAgent(
    val rank: Int,
    val nick: String,
    val team: String
)

@Serializable
data class RegionScoreData(
    val regionName: String,
    val enlAvgScore: Long,
    val resAvgScore: Long,
    val checkpoints: List<CheckpointScore> = emptyList(),
    val topAgents: List<TopAgent> = emptyList()
) {
    val totalAvgScore: Long get() = (enlAvgScore + resAvgScore).coerceAtLeast(1L)
    val enlPercentage: Float get() = (enlAvgScore.toFloat() / totalAvgScore.toFloat()) * 100f
    val resPercentage: Float get() = (resAvgScore.toFloat() / totalAvgScore.toFloat()) * 100f

    val trailingFaction: String get() = if (resAvgScore < enlAvgScore) "Resistance" else "Enlightened"

    /**
     * Estimated score at final Checkpoint 35 based on past CPs and median calculation.
     */
    fun estimatedScore(faction: String): Long {
        if (checkpoints.isEmpty()) return if (faction == "Resistance") resAvgScore else enlAvgScore
        val isRes = faction.equals("Resistance", ignoreCase = true)
        val validScores = checkpoints.map { if (isRes) it.resScore else it.enlScore }.sorted()
        val median = if (validScores.isNotEmpty()) validScores[validScores.size / 2] else 0L

        val cpCount = checkpoints.size.coerceAtMost(35)
        val existingSum = checkpoints.sumOf { if (isRes) it.resScore else it.enlScore }
        val remainingCps = (35 - cpCount).coerceAtLeast(0)
        return (existingSum + (median * remainingCps)) / 35L
    }

    val requiredMuForTrailing: Long
        get() {
            val eRes = estimatedScore("Resistance")
            val eEnl = estimatedScore("Enlightened")
            return (kotlin.math.abs(eRes - eEnl) * 35L) + 1L
        }

    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        fun parseFromJson(jsonString: String): RegionScoreData? {
            return try {
                val root = json.parseToJsonElement(jsonString).jsonObject
                val resultObj = root["result"]?.jsonObject ?: root

                val regionName = resultObj["regionName"]?.jsonPrimitive?.contentOrNull ?: "Unknown Region"
                val gameScoreArr = resultObj["gameScore"]?.jsonArray
                val enlAvg = gameScoreArr?.getOrNull(0)?.jsonPrimitive?.longOrNull ?: 0L
                val resAvg = gameScoreArr?.getOrNull(1)?.jsonPrimitive?.longOrNull ?: 0L

                val cps = mutableListOf<CheckpointScore>()
                val cpArr = resultObj["checkpoints"]?.jsonArray
                val cycleStartTime = CycleTimesCalculator.getCycleStart()

                cpArr?.forEachIndexed { idx, elem ->
                    if (elem is JsonArray && elem.size >= 2) {
                        val enl = elem[0].jsonPrimitive.longOrNull ?: 0L
                        val res = elem[1].jsonPrimitive.longOrNull ?: 0L
                        val cpNum = idx + 1
                        val time = cycleStartTime + (cpNum * CycleTimesCalculator.CP_DURATION_MS)
                        cps.add(CheckpointScore(cpNum, time, enl, res))
                    }
                }

                val agents = mutableListOf<TopAgent>()
                val agentsArr = resultObj["topAgents"]?.jsonArray
                agentsArr?.forEachIndexed { idx, elem ->
                    if (elem is JsonObject) {
                        val nick = elem["nick"]?.jsonPrimitive?.contentOrNull ?: "Agent"
                        val team = elem["team"]?.jsonPrimitive?.contentOrNull ?: "NEUTRAL"
                        agents.add(TopAgent(idx + 1, nick, team))
                    }
                }

                RegionScoreData(
                    regionName = regionName,
                    enlAvgScore = enlAvg,
                    resAvgScore = resAvg,
                    checkpoints = cps,
                    topAgents = agents
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Calculates official Ingress septicycle checkpoint times and countdowns.
 * Each cycle is exactly 175 hours (35 checkpoints of 5 hours each).
 */
object CycleTimesCalculator {
    const val CP_COUNT = 35
    const val CP_DURATION_MS = 5 * 3600 * 1000L // 5 hours in ms
    const val CYCLE_DURATION_MS = 175 * 3600 * 1000L // 175 hours in ms

    fun getCycleStart(now: Long = PlatformUtils.currentTimeMillis()): Long {
        return (now / CYCLE_DURATION_MS) * CYCLE_DURATION_MS
    }

    fun getCycleEnd(now: Long = PlatformUtils.currentTimeMillis()): Long {
        return getCycleStart(now) + CYCLE_DURATION_MS
    }

    fun getLastCompletedCP(now: Long = PlatformUtils.currentTimeMillis()): Int {
        val start = getCycleStart(now)
        val elapsed = now - start
        return (elapsed / CP_DURATION_MS).toInt().coerceIn(0, CP_COUNT)
    }

    fun getNextCPEnd(now: Long = PlatformUtils.currentTimeMillis()): Long {
        val lastCP = getLastCompletedCP(now)
        val nextCP = (lastCP + 1).coerceAtMost(CP_COUNT)
        return getCycleStart(now) + (nextCP * CP_DURATION_MS)
    }

    fun getTimeToNextCPSeconds(now: Long = PlatformUtils.currentTimeMillis()): Long {
        val nextEnd = getNextCPEnd(now)
        return ((nextEnd - now) / 1000L).coerceAtLeast(0L)
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        val hStr = if (hours < 10) "0$hours" else "$hours"
        val mStr = if (minutes < 10) "0$minutes" else "$minutes"
        val sStr = if (seconds < 10) "0$seconds" else "$seconds"
        return "$hStr:$mStr:$sStr"
    }

    fun formatNumber(number: Long): String {
        val str = number.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            sb.append(str[i])
            count++
            if (count % 3 == 0 && i > 0 && str[i - 1] != '-') {
                sb.append(',')
            }
        }
        return sb.reverse().toString()
    }
}
