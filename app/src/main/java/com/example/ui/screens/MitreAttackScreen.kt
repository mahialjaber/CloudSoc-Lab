package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MitreTechnique
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.MitreGlobalCoverageDonutCanvas
import com.example.ui.components.MitreKillChainFlowCanvas
import com.example.ui.components.TerminalBox
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel

@Composable
fun MitreAttackScreen(viewModel: SocViewModel) {
    val allMitre by viewModel.mitreMatrix.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val socColors = SocTheme.colors
    var selectedTactic by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val tacticCoverage = remember(allMitre, incidents) {
        viewModel.getGlobalTacticCoverage()
    }
    val allMappedTactics = remember(incidents, allMitre) {
        val mappedIds = incidents.flatMap { it.userSelectedMitreIds }.toSet()
        allMitre.filter { mappedIds.contains(it.id) }.map { it.tactic }.toSet()
    }

    val tactics = listOf("ALL", "Initial Access", "Execution", "Persistence", "Privilege Escalation", "Defense Evasion", "Credential Access", "Collection", "Exfiltration", "Impact")

    val filteredTechniques = allMitre.filter { tech ->
        val matchesTactic = selectedTactic == "ALL" || tech.tactic.equals(selectedTactic, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                tech.id.contains(searchQuery, ignoreCase = true) ||
                tech.name.contains(searchQuery, ignoreCase = true) ||
                tech.description.contains(searchQuery, ignoreCase = true)
        matchesTactic && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(socColors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "MITRE ATT&CK® Enterprise & Cloud Matrix",
                subtitle = "Threat technique catalog with real-time Canvas coverage & forensic SIEM logic"
            )
        }

        // 1. MITRE ATT&CK Matrix Mastery Donut Canvas
        item {
            MitreGlobalCoverageDonutCanvas(
                tacticCoverage = tacticCoverage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Kill-Chain Stage Progression Rail Canvas
        item {
            MitreKillChainFlowCanvas(
                selectedTactics = allMappedTactics,
                onTacticClick = { clickedTactic ->
                    selectedTactic = if (selectedTactic == clickedTactic) "ALL" else clickedTactic
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search MITRE ID, technique, or keyword (e.g. T1078, IAM, IMDS)...", fontSize = 12.sp, color = socColors.textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = socColors.mitrePurple, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = socColors.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = socColors.mitrePurple,
                    unfocusedBorderColor = socColors.cardBorder,
                    focusedContainerColor = socColors.terminalBg,
                    unfocusedContainerColor = socColors.terminalBg,
                    focusedTextColor = socColors.textPrimary,
                    unfocusedTextColor = socColors.textPrimary
                ),
                singleLine = true
            )
        }

        // Tactic Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tactics) { tac ->
                    val isSelected = selectedTactic == tac
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) socColors.mitrePurple.copy(alpha = 0.25f) else socColors.surface)
                            .border(1.dp, if (isSelected) socColors.mitrePurple else socColors.cardBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedTactic = tac }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tac,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) socColors.mitrePurple else socColors.textSecondary
                        )
                    }
                }
            }
        }

        items(filteredTechniques) { technique ->
            MitreTechniqueDetailCard(technique = technique)
        }
    }
}

@Composable
private fun MitreTechniqueDetailCard(technique: MitreTechnique) {
    var isExpanded by remember { mutableStateOf(false) }
    val socColors = SocTheme.colors

    CyberCard(
        borderColor = socColors.mitrePurple.copy(alpha = 0.35f),
        backgroundColor = socColors.surface
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
                CyberBadge(text = technique.id, color = socColors.mitrePurple)
                CyberBadge(text = technique.tactic, color = socColors.primaryAccent)
            }
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = socColors.mitrePurple,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = technique.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = socColors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = technique.description,
            style = MaterialTheme.typography.bodySmall,
            color = socColors.textSecondary
        )

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (technique.cloudDetectionQuery.isNotBlank()) {
                    Text(
                        text = "CLOUD SIEM DETECTION LOGIC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = socColors.tertiaryAccent
                    )
                    TerminalBox(codeText = technique.cloudDetectionQuery)
                }

                if (technique.mitigation.isNotBlank()) {
                    Text(
                        text = "SECURITY MITIGATION & HARDENING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = socColors.warningAmber
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(socColors.terminalBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = technique.mitigation,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = socColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
