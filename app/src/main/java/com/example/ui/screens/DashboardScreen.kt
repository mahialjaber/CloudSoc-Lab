package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Incident
import com.example.model.IncidentStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.SocViewModel

@Composable
fun DashboardScreen(viewModel: SocViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val socColors = SocTheme.colors
    val nextChallenge = incidents.firstOrNull { it.status != IncidentStatus.SOLVED } ?: incidents.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(socColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Top Analyst Profile & Status Header
        item {
            AnalystHeroCard(
                userProfile = userProfile,
                onMentorClick = { viewModel.selectNavTab(MainNavTab.AI_MENTOR) }
            )
        }

        // Key SOC Readiness Metrics
        item {
            CyberSectionHeader(
                title = "SOC Readiness & Mastery",
                subtitle = "Algorithmic job-readiness index based on live investigation performance"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Readiness",
                    value = "${userProfile.overallReadinessScore}%",
                    subtitle = "Job Ready Threshold: 75%",
                    icon = Icons.Default.VerifiedUser,
                    accentColor = if (userProfile.overallReadinessScore >= 75) socColors.tertiaryAccent else socColors.primaryAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Accuracy",
                    value = "${userProfile.detectionAccuracy}%",
                    subtitle = "Triage & Detection",
                    icon = Icons.Default.Radar,
                    accentColor = socColors.mitrePurple,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Streak",
                    value = "${userProfile.streakDays}d",
                    subtitle = "Daily Duty Active",
                    icon = Icons.Default.Bolt,
                    accentColor = socColors.warningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Skill Accuracy Rubric Breakdown
        item {
            CyberCard(
                borderColor = socColors.cardBorder,
                backgroundColor = socColors.surface
            ) {
                Text(
                    text = "CORE SOC INVESTIGATION DOMAINS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.primaryAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearScoreBar(label = "Alert Triage & Detection (SIEM)", value = userProfile.detectionAccuracy, color = socColors.primaryAccent)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "Evidence Correlation & Bookmarking", value = userProfile.evidenceAccuracy, color = socColors.tertiaryAccent)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "IOC Extraction & Dissection", value = userProfile.iocAccuracy, color = socColors.mitrePurple)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "MITRE ATT&CK Matrix Mapping", value = userProfile.mitreAccuracy, color = socColors.warningAmber)
                Spacer(modifier = Modifier.height(8.dp))
                LinearScoreBar(label = "Surgical Containment Playbooks", value = userProfile.containmentAccuracy, color = socColors.secondaryAccent)
            }
        }

        // Weakest Skills & Recommended Focus
        item {
            CyberSectionHeader(
                title = "Weakest Skills & Skill Gaps",
                subtitle = "Targeted areas identified from recent investigation reports"
            )
            CyberCard(
                borderColor = socColors.alertRed.copy(alpha = 0.4f),
                backgroundColor = socColors.cardBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(socColors.alertRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = socColors.alertRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Critical Weaknesses Detected",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = socColors.alertRed
                        )
                        Text(
                            text = userProfile.weakestSkills.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = socColors.textSecondary
                        )
                    }
                    Button(
                        onClick = { viewModel.selectNavTab(MainNavTab.LEARNING_PATH) },
                        colors = ButtonDefaults.buttonColors(containerColor = socColors.alertRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("train_weak_skills_button")
                    ) {
                        Text("Train", color = socColors.alertRed, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Recommended Next Challenge
        item {
            CyberSectionHeader(
                title = "Recommended Next Incident",
                subtitle = "Tailored scenario to advance your tier progression"
            )
            RecommendedChallengeCard(
                incident = nextChallenge,
                onStartClick = {
                    viewModel.selectIncident(nextChallenge.id)
                    viewModel.selectNavTab(MainNavTab.INCIDENT_LAB)
                }
            )
        }

        // Active Threat Feed / Recent Incidents Carousel
        item {
            CyberSectionHeader(
                title = "Active Incident Queue",
                subtitle = "Live cloud telemetry waiting for SOC triage",
                actionText = "View All (${incidents.size})",
                onActionClick = { viewModel.selectNavTab(MainNavTab.INVESTIGATIONS) }
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(incidents) { incident ->
                    IncidentQueueItem(
                        incident = incident,
                        onClick = {
                            viewModel.selectIncident(incident.id)
                            viewModel.selectNavTab(MainNavTab.INCIDENT_LAB)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalystHeroCard(
    userProfile: com.example.model.UserProfile,
    onMentorClick: () -> Unit
) {
    val socColors = SocTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(socColors.surface, socColors.cardBg, socColors.surfaceVariant)
                )
            )
            .border(1.dp, socColors.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(socColors.primaryAccent.copy(alpha = 0.15f))
                            .border(1.dp, socColors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = socColors.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userProfile.analystName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = socColors.textPrimary
                        )
                        Text(
                            text = userProfile.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = socColors.primaryAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onMentorClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(socColors.mitrePurple.copy(alpha = 0.18f))
                        .border(1.dp, socColors.mitrePurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .testTag("ai_mentor_hero_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Mentor",
                        tint = socColors.mitrePurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // XP and Level Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL ${userProfile.level}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.tertiaryAccent
                )
                Text(
                    text = "${userProfile.xp} / ${userProfile.nextLevelXp} XP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = socColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (userProfile.xp.toFloat() / userProfile.nextLevelXp.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = socColors.tertiaryAccent,
                trackColor = socColors.terminalBg
            )
        }
    }
}

@Composable
private fun RecommendedChallengeCard(
    incident: Incident,
    onStartClick: () -> Unit
) {
    val socColors = SocTheme.colors
    CyberCard(
        borderColor = socColors.primaryAccent.copy(alpha = 0.4f),
        backgroundColor = socColors.surface,
        modifier = Modifier.testTag("recommended_challenge_card")
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
                SeverityBadge(severity = incident.initialSeverity)
                CyberBadge(text = incident.category.displayName, color = socColors.mitrePurple)
            }
            CyberBadge(text = "+${incident.xpReward} XP", color = socColors.tertiaryAccent)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = incident.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = socColors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = incident.alertHeadline,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = socColors.warningAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = incident.alertDescription,
            style = MaterialTheme.typography.bodySmall,
            color = socColors.textSecondary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("launch_recommended_incident_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = socColors.primaryAccent)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = socColors.background,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LAUNCH INCIDENT INVESTIGATION",
                color = socColors.background,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun IncidentQueueItem(
    incident: Incident,
    onClick: () -> Unit
) {
    val socColors = SocTheme.colors
    Box(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(socColors.cardBg)
            .border(1.dp, socColors.cardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = incident.initialSeverity)
                val statusColor = when (incident.status) {
                    IncidentStatus.SOLVED -> socColors.tertiaryAccent
                    IncidentStatus.IN_PROGRESS -> socColors.warningAmber
                    else -> socColors.textMuted
                }
                CyberBadge(text = incident.status.name, color = statusColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = incident.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = socColors.textPrimary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = incident.targetOrganization,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = socColors.textSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${incident.logs.size} Logs",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = socColors.textMuted
                )
                Text(
                    text = "Triage →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = socColors.primaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
