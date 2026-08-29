package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.LabWorkflowStep
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.SocViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentLabScreen(viewModel: SocViewModel) {
    val activeIncident by viewModel.activeIncident.collectAsState()
    val currentStep by viewModel.currentLabStep.collectAsState()
    val filteredLogs by viewModel.filteredLogs.collectAsState()
    val selectedLogForDetail by viewModel.selectedLogForDetail.collectAsState()
    val lastSubmittedScore by viewModel.lastSubmittedScore.collectAsState()

    var showAddIocDialog by remember { mutableStateOf(false) }
    var showAddTimelineDialog by remember { mutableStateOf<LogEvent?>(null) }

    if (activeIncident == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBg),
            contentAlignment = Alignment.Center
        ) {
            Text("No active incident selected", color = CyberTextSecondary)
        }
        return
    }

    val incident = activeIncident!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Incident Case Header
        IncidentHeader(
            incident = incident,
            onAskMentor = { viewModel.selectNavTab(MainNavTab.AI_MENTOR) },
            onRequestHint = { viewModel.requestProgressiveHint() }
        )

        // Workflow Step Tabs (1 to 7)
        WorkflowStepsTabBar(
            currentStep = currentStep,
            onStepSelected = { viewModel.selectLabStep(it) }
        )

        // Step Content View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                LabWorkflowStep.LOG_EXPLORER -> LogExplorerStep(
                    viewModel = viewModel,
                    incident = incident,
                    logs = filteredLogs,
                    onLogClick = { viewModel.inspectLog(it) },
                    onBookmarkToggle = { viewModel.toggleLogBookmark(it.id) }
                )
                LabWorkflowStep.EVIDENCE_BOOKMARKS -> EvidenceBookmarksStep(
                    viewModel = viewModel,
                    incident = incident,
                    onLogClick = { viewModel.inspectLog(it) }
                )
                LabWorkflowStep.IOC_EXTRACTOR -> IocExtractorStep(
                    incident = incident,
                    onAddIocClick = { showAddIocDialog = true },
                    onRemoveIoc = { viewModel.removeIoc(it) }
                )
                LabWorkflowStep.ATTACK_TIMELINE -> AttackTimelineStep(
                    incident = incident,
                    onRemoveMilestone = { viewModel.removeTimelineMilestone(it) },
                    onAddMilestoneFromLog = { showAddTimelineDialog = incident.logs.firstOrNull() }
                )
                LabWorkflowStep.MITRE_MAPPER -> MitreMapperStep(
                    viewModel = viewModel,
                    incident = incident
                )
                LabWorkflowStep.CONTAINMENT -> ContainmentStep(
                    viewModel = viewModel,
                    incident = incident
                )
                LabWorkflowStep.REPORT_SUBMIT -> ReportSubmitStep(
                    viewModel = viewModel,
                    incident = incident
                )
            }
        }
    }

    // Modal: Raw Log Inspector & Field Breakdown
    if (selectedLogForDetail != null) {
        LogDetailModal(
            log = selectedLogForDetail!!,
            isBookmarked = incident.bookmarkedLogIds.contains(selectedLogForDetail!!.id),
            onDismiss = { viewModel.inspectLog(null) },
            onToggleBookmark = { viewModel.toggleLogBookmark(selectedLogForDetail!!.id) },
            onAddToTimeline = {
                showAddTimelineDialog = selectedLogForDetail
                viewModel.inspectLog(null)
            },
            onAskMentorAboutLog = {
                viewModel.sendMentorMessage("Explain this log: ${selectedLogForDetail!!.rawPayload}", selectedLogForDetail)
                viewModel.inspectLog(null)
                viewModel.selectNavTab(MainNavTab.AI_MENTOR)
            }
        )
    }

    // Modal: Add Custom IOC
    if (showAddIocDialog) {
        AddIocDialog(
            onDismiss = { showAddIocDialog = false },
            onAdd = { type, value, note ->
                viewModel.addIoc(type, value, note)
                showAddIocDialog = false
            }
        )
    }

    // Modal: Add Timeline Milestone
    if (showAddTimelineDialog != null) {
        AddTimelineMilestoneDialog(
            log = showAddTimelineDialog!!,
            onDismiss = { showAddTimelineDialog = null },
            onAdd = { logId, title, desc, phase ->
                viewModel.addTimelineMilestone(logId, title, desc, phase)
                showAddTimelineDialog = null
            }
        )
    }

    // Modal: Scoring Rubric Results Debrief
    if (lastSubmittedScore != null) {
        ScoreRubricResultModal(
            result = lastSubmittedScore!!,
            onDismiss = { viewModel.dismissScoreDialog() },
            onViewReport = {
                viewModel.dismissScoreDialog()
                viewModel.selectNavTab(MainNavTab.REPORTS)
            }
        )
    }
}

@Composable
private fun IncidentHeader(
    incident: Incident,
    onAskMentor: () -> Unit,
    onRequestHint: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurface)
            .border(1.dp, CyberBorder)
            .padding(14.dp)
    ) {
        Column {
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRequestHint,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberOrange.copy(alpha = 0.15f))
                            .testTag("request_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Hint",
                            tint = CyberOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onAskMentor,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberPurple.copy(alpha = 0.15f))
                            .testTag("incident_ask_mentor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Ask Mentor",
                            tint = CyberPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Target: ${incident.targetOrganization} | Scope: ${incident.assetScope}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = CyberTextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Alert: ${incident.alertHeadline}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = CyberOrange
            )
        }
    }
}

@Composable
private fun WorkflowStepsTabBar(
    currentStep: LabWorkflowStep,
    onStepSelected: (LabWorkflowStep) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberCardBg)
            .border(1.dp, CyberBorder)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LabWorkflowStep.values().forEach { step ->
            val isSelected = currentStep == step
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface)
                    .border(
                        1.dp,
                        if (isSelected) CyberCyan else CyberBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onStepSelected(step) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("lab_step_${step.stepNum}")
            ) {
                Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) CyberCyan else CyberTextSecondary
                )
            }
        }
    }
}

@Composable
private fun LogExplorerStep(
    viewModel: SocViewModel,
    incident: Incident,
    logs: List<LogEvent>,
    onLogClick: (LogEvent) -> Unit,
    onBookmarkToggle: (LogEvent) -> Unit
) {
    val searchQuery by viewModel.logSearchQuery.collectAsState()
    val selectedSource by viewModel.logFilterSource.collectAsState()
    val onlyBookmarked by viewModel.onlyBookmarkedLogs.collectAsState()
    val hideNoise by viewModel.hideNoiseLogs.collectAsState()

    val sources = listOf("ALL", "CloudTrail", "Sysmon", "Linux auth.log", "Linux auditd", "S3AccessLog", "Web WAF / Nginx", "VPC Flow Logs", "GuardDuty")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Search Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.logSearchQuery.value = it },
            placeholder = { Text("Filter logs (e.g. 'AttachUserPolicy', 'ip:185', 'sarah')", fontSize = 12.sp, color = CyberTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.logSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("siem_search_input"),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = CyberTerminalBg,
                unfocusedContainerColor = CyberTerminalBg,
                focusedTextColor = CyberTextPrimary,
                unfocusedTextColor = CyberTextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Log Source Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sources) { src ->
                val isSelected = selectedSource == src
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberPurple.copy(alpha = 0.25f) else CyberSurface)
                        .border(1.dp, if (isSelected) CyberPurple else CyberBorder, RoundedCornerShape(6.dp))
                        .clickable { viewModel.logFilterSource.value = src }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = src,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isSelected) CyberPurple else CyberTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Toggles: Hide Noise, Bookmarked Only
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = hideNoise,
                    onClick = { viewModel.hideNoiseLogs.value = !hideNoise },
                    label = { Text("Filter Noise", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberOrange.copy(alpha = 0.2f),
                        selectedLabelColor = CyberOrange
                    )
                )
                FilterChip(
                    selected = onlyBookmarked,
                    onClick = { viewModel.onlyBookmarkedLogs.value = !onlyBookmarked },
                    label = { Text("Bookmarked (${incident.bookmarkedLogIds.size})", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan
                    )
                )
            }
            Text(
                text = "${logs.size} Events",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = CyberTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Log Event Stream
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 60.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs) { log ->
                val isBookmarked = incident.bookmarkedLogIds.contains(log.id)
                LogEventCard(
                    log = log,
                    isBookmarked = isBookmarked,
                    onClick = { onLogClick(log) },
                    onBookmarkToggle = { onBookmarkToggle(log) }
                )
            }
        }
    }
}

@Composable
private fun LogEventCard(
    log: LogEvent,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isBookmarked) CyberCyan.copy(alpha = 0.08f) else CyberTerminalBg)
            .border(
                1.dp,
                if (isBookmarked) CyberCyan.copy(alpha = 0.6f) else CyberBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = log.source, color = CyberBlue)
                    SeverityBadge(severity = log.severity)
                    Text(
                        text = log.timestamp.take(19),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = CyberTextMuted
                    )
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) CyberCyan else CyberTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${log.eventName} [${log.actor}]",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (log.isMalicious) CyberAlertRed else CyberTextPrimary
            )

            if (log.sourceIp != "-") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Source IP: ${log.sourceIp} ${if (log.destinationIp != "-") "→ Dst: ${log.destinationIp}" else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = CyberOrange
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.rawPayload,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                ),
                color = CyberGreen,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun EvidenceBookmarksStep(
    viewModel: SocViewModel,
    incident: Incident,
    onLogClick: (LogEvent) -> Unit
) {
    val bookmarkedLogs = incident.logs.filter { incident.bookmarkedLogIds.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Triage Verdict Selector
        item {
            CyberCard(
                borderColor = CyberCyan.copy(alpha = 0.4f),
                backgroundColor = CyberSurface
            ) {
                Text(
                    text = "ALERT TRIAGE CLASSIFICATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Assess the initial alert based on collected telemetry:",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TriageVerdictOption(
                        label = "True Positive",
                        description = "Confirmed Attack",
                        isSelected = incident.userTriageVerdict == "TRUE_POSITIVE",
                        accentColor = CyberAlertRed,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTriageVerdict("TRUE_POSITIVE") }
                    )
                    TriageVerdictOption(
                        label = "False Positive",
                        description = "Erroneous Alert",
                        isSelected = incident.userTriageVerdict == "FALSE_POSITIVE",
                        accentColor = CyberOrange,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTriageVerdict("FALSE_POSITIVE") }
                    )
                    TriageVerdictOption(
                        label = "Benign",
                        description = "Normal Admin Activity",
                        isSelected = incident.userTriageVerdict == "BENIGN",
                        accentColor = CyberGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTriageVerdict("BENIGN") }
                    )
                }
            }
        }

        // Bookmarked Evidence List
        item {
            CyberSectionHeader(
                title = "Bookmarked Evidence Stream (${bookmarkedLogs.size})",
                subtitle = "Key forensic events tagged for the attack chain"
            )
        }

        if (bookmarkedLogs.isEmpty()) {
            item {
                CyberCard {
                    Text(
                        text = "No logs bookmarked yet. Switch to 'SIEM Logs' tab and click the bookmark icon on suspicious API calls or process events.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }
        } else {
            items(bookmarkedLogs) { log ->
                LogEventCard(
                    log = log,
                    isBookmarked = true,
                    onClick = { onLogClick(log) },
                    onBookmarkToggle = { viewModel.toggleLogBookmark(log.id) }
                )
            }
        }

        // Analyst Working Notes
        item {
            CyberCard {
                Text(
                    text = "ANALYST INVESTIGATIVE NOTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = incident.userInvestigativeNotes,
                    onValueChange = { viewModel.updateInvestigativeNotes(it) },
                    placeholder = { Text("Record hypothesis, suspect IPs, compromised IAM credentials...", fontSize = 12.sp, color = CyberTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberTerminalBg,
                        unfocusedContainerColor = CyberTerminalBg,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun TriageVerdictOption(
    label: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else CyberTerminalBg)
            .border(
                1.dp,
                if (isSelected) accentColor else CyberBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (isSelected) accentColor else CyberTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = CyberTextMuted
            )
        }
    }
}

@Composable
private fun IocExtractorStep(
    incident: Incident,
    onAddIocClick: () -> Unit,
    onRemoveIoc: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Discovered Indicators of Compromise (IOCs)",
                subtitle = "Extract malicious IPs, domains, hashes, and IAM ARNs from evidence",
                actionText = "+ Add IOC",
                onActionClick = onAddIocClick
            )
        }

        if (incident.userExtractedIocs.isEmpty()) {
            item {
                CyberCard {
                    Text(
                        text = "No IOCs cataloged yet. Tap '+ Add IOC' to log malicious IPs, compromised usernames, backdoor roles, or payload hashes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }
        } else {
            items(incident.userExtractedIocs) { ioc ->
                IocItemCard(ioc = ioc, onRemove = { onRemoveIoc(ioc.id) })
            }
        }
    }
}

@Composable
private fun IocItemCard(ioc: IocItem, onRemove: () -> Unit) {
    CyberCard(
        borderColor = CyberPurple.copy(alpha = 0.4f),
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
                CyberBadge(text = ioc.type.label, color = CyberPurple)
                Text(
                    text = ioc.value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberTextPrimary
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
            }
        }
        if (ioc.contextNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ioc.contextNote,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = CyberTextSecondary
            )
        }
    }
}

@Composable
private fun AttackTimelineStep(
    incident: Incident,
    onRemoveMilestone: (String) -> Unit,
    onAddMilestoneFromLog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Attack Chain Progression Timeline",
                subtitle = "Reconstruct the chronological steps taken by the adversary",
                actionText = "+ Add Milestone",
                onActionClick = onAddMilestoneFromLog
            )
        }

        if (incident.userTimeline.isEmpty()) {
            item {
                CyberCard {
                    Text(
                        text = "Timeline is empty. In the 'SIEM Logs' tab, tap any critical log and select 'Add to Attack Timeline' to build the attack sequence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }
        } else {
            items(incident.userTimeline) { milestone ->
                TimelineMilestoneCard(
                    milestone = milestone,
                    onRemove = { onRemoveMilestone(milestone.id) }
                )
            }
        }
    }
}

@Composable
private fun TimelineMilestoneCard(milestone: TimelineMilestone, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = milestone.phase, color = CyberCyan)
                    Text(
                        text = milestone.timestamp.take(19),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = CyberTextMuted
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = milestone.description,
                style = MaterialTheme.typography.bodySmall,
                color = CyberTextSecondary
            )
        }
    }
}

@Composable
private fun MitreMapperStep(
    viewModel: SocViewModel,
    incident: Incident
) {
    val allMitre by viewModel.mitreMatrix.collectAsState()
    val (mappedCount, targetCount) = viewModel.getMitreMappingProgress(incident)
    val selectedTactics = allMitre.filter { incident.userSelectedMitreIds.contains(it.id) }.map { it.tactic }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Visual Canvas Progress Indicator
        item {
            MitreTaskProgressIndicatorCanvas(
                mappedCount = mappedCount,
                targetCount = targetCount,
                precisionPercentage = if (incident.userSelectedMitreIds.isEmpty()) 100 else ((mappedCount.toFloat() / incident.userSelectedMitreIds.size.coerceAtLeast(1).toFloat()) * 100).toInt(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Kill-Chain Tactic Stage Progression Rail on Canvas
        item {
            MitreKillChainFlowCanvas(
                selectedTactics = selectedTactics,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CyberSectionHeader(
                title = "MITRE ATT&CK Cloud Matrix Mapping",
                subtitle = "Select all tactics & techniques exhibited by the adversary in this incident"
            )
        }

        items(allMitre) { tech ->
            val isSelected = incident.userSelectedMitreIds.contains(tech.id)
            MitreSelectableCard(
                technique = tech,
                isSelected = isSelected,
                onToggle = { viewModel.toggleMitreTechnique(tech.id) }
            )
        }
    }
}

@Composable
private fun MitreSelectableCard(
    technique: MitreTechnique,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberPurple.copy(alpha = 0.15f) else CyberTerminalBg)
            .border(
                1.dp,
                if (isSelected) CyberPurple else CyberBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onToggle() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = technique.id, color = CyberPurple)
                    CyberBadge(text = technique.tactic, color = CyberCyan)
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CyberPurple,
                        checkmarkColor = CyberBg
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = technique.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = technique.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = CyberTextSecondary
            )
        }
    }
}

@Composable
private fun ContainmentStep(
    viewModel: SocViewModel,
    incident: Incident
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Incident Containment & Response Playbooks",
                subtitle = "Select surgical containment actions to neutralize the threat while minimizing service disruption"
            )
        }

        items(incident.availableContainmentActions) { action ->
            val isSelected = incident.userSelectedContainmentIds.contains(action.id)
            ContainmentActionCard(
                action = action,
                isSelected = isSelected,
                onToggle = { viewModel.toggleContainmentAction(action.id) }
            )
        }
    }
}

@Composable
private fun ContainmentActionCard(
    action: ContainmentAction,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val isDestructive = action.impactWarning.isNotBlank()
    val borderColor = when {
        isSelected && isDestructive -> CyberAlertRed
        isSelected -> CyberGreen
        else -> CyberBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) borderColor.copy(alpha = 0.12f) else CyberSurface)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = action.category, color = CyberBlue)
                    if (isDestructive) {
                        CyberBadge(text = "HIGH IMPACT", color = CyberAlertRed)
                    }
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isDestructive) CyberAlertRed else CyberGreen,
                        checkmarkColor = CyberBg
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = CyberTextSecondary
            )
            if (isDestructive) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CyberAlertRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = action.impactWarning,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = CyberAlertRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportSubmitStep(
    viewModel: SocViewModel,
    incident: Incident
) {
    var severityChoice by remember { mutableStateOf(incident.userAssessedSeverity ?: incident.initialSeverity) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 70.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Submit Formal SOC Incident Report",
                subtitle = "Complete the executive debrief and submit your investigation for grading"
            )
        }

        // Severity Assessment Picker
        item {
            CyberCard {
                Text(
                    text = "FINAL SEVERITY ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IncidentSeverity.values().forEach { sev ->
                        val isSelected = severityChoice == sev
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberTerminalBg)
                                .border(1.dp, if (isSelected) CyberCyan else CyberBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    severityChoice = sev
                                    viewModel.assessSeverity(sev)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sev.displayName.take(4),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isSelected) CyberCyan else CyberTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Executive Summary Input
        item {
            CyberCard {
                Text(
                    text = "EXECUTIVE SUMMARY (ANALYST HYPOTHESIS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Summarize the adversary intrusion vector, key compromised assets, blast radius, and containment status in professional SOC language.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = CyberTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = incident.userExecutiveSummary,
                    onValueChange = { viewModel.updateExecutiveSummary(it) },
                    placeholder = { Text("e.g. Adversary obtained leaked AWS API keys for dev-cameron from Tor IP 185.220.101.42, escalated privileges via AdministratorAccess, and created backdoor role AWSServiceRoleForBackupSync...", fontSize = 12.sp, color = CyberTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("executive_summary_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberTerminalBg,
                        unfocusedContainerColor = CyberTerminalBg,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    )
                )
            }
        }

        // Pre-flight Investigation Checklist
        item {
            CyberCard(backgroundColor = CyberSurface) {
                Text(
                    text = "INVESTIGATION PRE-FLIGHT CHECKLIST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChecklistItem(label = "Triage Verdict Classified", isDone = incident.isTriageDone)
                ChecklistItem(label = "Evidence Bookmarked (${incident.bookmarkedLogIds.size} logs)", isDone = incident.bookmarkedLogIds.isNotEmpty())
                ChecklistItem(label = "IOCs Extracted (${incident.userExtractedIocs.size} items)", isDone = incident.userExtractedIocs.isNotEmpty())
                ChecklistItem(label = "Timeline Milestones (${incident.userTimeline.size} steps)", isDone = incident.userTimeline.isNotEmpty())
                ChecklistItem(label = "MITRE ATT&CK Mapped (${incident.userSelectedMitreIds.size} techniques)", isDone = incident.userSelectedMitreIds.isNotEmpty())
                ChecklistItem(label = "Containment Playbooks (${incident.userSelectedContainmentIds.size} actions)", isDone = incident.userSelectedContainmentIds.isNotEmpty())
                ChecklistItem(label = "Executive Narrative Articulated", isDone = incident.userExecutiveSummary.isNotBlank())
            }
        }

        // Big Submit Button
        item {
            Button(
                onClick = { viewModel.submitInvestigation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_formal_investigation_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = CyberBg, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SUBMIT INCIDENT REPORT FOR SCORING",
                    color = CyberBg,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

@Composable
private fun ChecklistItem(label: String, isDone: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) CyberGreen else CyberTextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDone) CyberTextPrimary else CyberTextMuted
        )
    }
}

@Composable
private fun LogDetailModal(
    log: LogEvent,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    onAddToTimeline: () -> Unit,
    onAskMentorAboutLog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Inspector",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberCyan
                )
                CyberBadge(text = log.source, color = CyberBlue)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "${log.eventName} [${log.timestamp}]",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Parsed fields
                if (log.parsedFields.isNotEmpty()) {
                    Text(
                        text = "PARSED FIELDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyberPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurface)
                            .padding(8.dp)
                    ) {
                        log.parsedFields.forEach { (k, v) ->
                            Text(
                                text = "$k: $v",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = CyberGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Raw JSON Payload
                Text(
                    text = "RAW PAYLOAD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                TerminalBox(
                    codeText = log.rawPayload,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onToggleBookmark,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBookmarked) CyberCyan.copy(alpha = 0.2f) else CyberSurface
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBookmarked) "Saved" else "Bookmark", fontSize = 10.sp, color = CyberCyan)
                    }

                    Button(
                        onClick = onAddToTimeline,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Timeline", fontSize = 10.sp, color = CyberGreen)
                    }

                    Button(
                        onClick = onAskMentorAboutLog,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Alex", fontSize = 10.sp, color = CyberPurple)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyberCyan)
            }
        },
        containerColor = CyberCardBg
    )
}

@Composable
private fun AddIocDialog(
    onDismiss: () -> Unit,
    onAdd: (IocType, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(IocType.IP_ADDRESS) }
    var valueText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Extract Indicator of Compromise", color = CyberCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IocType.values().forEach { t ->
                        val isSelected = selectedType == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberPurple.copy(alpha = 0.2f) else CyberSurface)
                                .border(1.dp, if (isSelected) CyberPurple else CyberBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedType = t }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = t.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isSelected) CyberPurple else CyberTextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("IOC Value (IP, ARN, Hash, Domain)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Context Note (e.g. Tor Exit, Leaked Key ID)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedType, valueText, noteText) },
                enabled = valueText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Add IOC", color = CyberBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyberTextMuted)
            }
        },
        containerColor = CyberCardBg
    )
}

@Composable
private fun AddTimelineMilestoneDialog(
    log: LogEvent,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    val phases = listOf("Initial Access", "Execution", "Persistence", "Privilege Escalation", "Defense Evasion", "Credential Access", "Discovery", "Lateral Movement", "Collection", "Exfiltration", "Impact")
    var selectedPhase by remember { mutableStateOf(phases.first()) }
    var titleText by remember { mutableStateOf(log.eventName) }
    var descText by remember { mutableStateOf(log.parsedFields.entries.joinToString(", ") { "${it.key}: ${it.value}" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to Attack Chain Timeline", color = CyberCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Phase picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    phases.forEach { p ->
                        val isSelected = selectedPhase == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface)
                                .border(1.dp, if (isSelected) CyberCyan else CyberBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedPhase = p }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isSelected) CyberCyan else CyberTextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Milestone Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    )
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Event Description & Adversary Action") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(log.id, titleText, descText, selectedPhase) },
                enabled = titleText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Add Milestone", color = CyberBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyberTextMuted)
            }
        },
        containerColor = CyberCardBg
    )
}

@Composable
private fun ScoreRubricResultModal(
    result: InvestigationScoreResult,
    onDismiss: () -> Unit,
    onViewReport: () -> Unit
) {
    val gradeColor = when (result.grade) {
        "S", "A" -> CyberGreen
        "B" -> CyberCyan
        "C" -> CyberOrange
        else -> CyberAlertRed
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Investigation Evaluation", color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(gradeColor.copy(alpha = 0.2f))
                        .border(1.dp, gradeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.grade,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = gradeColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score: ${result.overallScore} / 100",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = gradeColor
                    )
                    CyberBadge(text = "+${result.earnedXp} XP EARNED", color = CyberGreen)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rubric Breakdown
                Text(
                    text = "SCORING RUBRIC CRITERIA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RubricScoreRow("Alert Detection & Triage", "${result.detectionScore}/20")
                    RubricScoreRow("Evidence Bookmarking", "${result.evidenceScore}/15")
                    RubricScoreRow("IOC Extraction & Recall", "${result.iocScore}/15")
                    RubricScoreRow("Timeline Correlation", "${result.timelineScore}/15")
                    RubricScoreRow("MITRE ATT&CK Mapping", "${result.mitreScore}/15")
                    RubricScoreRow("Containment Safety & Accuracy", "${result.containmentScore}/10")
                    RubricScoreRow("Executive Reasoning", "${result.reasoningScore}/10")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Lead Mentor Critique
                Text(
                    text = "LEAD MENTOR CRITIQUE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberPurple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.mentorEvaluationComment,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weaknesses Detected
                if (result.weaknessesDetected.isNotEmpty()) {
                    Text(
                        text = "DETECTED WEAKNESS AREAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyberAlertRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    result.weaknessesDetected.forEach { w ->
                        Text(
                            text = "• $w",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = CyberAlertRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onViewReport,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("View Archived Report", color = CyberBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = CyberTextMuted)
            }
        },
        containerColor = CyberCardBg
    )
}

@Composable
private fun RubricScoreRow(label: String, score: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyberTextSecondary)
        Text(text = score, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = CyberTextPrimary)
    }
}
