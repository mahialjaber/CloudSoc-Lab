package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Incident
import com.example.model.IncidentStatus
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.SocViewModel

@Composable
fun InvestigationsScreen(viewModel: SocViewModel) {
    val incidents by viewModel.incidents.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "AWS IAM", "S3 Storage", "Sysmon", "Linux Compromise", "Multi-Stage", "EC2 GuardDuty")

    val filteredIncidents = incidents.filter { inc ->
        if (selectedCategoryFilter == "ALL") true
        else inc.category.displayName.contains(selectedCategoryFilter, ignoreCase = true)
    }

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
                title = "SOC Incident Lab Scenarios",
                subtitle = "Hands-on simulated cloud & endpoint intrusions with authentic telemetry"
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else CyberSurface)
                            .border(1.dp, if (isSelected) CyberCyan else CyberBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) CyberCyan else CyberTextSecondary
                        )
                    }
                }
            }
        }

        items(filteredIncidents) { incident ->
            InvestigationCaseCard(
                incident = incident,
                onLaunch = {
                    viewModel.selectIncident(incident.id)
                    viewModel.selectNavTab(MainNavTab.INCIDENT_LAB)
                }
            )
        }
    }
}

@Composable
private fun InvestigationCaseCard(
    incident: Incident,
    onLaunch: () -> Unit
) {
    CyberCard(
        borderColor = when (incident.status) {
            IncidentStatus.SOLVED -> CyberGreen.copy(alpha = 0.5f)
            IncidentStatus.IN_PROGRESS -> CyberOrange.copy(alpha = 0.5f)
            else -> CyberBorder
        },
        backgroundColor = CyberSurface
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
                CyberBadge(text = incident.id, color = CyberCyan)
                SeverityBadge(severity = incident.initialSeverity)
                CyberBadge(text = incident.difficulty.displayName, color = CyberPurple)
            }
            val statusColor = when (incident.status) {
                IncidentStatus.SOLVED -> CyberGreen
                IncidentStatus.IN_PROGRESS -> CyberOrange
                else -> CyberTextMuted
            }
            CyberBadge(text = incident.status.name, color = statusColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = incident.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CyberTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Target: ${incident.targetOrganization} (${incident.assetScope})",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = CyberTextMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = incident.alertDescription,
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextSecondary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${incident.logs.size} SIEM Events | +${incident.xpReward} XP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = CyberGreen
            )

            Button(
                onClick = onLaunch,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (incident.status == IncidentStatus.SOLVED) "Review Lab" else "Open Lab",
                    color = CyberBg,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}
