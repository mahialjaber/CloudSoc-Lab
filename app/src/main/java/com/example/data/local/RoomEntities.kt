package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_reports")
data class SavedReportEntity(
    @PrimaryKey val reportId: String,
    val incidentId: String,
    val incidentTitle: String,
    val category: String,
    val analystName: String,
    val submissionTimestamp: Long,
    val severityAssessed: String,
    val executiveSummary: String,
    val attackTimelineSummaryJson: String,
    val identifiedIocsJson: String,
    val mitreTacticsCoveredJson: String,
    val containmentExecutedJson: String,
    val rootCauseFindings: String,
    val lessonsLearned: String,
    val score: Int,
    val grade: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val analystName: String = "SOC Analyst",
    val title: String = "SOC Analyst Tier 1",
    val level: Int = 1,
    val xp: Int = 300,
    val nextLevelXp: Int = 1000,
    val streakDays: Int = 3,
    val totalInvestigated: Int = 1,
    val totalSolved: Int = 1,
    val overallReadinessScore: Int = 62,
    val detectionAccuracy: Int = 80,
    val evidenceAccuracy: Int = 70,
    val iocAccuracy: Int = 75,
    val mitreAccuracy: Int = 60,
    val containmentAccuracy: Int = 85,
    val reasoningAccuracy: Int = 70,
    val weakestSkillsJson: String = "[\"Sysmon Process Parentage\", \"VPC Flow Log Analysis\", \"IAM Policy Logic\"]",
    val completedIncidentIdsJson: String = "[]"
)

@Entity(tableName = "incident_progress")
data class IncidentProgressEntity(
    @PrimaryKey val incidentId: String,
    val status: String,
    val userTriageVerdict: String,
    val bookmarkedLogIdsJson: String,
    val userExtractedIocsJson: String,
    val userTimelineJson: String,
    val userSelectedMitreIdsJson: String,
    val userAssessedSeverity: String?,
    val userSelectedContainmentIdsJson: String,
    val userExecutiveSummary: String,
    val userRootCauseHypothesis: String,
    val userNotes: String,
    val finalScoreJson: String?
)

@Entity(tableName = "mentor_messages")
data class MentorMessageEntity(
    @PrimaryKey val id: String,
    val incidentId: String?,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val suggestedActionsJson: String
)
