package com.example.util

import com.example.model.IncidentSeverity
import com.example.model.LogEvent

object SiemFilter {

    fun filterLogs(
        logs: List<LogEvent>,
        query: String,
        selectedSource: String? = null,
        selectedSeverity: IncidentSeverity? = null,
        onlyBookmarked: Boolean = false,
        bookmarkedIds: Set<String> = emptySet(),
        hideNoise: Boolean = false
    ): List<LogEvent> {
        val trimmed = query.trim().lowercase()

        return logs.filter { log ->
            // Filter by bookmarked
            if (onlyBookmarked && !bookmarkedIds.contains(log.id)) {
                return@filter false
            }

            // Filter noise
            if (hideNoise && !log.isMalicious) {
                return@filter false
            }

            // Filter by source
            if (!selectedSource.isNullOrBlank() && selectedSource != "ALL" && !log.source.equals(selectedSource, ignoreCase = true)) {
                return@filter false
            }

            // Filter by severity
            if (selectedSeverity != null && log.severity != selectedSeverity) {
                return@filter false
            }

            // Filter by query text or structured field
            if (trimmed.isNotBlank()) {
                val matchesText = log.rawPayload.lowercase().contains(trimmed) ||
                        log.eventName.lowercase().contains(trimmed) ||
                        log.actor.lowercase().contains(trimmed) ||
                        log.sourceIp.lowercase().contains(trimmed) ||
                        log.destinationIp.lowercase().contains(trimmed) ||
                        log.parsedFields.values.any { it.lowercase().contains(trimmed) }

                // Check field queries like source:cloudtrail or ip:185.
                val matchesFieldQuery = if (trimmed.contains(":")) {
                    val parts = trimmed.split(":", limit = 2)
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    when (key) {
                        "ip", "sourceip" -> log.sourceIp.lowercase().contains(value) || log.destinationIp.lowercase().contains(value)
                        "actor", "user" -> log.actor.lowercase().contains(value)
                        "event", "action" -> log.eventName.lowercase().contains(value)
                        "source" -> log.source.lowercase().contains(value)
                        else -> log.parsedFields[key]?.lowercase()?.contains(value) == true
                    }
                } else false

                if (!matchesText && !matchesFieldQuery) {
                    return@filter false
                }
            }

            true
        }
    }
}
