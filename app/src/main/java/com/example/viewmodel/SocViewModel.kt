package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SocRepository
import com.example.model.*
import com.example.ui.theme.SocThemeMode
import com.example.util.SiemFilter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavTab(val label: String) {
    DASHBOARD("Dashboard"),
    INCIDENT_LAB("Incident Lab"),
    LEARNING_PATH("Learning Path"),
    MITRE_ATTACK("MITRE ATT&CK"),
    INVESTIGATIONS("Investigations"),
    REPORTS("Reports"),
    PROGRESS("Progress"),
    AI_MENTOR("AI Mentor"),
    SETTINGS("Settings")
}

enum class LabWorkflowStep(val label: String, val stepNum: Int) {
    LOG_EXPLORER("1. SIEM Logs", 1),
    EVIDENCE_BOOKMARKS("2. Evidence", 2),
    IOC_EXTRACTOR("3. IOCs", 3),
    ATTACK_TIMELINE("4. Timeline", 4),
    MITRE_MAPPER("5. MITRE Mapping", 5),
    CONTAINMENT("6. Response", 6),
    REPORT_SUBMIT("7. Submit Report", 7)
}

class SocViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SocRepository.getInstance(application)
    private val sharedPrefs = application.getSharedPreferences("soc_prefs", Context.MODE_PRIVATE)

    // Navigation
    private val _currentNavTab = MutableStateFlow(MainNavTab.DASHBOARD)
    val currentNavTab: StateFlow<MainNavTab> = _currentNavTab.asStateFlow()

    private val _currentLabStep = MutableStateFlow(LabWorkflowStep.LOG_EXPLORER)
    val currentLabStep: StateFlow<LabWorkflowStep> = _currentLabStep.asStateFlow()

    // SOC Theme Mode & Eye-Strain Reduction state (persisted)
    private val savedThemeOrdinal = sharedPrefs.getInt("soc_theme_mode", SocThemeMode.STANDARD_CYBER.ordinal)
    private val initialTheme = SocThemeMode.values().getOrElse(savedThemeOrdinal) { SocThemeMode.STANDARD_CYBER }
    private val _currentThemeMode = MutableStateFlow(initialTheme)
    val currentThemeMode: StateFlow<SocThemeMode> = _currentThemeMode.asStateFlow()

    // Domain data flows from Repository
    val incidents: StateFlow<List<Incident>> = repository.incidents
    val selectedIncidentId: StateFlow<String> = repository.selectedIncidentId
    val activeIncident: StateFlow<Incident?> = repository.activeIncident
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val savedReports: Flow<List<IncidentReport>> = repository.savedReports
    val mitreMatrix: StateFlow<List<MitreTechnique>> = repository.mitreMatrix
    val learningPaths: StateFlow<List<LearningTopic>> = repository.learningPaths
    val mentorMessages: StateFlow<List<MentorMessage>> = repository.mentorMessages

    // SIEM Log Filters
    val logSearchQuery = MutableStateFlow("")
    val logFilterSource = MutableStateFlow("ALL")
    val logFilterSeverity = MutableStateFlow<IncidentSeverity?>(null)
    val onlyBookmarkedLogs = MutableStateFlow(false)
    val hideNoiseLogs = MutableStateFlow(false)

    // Log Detail Inspector
    private val _selectedLogForDetail = MutableStateFlow<LogEvent?>(null)
    val selectedLogForDetail: StateFlow<LogEvent?> = _selectedLogForDetail.asStateFlow()

    // Selected Report for Detail View
    private val _selectedReportForDetail = MutableStateFlow<IncidentReport?>(null)
    val selectedReportForDetail: StateFlow<IncidentReport?> = _selectedReportForDetail.asStateFlow()

    // Last submission score result
    private val _lastSubmittedScore = MutableStateFlow<InvestigationScoreResult?>(null)
    val lastSubmittedScore: StateFlow<InvestigationScoreResult?> = _lastSubmittedScore.asStateFlow()

    private val _isMentorThinking = MutableStateFlow(false)
    val isMentorThinking: StateFlow<Boolean> = _isMentorThinking.asStateFlow()

    // Hint Level Tracker
    private val _currentHintLevel = MutableStateFlow(1)
    val currentHintLevel: StateFlow<Int> = _currentHintLevel.asStateFlow()

    // Filtered logs flow for the active incident
    @Suppress("UNCHECKED_CAST")
    val filteredLogs: StateFlow<List<LogEvent>> = combine(
        activeIncident,
        logSearchQuery,
        logFilterSource,
        logFilterSeverity,
        onlyBookmarkedLogs,
        hideNoiseLogs
    ) { args: Array<Any?> ->
        val incident = args[0] as? Incident ?: return@combine emptyList()
        val query = args[1] as String
        val source = args[2] as String
        val severity = args[3] as? IncidentSeverity
        val onlyBm = args[4] as Boolean
        val hideNoise = args[5] as Boolean

        SiemFilter.filterLogs(
            logs = incident.logs,
            query = query,
            selectedSource = source,
            selectedSeverity = severity,
            onlyBookmarked = onlyBm,
            bookmarkedIds = incident.bookmarkedLogIds,
            hideNoise = hideNoise
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: SocThemeMode) {
        _currentThemeMode.value = mode
        sharedPrefs.edit().putInt("soc_theme_mode", mode.ordinal).apply()
    }

    fun toggleNightWatchMode() {
        val nextMode = when (_currentThemeMode.value) {
            SocThemeMode.STANDARD_CYBER -> SocThemeMode.NIGHT_WATCH_REDUCED_STRAIN
            SocThemeMode.NIGHT_WATCH_REDUCED_STRAIN -> SocThemeMode.OLED_MIDNIGHT
            SocThemeMode.OLED_MIDNIGHT -> SocThemeMode.WARM_AMBER_TERMINAL
            SocThemeMode.WARM_AMBER_TERMINAL -> SocThemeMode.STANDARD_CYBER
        }
        setThemeMode(nextMode)
    }

    fun selectNavTab(tab: MainNavTab) {
        _currentNavTab.value = tab
    }

    fun selectLabStep(step: LabWorkflowStep) {
        _currentLabStep.value = step
    }

    fun selectIncident(id: String) {
        repository.selectIncident(id)
        _selectedLogForDetail.value = null
        _currentLabStep.value = LabWorkflowStep.LOG_EXPLORER
    }

    fun inspectLog(log: LogEvent?) {
        _selectedLogForDetail.value = log
    }

    fun inspectReport(report: IncidentReport?) {
        _selectedReportForDetail.value = report
    }

    fun toggleLogBookmark(logId: String) {
        val incId = selectedIncidentId.value
        repository.toggleLogBookmark(incId, logId)
    }

    fun setTriageVerdict(verdict: String) {
        val incId = selectedIncidentId.value
        repository.setTriageVerdict(incId, verdict)
    }

    fun addIoc(type: IocType, value: String, contextNote: String) {
        if (value.isBlank()) return
        val incId = selectedIncidentId.value
        val ioc = IocItem(
            id = "ioc-user-${System.currentTimeMillis()}",
            type = type,
            value = value.trim(),
            contextNote = contextNote.trim(),
            isTrueIoc = true
        )
        repository.addExtractedIoc(incId, ioc)
    }

    fun removeIoc(iocId: String) {
        val incId = selectedIncidentId.value
        repository.removeExtractedIoc(incId, iocId)
    }

    fun addTimelineMilestone(logId: String, title: String, description: String, phase: String) {
        if (title.isBlank()) return
        val inc = activeIncident.value ?: return
        val log = inc.logs.find { it.id == logId }
        val milestone = TimelineMilestone(
            id = "ms-user-${System.currentTimeMillis()}",
            logId = logId,
            timestamp = log?.timestamp ?: "2026-08-27T00:00:00Z",
            title = title,
            description = description,
            phase = phase,
            isCriticalStep = true
        )
        repository.addTimelineMilestone(inc.id, milestone)
    }

    fun removeTimelineMilestone(milestoneId: String) {
        val incId = selectedIncidentId.value
        repository.removeTimelineMilestone(incId, milestoneId)
    }

    fun toggleMitreTechnique(techniqueId: String) {
        val incId = selectedIncidentId.value
        repository.toggleMitreTechnique(incId, techniqueId)
    }

    fun toggleContainmentAction(actionId: String) {
        val incId = selectedIncidentId.value
        repository.toggleContainmentAction(incId, actionId)
    }

    fun updateExecutiveSummary(summary: String) {
        val incId = selectedIncidentId.value
        repository.updateExecutiveSummary(incId, summary)
    }

    fun updateInvestigativeNotes(notes: String) {
        val incId = selectedIncidentId.value
        repository.updateInvestigativeNotes(incId, notes)
    }

    fun assessSeverity(severity: IncidentSeverity) {
        val incId = selectedIncidentId.value
        repository.assessSeverity(incId, severity)
    }

    fun submitInvestigation() {
        val incId = selectedIncidentId.value
        viewModelScope.launch {
            val result = repository.submitInvestigation(incId)
            if (result != null) {
                _lastSubmittedScore.value = result.first
                _selectedReportForDetail.value = result.second
            }
        }
    }

    fun dismissScoreDialog() {
        _lastSubmittedScore.value = null
    }

    fun requestProgressiveHint() {
        val inc = activeIncident.value
        val nextLevel = (_currentHintLevel.value % 3) + 1
        _currentHintLevel.value = nextLevel
        sendMentorMessage("Give me a progressive hint (Tier $nextLevel)", inc?.logs?.firstOrNull(), nextLevel)
    }

    fun sendMentorMessage(text: String, logContext: LogEvent? = null, hintLevel: Int = 1) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isMentorThinking.value = true
            repository.sendMentorMessage(text, logContext ?: _selectedLogForDetail.value, hintLevel)
            _isMentorThinking.value = false
        }
    }

    fun toggleTopicComplete(topicId: String) {
        repository.toggleLearningTopicComplete(topicId)
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    /**
     * Calculates user completion status for MITRE ATT&CK mapping tasks.
     * Returns Pair(correctlyMappedCount, totalTargetCount)
     */
    fun getMitreMappingProgress(incident: Incident?): Pair<Int, Int> {
        if (incident == null) return Pair(0, 1)
        val targetIds = incident.correctMitreTechniques.map { it.id }.toSet()
        val userIds = incident.userSelectedMitreIds
        val correctlyMapped = userIds.intersect(targetIds).size
        val targetCount = if (targetIds.isEmpty()) 1 else targetIds.size
        return Pair(correctlyMapped, targetCount)
    }

    /**
     * Calculates global breakdown of MITRE ATT&CK tactics mapped across all incidents.
     */
    fun getGlobalTacticCoverage(): Map<String, Float> {
        val allMatrix = mitreMatrix.value
        val allIncidents = incidents.value
        val allMappedTechniques = allIncidents.flatMap { it.userSelectedMitreIds }.toSet()

        val tactics = listOf(
            "Initial Access",
            "Execution",
            "Persistence",
            "Privilege Escalation",
            "Defense Evasion",
            "Credential Access",
            "Collection",
            "Exfiltration"
        )

        return tactics.associateWith { tactic ->
            val totalInTactic = allMatrix.count { it.tactic.equals(tactic, ignoreCase = true) }
            val mappedInTactic = allMatrix.count { it.tactic.equals(tactic, ignoreCase = true) && allMappedTechniques.contains(it.id) }
            if (totalInTactic == 0) 0.5f else (mappedInTactic.toFloat() / totalInTactic.toFloat()).coerceIn(0f, 1f)
        }
    }
}
