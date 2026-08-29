package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IncidentReport
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: SocViewModel) {
    val reports by viewModel.savedReports.collectAsState(initial = emptyList())
    val selectedReport by viewModel.selectedReportForDetail.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "SOC Incident Portfolio & Reports",
                subtitle = "Executive debriefs and forensic reports archived in local Room database"
            )
        }

        if (reports.isEmpty()) {
            item {
                CyberCard {
                    Text(
                        text = "No submitted incident reports yet. Complete an investigation in the Incident Lab and click 'Submit Incident Report' to generate an executive report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }
        } else {
            items(reports) { report ->
                ReportSummaryCard(
                    report = report,
                    onClick = { viewModel.inspectReport(report) }
                )
            }
        }
    }

    if (selectedReport != null) {
        ReportDetailModal(
            report = selectedReport!!,
            onDismiss = { viewModel.inspectReport(null) },
            onCopy = {
                val formattedText = formatReportForExport(selectedReport!!)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("SOC Incident Report", formattedText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun ReportSummaryCard(
    report: IncidentReport,
    onClick: () -> Unit
) {
    val gradeColor = when (report.grade) {
        "S", "A" -> CyberGreen
        "B" -> CyberCyan
        "C" -> CyberOrange
        else -> CyberAlertRed
    }

    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(report.submissionTimestamp))

    CyberCard(
        borderColor = gradeColor.copy(alpha = 0.4f),
        backgroundColor = CyberSurface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberBadge(text = report.reportId, color = CyberCyan)
                CyberBadge(text = report.category, color = CyberPurple)
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(gradeColor.copy(alpha = 0.15f))
                    .border(1.dp, gradeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = report.grade,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = gradeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = report.incidentTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CyberTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Submitted: $dateStr | Score: ${report.score}/100",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = CyberTextMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = report.executiveSummary,
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextSecondary,
            maxLines = 2
        )
    }
}

@Composable
private fun ReportDetailModal(
    report: IncidentReport,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).format(Date(report.submissionTimestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SOC INCIDENT REPORT", color = CyberCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(report.reportId, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = CyberTextMuted)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = report.incidentTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberTextPrimary
                    )
                    Text(
                        text = "Analyst: ${report.analystName} | Submitted: $dateStr",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                        color = CyberTextMuted
                    )
                }

                // Executive Summary
                item {
                    ReportSectionHeader(title = "1. EXECUTIVE SUMMARY")
                    Text(report.executiveSummary, style = MaterialTheme.typography.bodySmall, color = CyberTextPrimary)
                }

                // Attack Timeline
                item {
                    ReportSectionHeader(title = "2. ATTACK TIMELINE & CORRELATION")
                    report.attackTimelineSummary.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyberTextSecondary)
                    }
                }

                // IOC Table
                item {
                    ReportSectionHeader(title = "3. EXTRACTED INDICATORS OF COMPROMISE (IOCS)")
                    report.identifiedIocs.forEach { ioc ->
                        Text("• $ioc", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace), color = CyberOrange)
                    }
                }

                // MITRE Techniques
                item {
                    ReportSectionHeader(title = "4. MITRE ATT&CK MAPPINGS")
                    report.mitreTacticsCovered.forEach { tac ->
                        Text("• $tac", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyberPurple)
                    }
                }

                // Containment Actions
                item {
                    ReportSectionHeader(title = "5. CONTAINMENT & RESPONSE EXECUTED")
                    report.containmentExecuted.forEach { act ->
                        Text("• $act", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyberGreen)
                    }
                }

                // Lessons Learned & Root Cause
                item {
                    ReportSectionHeader(title = "6. ROOT CAUSE & LESSONS LEARNED")
                    Text(report.rootCauseFindings, style = MaterialTheme.typography.bodySmall, color = CyberTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Preventative Controls: ${report.lessonsLearned}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyberTextSecondary)
                }

                // Sign-off
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurface)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Signed off by: ${report.mentorSignature}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = CyberTextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Close", color = CyberBg)
            }
        },
        containerColor = CyberCardBg
    )
}

@Composable
private fun ReportSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        color = CyberCyan,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

private fun formatReportForExport(report: IncidentReport): String {
    return """
============================================================
              CLOUDSOC LAB INCIDENT REPORT
============================================================
Report ID: ${report.reportId}
Incident: ${report.incidentTitle}
Category: ${report.category}
Assessed Severity: ${report.severityAssessed}
Analyst: ${report.analystName}
Score: ${report.score}/100 (Grade ${report.grade})

1. EXECUTIVE SUMMARY:
${report.executiveSummary}

2. ATTACK TIMELINE & CORRELATION:
${report.attackTimelineSummary.joinToString("\n") { "• $it" }}

3. INDICATORS OF COMPROMISE (IOCS):
${report.identifiedIocs.joinToString("\n") { "• $it" }}

4. MITRE ATT&CK MATRIX:
${report.mitreTacticsCovered.joinToString("\n") { "• $it" }}

5. CONTAINMENT & ERADICATION:
${report.containmentExecuted.joinToString("\n") { "• $it" }}

6. ROOT CAUSE FINDINGS:
${report.rootCauseFindings}

7. LESSONS LEARNED & RECOMMENDATIONS:
${report.lessonsLearned}

Reviewer: ${report.mentorSignature}
============================================================
    """.trimIndent()
}
