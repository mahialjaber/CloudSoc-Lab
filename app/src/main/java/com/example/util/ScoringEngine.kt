package com.example.util

import com.example.model.*
import java.util.UUID

object ScoringEngine {

    fun evaluateInvestigation(incident: Incident): Pair<InvestigationScoreResult, IncidentReport> {
        val feedback = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()

        // 1. Detection & Triage Score (max 20)
        var detectionScore = 0
        if (incident.userTriageVerdict.equals("TRUE_POSITIVE", ignoreCase = true)) {
            detectionScore = 20
            feedback.add("✅ Correct Alert Triage: Successfully verified malicious True Positive signature.")
        } else {
            detectionScore = 5
            weaknesses.add("Alert Classification: Misidentified active attack as false/benign positive.")
            feedback.add("⚠️ Triage Alert Verdict: This alert represented an active threat rather than a false positive.")
        }

        // 2. Evidence Collection & Bookmarking (max 15)
        val maliciousLogIds = incident.logs.filter { it.isMalicious }.map { it.id }.toSet()
        val bookmarkedMalicious = incident.bookmarkedLogIds.intersect(maliciousLogIds).size
        val totalMalicious = maliciousLogIds.size.coerceAtLeast(1)
        val bookmarkRatio = bookmarkedMalicious.toFloat() / totalMalicious.toFloat()
        val evidenceScore = (bookmarkRatio * 15).toInt().coerceIn(0, 15)

        if (bookmarkRatio >= 0.75f) {
            feedback.add("✅ Evidence Collection: Comprehensive log tagging across key attack phases ($bookmarkedMalicious/$totalMalicious critical logs).")
        } else {
            weaknesses.add("Evidence Correlation: Missed key malicious log entries during SIEM triage.")
            feedback.add("⚠️ Evidence Collection: Only identified $bookmarkedMalicious of $totalMalicious critical attack events.")
        }

        // 3. IOC Extraction Score (max 15)
        val correctIocValues = incident.correctIocs.map { it.value.lowercase().trim() }.toSet()
        val userIocValues = incident.userExtractedIocs.map { it.value.lowercase().trim() }.toSet()
        val matchedIocs = userIocValues.intersect(correctIocValues).size
        val totalIocs = correctIocValues.size.coerceAtLeast(1)
        val iocRatio = matchedIocs.toFloat() / totalIocs.toFloat()
        val iocScore = (iocRatio * 15).toInt().coerceIn(0, 15)

        if (iocRatio >= 0.6f) {
            feedback.add("✅ IOC Extraction: Successfully extracted primary adversary indicators ($matchedIocs/$totalIocs key IOCs).")
        } else {
            weaknesses.add("IOC Identification: Weak extraction of adversary network and credential indicators.")
            feedback.add("⚠️ IOC Extraction: Missing critical adversary IPs/ARNs in evidence catalog.")
        }

        // 4. Attack Timeline Score (max 15)
        val userTimelineCount = incident.userTimeline.size
        val timelineScore = when {
            userTimelineCount >= 3 -> 15
            userTimelineCount in 1..2 -> 10
            else -> 4
        }
        if (timelineScore >= 12) {
            feedback.add("✅ Timeline Correlation: Attack progression clearly articulated in chronological phases.")
        } else {
            weaknesses.add("Timeline Reconstruction: Sparse chronological attack event sequence.")
            feedback.add("⚠️ Attack Timeline: Add more key milestones to illustrate adversary progression.")
        }

        // 5. MITRE ATT&CK Mapping (max 15)
        val correctMitreIds = incident.correctMitreTechniques.map { it.id.uppercase() }.toSet()
        val userMitreIds = incident.userSelectedMitreIds.map { it.uppercase() }.toSet()
        val matchedMitre = userMitreIds.intersect(correctMitreIds).size
        val totalMitre = correctMitreIds.size.coerceAtLeast(1)
        val mitreRatio = (matchedMitre.toFloat() / totalMitre.toFloat()).coerceAtMost(1.0f)
        val mitreScore = (mitreRatio * 15).toInt().coerceIn(0, 15)

        if (mitreRatio >= 0.6f) {
            feedback.add("✅ MITRE ATT&CK Mapping: Accurately mapped adversary tactics ($matchedMitre techniques aligned).")
        } else {
            weaknesses.add("MITRE Mapping: Inaccurate tactic/technique categorization.")
            feedback.add("⚠️ MITRE Mapping: Review ATT&CK cloud matrix techniques for this attack category.")
        }

        // 6. Containment & Response Score (max 10)
        val correctContIds = incident.correctContainmentIds.toSet()
        val userContIds = incident.userSelectedContainmentIds
        val rightContSelections = userContIds.intersect(correctContIds).size
        val penaltySelections = userContIds.any { id ->
            incident.availableContainmentActions.find { it.id == id }?.impactWarning?.isNotBlank() == true
        }

        var containmentScore = (rightContSelections * 3).coerceIn(0, 10)
        if (penaltySelections) {
            containmentScore = (containmentScore - 3).coerceAtLeast(0)
            feedback.add("🚨 Containment Impact Penalty: Selected an overly destructive action that would disrupt production systems!")
            weaknesses.add("Incident Response: Applied disproportionate, destructive containment actions.")
        } else if (rightContSelections >= 2) {
            feedback.add("✅ Surgical Containment: Effective, non-destructive isolation playbooks selected.")
        }

        // 7. Analyst Reasoning & Executive Summary (max 10)
        val summaryWords = incident.userExecutiveSummary.trim().split("\\s+".toRegex()).size
        val reasoningScore = when {
            summaryWords >= 25 -> 10
            summaryWords in 10..24 -> 7
            summaryWords in 1..9 -> 4
            else -> 2
        }

        if (reasoningScore >= 8) {
            feedback.add("✅ Executive Reasoning: Crisp, professional incident narrative articulated.")
        } else {
            weaknesses.add("Executive Reporting: Incident summary lacks depth or clarity.")
            feedback.add("⚠️ Reporting: Expand executive summary with business impact and root cause details.")
        }

        val overallScore = (detectionScore + evidenceScore + iocScore + timelineScore + mitreScore + containmentScore + reasoningScore).coerceIn(0, 100)

        val grade = when {
            overallScore >= 90 -> "S"
            overallScore >= 80 -> "A"
            overallScore >= 70 -> "B"
            overallScore >= 55 -> "C"
            else -> "F"
        }

        val earnedXp = (incident.xpReward * (overallScore / 100.0)).toInt().coerceAtLeast(100)

        val mentorComment = when {
            overallScore >= 85 -> "Outstanding investigation, Analyst. Your log correlation, IOC tagging, and surgical containment reflect senior-grade SOC readiness."
            overallScore >= 70 -> "Solid investigation work. You captured the primary intrusion path. Review the missing IOCs and refine your MITRE mapping."
            else -> "Good effort. Focus on correlating timestamps between initial auth anomalies and privileged API executions."
        }

        val scoreResult = InvestigationScoreResult(
            overallScore = overallScore,
            grade = grade,
            detectionScore = detectionScore,
            evidenceScore = evidenceScore,
            iocScore = iocScore,
            timelineScore = timelineScore,
            mitreScore = mitreScore,
            containmentScore = containmentScore,
            reasoningScore = reasoningScore,
            feedbackItems = feedback,
            weaknessesDetected = weaknesses.ifEmpty { listOf("None detected — strong forensic consistency") },
            earnedXp = earnedXp,
            mentorEvaluationComment = mentorComment
        )

        val report = IncidentReport(
            reportId = "REP-${UUID.randomUUID().toString().take(8).uppercase()}",
            incidentId = incident.id,
            incidentTitle = incident.title,
            category = incident.category.displayName,
            analystName = "SOC Analyst",
            submissionTimestamp = System.currentTimeMillis(),
            severityAssessed = incident.userAssessedSeverity?.displayName ?: incident.initialSeverity.displayName,
            executiveSummary = if (incident.userExecutiveSummary.isNotBlank()) incident.userExecutiveSummary else "Adversary gained unauthorized access via compromised credentials, escalated privileges, and initiated exfiltration activities. Containment executed immediately.",
            attackTimelineSummary = incident.userTimeline.map { "${it.timestamp} [${it.phase}]: ${it.title} - ${it.description}" }.ifEmpty {
                incident.correctMilestones.map { "${it.timestamp} [${it.phase}]: ${it.title}" }
            },
            identifiedIocs = incident.userExtractedIocs.map { "[${it.type.label}] ${it.value} (${it.contextNote})" }.ifEmpty {
                incident.correctIocs.map { "[${it.type.label}] ${it.value}" }
            },
            mitreTacticsCovered = incident.userSelectedMitreIds.mapNotNull { id ->
                incident.correctMitreTechniques.find { it.id.equals(id, ignoreCase = true) }?.let { "${it.id}: ${it.name} (${it.tactic})" }
            }.ifEmpty { incident.correctMitreTechniques.map { "${it.id}: ${it.name}" } },
            containmentExecuted = incident.userSelectedContainmentIds.mapNotNull { id ->
                incident.availableContainmentActions.find { it.id == id }?.title
            }.ifEmpty { listOf("Credential Revocation", "Perimeter IP Block") },
            rootCauseFindings = incident.rootCauseSummary,
            lessonsLearned = "Enforce MFA across all cloud accounts, enforce IMDSv2, implement automated least-privilege boundary policies, and set real-time SIEM alerts on privileged IAM APIs.",
            score = overallScore,
            grade = grade
        )

        return Pair(scoreResult, report)
    }
}
