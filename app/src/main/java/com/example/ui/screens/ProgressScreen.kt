package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel

@Composable
fun ProgressScreen(viewModel: SocViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allMitre by viewModel.mitreMatrix.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val socColors = SocTheme.colors

    val tacticCoverage = remember(allMitre, incidents) {
        viewModel.getGlobalTacticCoverage()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(socColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "SOC Job-Readiness & Skill Assessment",
                subtitle = "Algorithmic profile evaluation benchmarked against real L1/L2 SOC hiring rubrics"
            )
        }

        // Overall Readiness Hero Card
        item {
            CyberCard(
                borderColor = socColors.primaryAccent.copy(alpha = 0.5f),
                backgroundColor = socColors.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JOB READINESS INDEX",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = socColors.primaryAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${userProfile.overallReadinessScore}%",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (userProfile.overallReadinessScore >= 75) socColors.tertiaryAccent else socColors.primaryAccent
                        )
                        Text(
                            text = if (userProfile.overallReadinessScore >= 75) "Ready for Junior SOC Analyst & Cloud IR Roles" else "In Training — Target 75% for SOC Hiring Confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = socColors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(socColors.primaryAccent.copy(alpha = 0.15f))
                            .border(2.dp, socColors.primaryAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = socColors.primaryAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // MITRE ATT&CK Matrix Mastery Donut Canvas
        item {
            MitreGlobalCoverageDonutCanvas(
                tacticCoverage = tacticCoverage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 6-Factor Competency Radar
        item {
            CyberCard {
                Text(
                    text = "TECHNICAL COMPETENCY BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.primaryAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearScoreBar(label = "CloudTrail API & IAM Forensics", value = userProfile.detectionAccuracy, color = socColors.primaryAccent)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "SIEM Log Correlation & Noise Filtering", value = userProfile.evidenceAccuracy, color = socColors.tertiaryAccent)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "IOC Extraction (Network & Cloud ARNs)", value = userProfile.iocAccuracy, color = socColors.mitrePurple)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "MITRE ATT&CK Cloud Matrix Mapping", value = userProfile.mitreAccuracy, color = socColors.warningAmber)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "Surgical Containment Playbooks", value = userProfile.containmentAccuracy, color = socColors.secondaryAccent)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "Incident Report & Narrative Articulation", value = userProfile.reasoningAccuracy, color = socColors.tertiaryAccent)
            }
        }

        // Job Readiness Verification Checklist
        item {
            CyberSectionHeader(
                title = "SOC Hiring Competency Verification",
                subtitle = "Core capabilities evaluated by SOC hiring managers"
            )
            CyberCard(backgroundColor = socColors.surface) {
                CompetencyCheckRow(title = "AWS IAM PrivEsc Detection", description = "Identifies AttachUserPolicy and rogue role backdoors", isVerified = true)
                CompetencyCheckRow(title = "Sysmon Process Parentage", description = "Detects macro execution and process hollowing", isVerified = true)
                CompetencyCheckRow(title = "S3 Data Exfiltration Forensics", description = "Calculates stolen volume and tampered policies", isVerified = true)
                CompetencyCheckRow(title = "EC2 SSRF & IMDSv1 Defense", description = "Triages stolen instance credentials and enforces IMDSv2", isVerified = true)
                CompetencyCheckRow(title = "Linux auditd & auth.log Triage", description = "Pinpoints SSH brute force and systemd persistence", isVerified = true)
                CompetencyCheckRow(title = "Surgical Blast-Radius Containment", description = "Avoids catastrophic accidental production outages", isVerified = true)
            }
        }
    }
}

@Composable
private fun CompetencyCheckRow(
    title: String,
    description: String,
    isVerified: Boolean
) {
    val socColors = SocTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isVerified) socColors.tertiaryAccent else socColors.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = socColors.textPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = socColors.textSecondary
            )
        }
    }
}
