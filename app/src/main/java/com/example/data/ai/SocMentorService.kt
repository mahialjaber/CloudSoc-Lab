package com.example.data.ai

import com.example.BuildConfig
import com.example.model.Incident
import com.example.model.LogEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiRestApi {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class SocMentorService {

    private val api: GeminiRestApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(GeminiRestApi::class.java)
    }

    suspend fun askMentor(
        userPrompt: String,
        currentIncident: Incident?,
        selectedLog: LogEvent? = null,
        hintLevel: Int = 1
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are Alex Vance, a Senior Principal SOC Lead & Detection Engineer at CloudSOC Lab.
                    Your role is to mentor junior analysts.
                    GUIDELINES:
                    1. Do NOT immediately give away the final answer or full IOC list unless the user asks "Give me the solution".
                    2. Guide with investigative questions, progressive hints, and log analysis advice.
                    3. Explain AWS/Sysmon/Linux security concepts clearly (e.g. why an IP is suspicious, what IMDSv2 does, what Sysmon Event 10 means).
                    4. When reviewing user conclusions, challenge weak reasoning and praise solid evidence correlation.
                    Current Active Incident Context:
                    Title: ${currentIncident?.title ?: "General SOC Mentorship"}
                    Category: ${currentIncident?.category?.displayName ?: "General"}
                    Target: ${currentIncident?.targetOrganization ?: "N/A"}
                    Inspected Log: ${selectedLog?.rawPayload ?: "None"}
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = userPrompt))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val response = api.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                // Fallback to built-in offline senior SOC mentor engine
            }
        }

        // Built-in intelligent offline SOC mentor engine
        generateOfflineMentorResponse(userPrompt, currentIncident, selectedLog, hintLevel)
    }

    private fun generateOfflineMentorResponse(
        prompt: String,
        incident: Incident?,
        selectedLog: LogEvent?,
        hintLevel: Int
    ): String {
        val lowerPrompt = prompt.lowercase()

        if (lowerPrompt.contains("hint") || lowerPrompt.contains("clue") || lowerPrompt.contains("stuck")) {
            if (incident == null) return "Select an active case in the Incident Lab first, and I will guide your evidence correlation."
            return when (hintLevel) {
                1 -> "🔍 [Progressive Hint Tier 1 - Directional]: ${incident.hints.getOrElse(0) { "Start by filtering out baseline noise logs. Look for unfamiliar source IPs or privileged API actions." }}"
                2 -> "🔎 [Progressive Hint Tier 2 - Specific Event]: ${incident.hints.getOrElse(1) { "Examine the chronological transition between Initial Access and Privilege Escalation in the log stream." }}"
                else -> "🚨 [Progressive Hint Tier 3 - Correlated Evidence]: ${incident.hints.getOrElse(2) { "Check the exact command line arguments or requestParameters in the critical log entries." }}"
            }
        }

        if (lowerPrompt.contains("explain log") || lowerPrompt.contains("explain this") || selectedLog != null) {
            val log = selectedLog ?: incident?.logs?.firstOrNull()
            if (log != null) {
                return """
                    📋 [Log Forensic Breakdown by Senior Mentor]:
                    • Source: ${log.source} | Event: ${log.eventName}
                    • Actor / User: ${log.actor}
                    • Source IP: ${log.sourceIp}
                    
                    💡 Key Analyst Insight:
                    ${if (log.isMalicious) {
                        "This log represents a critical attack milestone in the '${log.attackPhase}' phase. Notice the parameters: ${log.parsedFields.entries.joinToString(", ") { "${it.key}: ${it.value}" }}. In a real SIEM, this anomaly should be bookmarked and correlated to preceding auth logs."
                    } else {
                        "This appears to be normal background baseline activity (${log.attackPhase}). Notice how the caller is an automated internal service. Be careful not to treat baseline noise as a false positive alert."
                    }}
                """.trimIndent()
            }
        }

        if (lowerPrompt.contains("solution") || lowerPrompt.contains("reveal") || lowerPrompt.contains("answer")) {
            if (incident != null) {
                return """
                    🎯 [Senior Mentor Debrief & Root Cause]:
                    ${incident.rootCauseSummary}
                    
                    Key IOCs:
                    ${incident.correctIocs.joinToString("\n") { "• [${it.type.label}] ${it.value} - ${it.contextNote}" }}
                    
                    MITRE ATT&CK Mapping:
                    ${incident.correctMitreTechniques.joinToString("\n") { "• ${it.id} (${it.name}) - ${it.tactic}" }}
                """.trimIndent()
            }
        }

        if (lowerPrompt.contains("review") || lowerPrompt.contains("conclusion") || lowerPrompt.contains("hypothesis")) {
            return """
                🧐 [Investigative Hypothesis Review]:
                Let's test your conclusion against standard SOC triage methodology:
                1. Have you verified the initial entry vector with concrete timestamps?
                2. Do your extracted IOCs distinguish between adversary assets and benign victim infrastructure?
                3. Does your MITRE ATT&CK mapping reflect the actual attack actions in the logs?
                
                Keep forming your timeline in the Incident Lab workspace, then submit your formal report for full rubric scoring!
            """.trimIndent()
        }

        return """
            Greetings, Analyst. I'm Alex Vance, Lead Detection Engineer.
            
            How can I assist your investigation?
            • Ask me to "Explain this log" to dissect tricky payload fields.
            • Request a "Progressive Hint" if you're hunting for the next pivot.
            • Ask me to "Challenge my hypothesis" before you submit your report.
            • Inquire about specific MITRE techniques (e.g. T1078, T1059, T1530).
        """.trimIndent()
    }
}
