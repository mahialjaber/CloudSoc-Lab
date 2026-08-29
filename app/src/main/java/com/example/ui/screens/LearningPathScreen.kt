package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.LearningTopic
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.TerminalBox
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel

@Composable
fun LearningPathScreen(viewModel: SocViewModel) {
    val topics by viewModel.learningPaths.collectAsState()
    val groupedTopics = topics.groupBy { it.tierTitle }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "SOC & Cloud Threat Detection Curriculum",
                subtitle = "Progressive mastery path from Tier 1 Triage to Incident Commander"
            )
        }

        groupedTopics.forEach { (tierTitle, topicList) ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = tierTitle, color = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(CyberBorder)
                    )
                }
            }

            items(topicList) { topic ->
                LearningTopicCard(
                    topic = topic,
                    onToggleComplete = { viewModel.toggleTopicComplete(topic.id) }
                )
            }
        }
    }
}

@Composable
private fun LearningTopicCard(
    topic: LearningTopic,
    onToggleComplete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAnswer by remember { mutableStateOf(false) }

    CyberCard(
        borderColor = if (topic.isCompleted) CyberGreen.copy(alpha = 0.5f) else CyberBorder,
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
                CyberBadge(text = topic.category, color = CyberPurple)
                Text(
                    text = "${topic.estimatedMinutes} min read",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = CyberTextMuted
                )
            }
            Checkbox(
                checked = topic.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CyberGreen,
                    checkmarkColor = CyberBg
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = topic.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CyberTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = topic.summary,
            style = MaterialTheme.typography.bodySmall,
            color = CyberTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExpanded) "Hide Detection Details & Quiz" else "View Detection Rules & Practice Quiz",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Key Takeaways
                Text(
                    text = "KEY ANALYST TAKEAWAYS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberCyan
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberTerminalBg)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    topic.keyTakeaways.forEach { takeaway ->
                        Text(
                            text = "• $takeaway",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = CyberTextPrimary
                        )
                    }
                }

                // Detection Logic
                Text(
                    text = "SIEM DETECTION QUERY (KQL / SPL)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberGreen
                )
                TerminalBox(codeText = topic.detectionLogicSnippet)

                // Practical Check Quiz
                Text(
                    text = "PRACTICAL CHECK QUESTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberOrange
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberTerminalBg)
                        .border(1.dp, CyberOrange.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = topic.practicalCheckQuestion,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!showAnswer) {
                            Button(
                                onClick = { showAnswer = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reveal Explanation", color = CyberOrange, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            Text(
                                text = "Answer: ${topic.practicalCheckAnswer}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = CyberGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
