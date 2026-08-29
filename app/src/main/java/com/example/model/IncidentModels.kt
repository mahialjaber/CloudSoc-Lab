package com.example.model

enum class IncidentCategory(val displayName: String, val iconTag: String) {
    AWS_IAM("AWS IAM & PrivEsc", "iam"),
    S3_STORAGE("S3 Data Exposure", "s3"),
    WINDOWS_SYSMON("Windows & Sysmon", "sysmon"),
    LINUX_AUDITD("Linux Compromise", "linux"),
    CREDENTIAL_ACCESS("Credential Abuse", "creds"),
    CLOUD_TRAIL("CloudTrail Anomaly", "cloudtrail"),
    MULTI_STAGE("Multi-Stage Cloud Attack", "multistage"),
    RANSOMWARE("Ransomware & Lateral Move", "ransom"),
    EC2_GUARDDUTY("EC2 & GuardDuty", "ec2")
}

enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Beginner (L1 SOC)"),
    INTERMEDIATE("Intermediate (L2 IR)"),
    ADVANCED("Advanced (Cloud Specialist)"),
    EXPERT("Expert (Lead Detection Eng)")
}

enum class IncidentSeverity(val displayName: String) {
    INFORMATIONAL("Informational"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class IncidentStatus {
    UNTOUCHED,
    IN_PROGRESS,
    REPORT_SUBMITTED,
    SOLVED
}

enum class IocType(val label: String) {
    IP_ADDRESS("IPv4/IPv6"),
    DOMAIN("Domain / Host"),
    FILE_HASH("SHA256 / MD5"),
    IAM_USER("IAM User / Principal"),
    ROLE_ARN("Role / Resource ARN"),
    PROCESS_NAME("Process / Command")
}

data class LogEvent(
    val id: String,
    val timestamp: String,
    val source: String, // CloudTrail, Sysmon, Linux auditd, VPC Flow, Okta, GuardDuty
    val eventName: String,
    val actor: String,
    val sourceIp: String = "-",
    val destinationIp: String = "-",
    val severity: IncidentSeverity = IncidentSeverity.INFORMATIONAL,
    val rawPayload: String,
    val parsedFields: Map<String, String> = emptyMap(),
    val isMalicious: Boolean = false,
    val attackPhase: String = "Normal Baseline"
)

data class IocItem(
    val id: String,
    val type: IocType,
    val value: String,
    val contextNote: String = "",
    val isTrueIoc: Boolean = false
)

data class TimelineMilestone(
    val id: String,
    val logId: String,
    val timestamp: String,
    val title: String,
    val description: String,
    val phase: String,
    val isCriticalStep: Boolean = false
)

data class MitreTechnique(
    val id: String, // e.g. T1078.004
    val name: String,
    val tactic: String, // e.g. Initial Access, Privilege Escalation
    val description: String,
    val cloudDetectionQuery: String = "",
    val mitigation: String = ""
)

data class ContainmentAction(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val isRecommended: Boolean,
    val impactWarning: String = ""
)

data class Incident(
    val id: String,
    val title: String,
    val category: IncidentCategory,
    val difficulty: DifficultyLevel,
    val initialSeverity: IncidentSeverity,
    val targetOrganization: String,
    val assetScope: String,
    val alertHeadline: String,
    val alertSource: String,
    val alertDescription: String,
    val logs: List<LogEvent>,
    val correctIocs: List<IocItem>,
    val correctMitreTechniques: List<MitreTechnique>,
    val correctMilestones: List<TimelineMilestone>,
    val availableContainmentActions: List<ContainmentAction>,
    val correctContainmentIds: List<String>,
    val rootCauseSummary: String,
    val hints: List<String>,
    val recommendedLearningTopics: List<String>,
    val xpReward: Int = 300,
    var status: IncidentStatus = IncidentStatus.UNTOUCHED,
    // Active analyst working state
    var isTriageDone: Boolean = false,
    var userTriageVerdict: String = "PENDING", // TRUE_POSITIVE, FALSE_POSITIVE, BENIGN
    var bookmarkedLogIds: Set<String> = emptySet(),
    var userExtractedIocs: List<IocItem> = emptyList(),
    var userTimeline: List<TimelineMilestone> = emptyList(),
    var userSelectedMitreIds: Set<String> = emptySet(),
    var userAssessedSeverity: IncidentSeverity? = null,
    var userSelectedContainmentIds: Set<String> = emptySet(),
    var userExecutiveSummary: String = "",
    var userRootCauseHypothesis: String = "",
    var userInvestigativeNotes: String = "",
    var finalScore: InvestigationScoreResult? = null
)

data class InvestigationScoreResult(
    val overallScore: Int, // 0 - 100
    val grade: String, // S, A, B, C, F
    val detectionScore: Int, // max 20
    val evidenceScore: Int, // max 15
    val iocScore: Int, // max 15
    val timelineScore: Int, // max 15
    val mitreScore: Int, // max 15
    val containmentScore: Int, // max 10
    val reasoningScore: Int, // max 10
    val feedbackItems: List<String>,
    val weaknessesDetected: List<String>,
    val earnedXp: Int,
    val mentorEvaluationComment: String
)

data class IncidentReport(
    val reportId: String,
    val incidentId: String,
    val incidentTitle: String,
    val category: String,
    val analystName: String = "SOC Analyst",
    val submissionTimestamp: Long,
    val severityAssessed: String,
    val executiveSummary: String,
    val attackTimelineSummary: List<String>,
    val identifiedIocs: List<String>,
    val mitreTacticsCovered: List<String>,
    val containmentExecuted: List<String>,
    val rootCauseFindings: String,
    val lessonsLearned: String,
    val score: Int,
    val grade: String,
    val mentorSignature: String = "Alex Vance, Principal SOC Lead & Detection Architect"
)

data class UserProfile(
    val analystName: String = "Junior SOC Analyst",
    val title: String = "SOC Analyst Tier 1",
    val level: Int = 2,
    val xp: Int = 650,
    val nextLevelXp: Int = 1200,
    val streakDays: Int = 4,
    val totalInvestigated: Int = 3,
    val totalSolved: Int = 3,
    val overallReadinessScore: Int = 68, // 0 - 100%
    val detectionAccuracy: Int = 85,
    val evidenceAccuracy: Int = 74,
    val iocAccuracy: Int = 70,
    val mitreAccuracy: Int = 62,
    val containmentAccuracy: Int = 80,
    val reasoningAccuracy: Int = 75,
    val weakestSkills: List<String> = listOf("Sysmon Process Parentage", "VPC Flow Analysis", "IAM Policy Logic"),
    val strongestSkills: List<String> = listOf("CloudTrail API Triage", "IOC Extraction", "S3 Exposure Remediation")
)

data class LearningTopic(
    val id: String,
    val tierNumber: Int,
    val tierTitle: String,
    val title: String,
    val category: String,
    val estimatedMinutes: Int,
    val summary: String,
    val keyTakeaways: List<String>,
    val detectionLogicSnippet: String,
    val sampleLogHighlight: String,
    val practicalCheckQuestion: String,
    val practicalCheckAnswer: String,
    var isCompleted: Boolean = false
)

data class MentorMessage(
    val id: String,
    val sender: String, // USER, MENTOR, SYSTEM
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList(),
    val logReference: String? = null
)
