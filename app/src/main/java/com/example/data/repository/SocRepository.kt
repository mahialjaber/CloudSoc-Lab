package com.example.data.repository

import android.content.Context
import com.example.data.ai.SocMentorService
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.local.SavedReportEntity
import com.example.data.local.UserProfileEntity
import com.example.data.sample.SampleIncidents
import com.example.data.sample.SampleLearningAndMitre
import com.example.model.*
import com.example.util.ScoringEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SocRepository private constructor(context: Context) {

    private val dao: AppDao = AppDatabase.getDatabase(context).appDao()
    private val mentorService: SocMentorService = SocMentorService()
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory incidents state initialized with realistic datasets
    private val _incidents = MutableStateFlow<List<Incident>>(SampleIncidents.getSampleIncidents())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    // Selected active incident
    private val _selectedIncidentId = MutableStateFlow<String>("INC-AWS-8821")
    val selectedIncidentId: StateFlow<String> = _selectedIncidentId.asStateFlow()

    val activeIncident: StateFlow<Incident?> = combine(_incidents, _selectedIncidentId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(scope, SharingStarted.Eagerly, _incidents.value.firstOrNull())

    // MITRE Matrix & Learning Paths
    val mitreMatrix: StateFlow<List<MitreTechnique>> = MutableStateFlow(SampleLearningAndMitre.getMitreMatrix()).asStateFlow()
    val learningPaths: MutableStateFlow<List<LearningTopic>> = MutableStateFlow(SampleLearningAndMitre.getLearningPaths())

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Saved Reports (from Room)
    val savedReports: Flow<List<IncidentReport>> = dao.getAllReports().map { list ->
        list.map { entity ->
            IncidentReport(
                reportId = entity.reportId,
                incidentId = entity.incidentId,
                incidentTitle = entity.incidentTitle,
                category = entity.category,
                analystName = entity.analystName,
                submissionTimestamp = entity.submissionTimestamp,
                severityAssessed = entity.severityAssessed,
                executiveSummary = entity.executiveSummary,
                attackTimelineSummary = entity.attackTimelineSummaryJson.split(";;;").filter { it.isNotBlank() },
                identifiedIocs = entity.identifiedIocsJson.split(";;;").filter { it.isNotBlank() },
                mitreTacticsCovered = entity.mitreTacticsCoveredJson.split(";;;").filter { it.isNotBlank() },
                containmentExecuted = entity.containmentExecutedJson.split(";;;").filter { it.isNotBlank() },
                rootCauseFindings = entity.rootCauseFindings,
                lessonsLearned = entity.lessonsLearned,
                score = entity.score,
                grade = entity.grade
            )
        }
    }

    // Mentor Messages
    private val _mentorMessages = MutableStateFlow<List<MentorMessage>>(
        listOf(
            MentorMessage(
                id = "m-init",
                sender = "MENTOR",
                text = "Welcome to the SOC, Analyst. I'm Alex Vance, Lead Detection Engineer. I'm here to assist your investigations. You can ask for progressive hints, have me explain tricky log payloads, or challenge your attack hypotheses.",
                suggestedActions = listOf("Explain active incident", "Give Hint 1", "Review my hypothesis", "Detection Query Help")
            )
        )
    )
    val mentorMessages: StateFlow<List<MentorMessage>> = _mentorMessages.asStateFlow()

    init {
        // Observe Room user profile
        scope.launch {
            dao.getUserProfile().collect { entity ->
                if (entity != null) {
                    _userProfile.value = UserProfile(
                        analystName = entity.analystName,
                        title = entity.title,
                        level = entity.level,
                        xp = entity.xp,
                        nextLevelXp = entity.nextLevelXp,
                        streakDays = entity.streakDays,
                        totalInvestigated = entity.totalInvestigated,
                        totalSolved = entity.totalSolved,
                        overallReadinessScore = entity.overallReadinessScore,
                        detectionAccuracy = entity.detectionAccuracy,
                        evidenceAccuracy = entity.evidenceAccuracy,
                        iocAccuracy = entity.iocAccuracy,
                        mitreAccuracy = entity.mitreAccuracy,
                        containmentAccuracy = entity.containmentAccuracy,
                        reasoningAccuracy = entity.reasoningAccuracy,
                        weakestSkills = entity.weakestSkillsJson.replace("[", "").replace("]", "").replace("\"", "").split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                } else {
                    // Seed initial profile in Room
                    dao.saveUserProfile(
                        UserProfileEntity(
                            id = 1,
                            analystName = "Junior SOC Analyst",
                            title = "SOC Analyst Tier 1",
                            level = 1,
                            xp = 350,
                            nextLevelXp = 1000,
                            streakDays = 3,
                            totalInvestigated = 1,
                            totalSolved = 1,
                            overallReadinessScore = 65
                        )
                    )
                }
            }
        }
    }

    fun selectIncident(id: String) {
        _selectedIncidentId.value = id
    }

    fun toggleLogBookmark(incidentId: String, logId: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    val updated = if (inc.bookmarkedLogIds.contains(logId)) {
                        inc.bookmarkedLogIds - logId
                    } else {
                        inc.bookmarkedLogIds + logId
                    }
                    inc.copy(bookmarkedLogIds = updated, status = if (inc.status == IncidentStatus.UNTOUCHED) IncidentStatus.IN_PROGRESS else inc.status)
                } else inc
            }
        }
    }

    fun setTriageVerdict(incidentId: String, verdict: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userTriageVerdict = verdict, isTriageDone = true, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun addExtractedIoc(incidentId: String, ioc: IocItem) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    val updated = inc.userExtractedIocs.filter { it.value != ioc.value } + ioc
                    inc.copy(userExtractedIocs = updated, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun removeExtractedIoc(incidentId: String, iocId: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userExtractedIocs = inc.userExtractedIocs.filter { it.id != iocId })
                } else inc
            }
        }
    }

    fun addTimelineMilestone(incidentId: String, milestone: TimelineMilestone) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    val updated = inc.userTimeline.filter { it.id != milestone.id } + milestone
                    inc.copy(userTimeline = updated.sortedBy { it.timestamp }, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun removeTimelineMilestone(incidentId: String, milestoneId: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userTimeline = inc.userTimeline.filter { it.id != milestoneId })
                } else inc
            }
        }
    }

    fun toggleMitreTechnique(incidentId: String, techniqueId: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    val updated = if (inc.userSelectedMitreIds.contains(techniqueId)) {
                        inc.userSelectedMitreIds - techniqueId
                    } else {
                        inc.userSelectedMitreIds + techniqueId
                    }
                    inc.copy(userSelectedMitreIds = updated, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun toggleContainmentAction(incidentId: String, actionId: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    val updated = if (inc.userSelectedContainmentIds.contains(actionId)) {
                        inc.userSelectedContainmentIds - actionId
                    } else {
                        inc.userSelectedContainmentIds + actionId
                    }
                    inc.copy(userSelectedContainmentIds = updated, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun updateExecutiveSummary(incidentId: String, summary: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userExecutiveSummary = summary, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun updateInvestigativeNotes(incidentId: String, notes: String) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userInvestigativeNotes = notes, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    fun assessSeverity(incidentId: String, severity: IncidentSeverity) {
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(userAssessedSeverity = severity, status = IncidentStatus.IN_PROGRESS)
                } else inc
            }
        }
    }

    suspend fun submitInvestigation(incidentId: String): Pair<InvestigationScoreResult, IncidentReport>? {
        val incident = _incidents.value.find { it.id == incidentId } ?: return null
        val (scoreResult, report) = ScoringEngine.evaluateInvestigation(incident)

        // Update incident status in memory
        _incidents.update { list ->
            list.map { inc ->
                if (inc.id == incidentId) {
                    inc.copy(
                        status = IncidentStatus.SOLVED,
                        finalScore = scoreResult
                    )
                } else inc
            }
        }

        // Save report to Room DB
        val reportEntity = SavedReportEntity(
            reportId = report.reportId,
            incidentId = report.incidentId,
            incidentTitle = report.incidentTitle,
            category = report.category,
            analystName = report.analystName,
            submissionTimestamp = report.submissionTimestamp,
            severityAssessed = report.severityAssessed,
            executiveSummary = report.executiveSummary,
            attackTimelineSummaryJson = report.attackTimelineSummary.joinToString(";;;"),
            identifiedIocsJson = report.identifiedIocs.joinToString(";;;"),
            mitreTacticsCoveredJson = report.mitreTacticsCovered.joinToString(";;;"),
            containmentExecutedJson = report.containmentExecuted.joinToString(";;;"),
            rootCauseFindings = report.rootCauseFindings,
            lessonsLearned = report.lessonsLearned,
            score = report.score,
            grade = report.grade
        )
        dao.insertReport(reportEntity)

        // Update User Profile stats & XP
        val currentProfile = _userProfile.value
        val newXp = currentProfile.xp + scoreResult.earnedXp
        val newLevel = (newXp / 1000) + 1
        val levelTitle = when (newLevel) {
            1 -> "SOC Analyst Tier 1"
            2 -> "Incident Responder Tier 2"
            3 -> "Senior Cloud Security Specialist"
            else -> "Lead Detection Engineer"
        }

        val updatedProfile = currentProfile.copy(
            level = newLevel,
            title = levelTitle,
            xp = newXp,
            totalSolved = currentProfile.totalSolved + 1,
            totalInvestigated = currentProfile.totalInvestigated + 1,
            overallReadinessScore = ((currentProfile.overallReadinessScore * 0.7) + (scoreResult.overallScore * 0.3)).toInt().coerceIn(0, 100),
            weakestSkills = scoreResult.weaknessesDetected
        )
        _userProfile.value = updatedProfile

        dao.saveUserProfile(
            UserProfileEntity(
                id = 1,
                analystName = updatedProfile.analystName,
                title = updatedProfile.title,
                level = updatedProfile.level,
                xp = updatedProfile.xp,
                nextLevelXp = (newLevel) * 1000,
                streakDays = updatedProfile.streakDays,
                totalInvestigated = updatedProfile.totalInvestigated,
                totalSolved = updatedProfile.totalSolved,
                overallReadinessScore = updatedProfile.overallReadinessScore,
                detectionAccuracy = ((currentProfile.detectionAccuracy * 0.7) + (scoreResult.detectionScore * 5 * 0.3)).toInt(),
                weakestSkillsJson = scoreResult.weaknessesDetected.joinToString(",", "[", "]") { "\"$it\"" }
            )
        )

        return Pair(scoreResult, report)
    }

    suspend fun sendMentorMessage(userText: String, logContext: LogEvent? = null, hintLevel: Int = 1) {
        val userMsg = MentorMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            text = userText,
            timestamp = System.currentTimeMillis()
        )
        _mentorMessages.update { it + userMsg }

        val mentorReplyText = mentorService.askMentor(
            userPrompt = userText,
            currentIncident = activeIncident.value,
            selectedLog = logContext,
            hintLevel = hintLevel
        )

        val mentorMsg = MentorMessage(
            id = UUID.randomUUID().toString(),
            sender = "MENTOR",
            text = mentorReplyText,
            timestamp = System.currentTimeMillis(),
            suggestedActions = listOf("Explain log", "Give progressive hint", "Review my hypothesis", "MITRE query help")
        )
        _mentorMessages.update { it + mentorMsg }
    }

    fun toggleLearningTopicComplete(topicId: String) {
        learningPaths.update { list ->
            list.map { topic ->
                if (topic.id == topicId) {
                    topic.copy(isCompleted = !topic.isCompleted)
                } else topic
            }
        }
    }

    suspend fun resetAllData() {
        dao.clearAllProgress()
        _incidents.value = SampleIncidents.getSampleIncidents()
        _userProfile.value = UserProfile(
            analystName = "SOC Analyst",
            title = "SOC Analyst Tier 1",
            level = 1,
            xp = 250,
            overallReadinessScore = 55
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: SocRepository? = null

        fun getInstance(context: Context): SocRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SocRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
