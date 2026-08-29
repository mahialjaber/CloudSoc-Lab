package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.BuildConfig
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel

@Composable
fun SettingsScreen(viewModel: SocViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentTheme by viewModel.currentThemeMode.collectAsState()
    val socColors = SocTheme.colors
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    val hasGeminiApiKey = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (e: Throwable) {
        false
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
                title = "SOC Platform Settings",
                subtitle = "Configure analyst profile, SOC dark mode ergonomics, AI model, and lab state"
            )
        }

        // Analyst Profile Card
        item {
            CyberCard {
                Text(
                    text = "ANALYST PROFILE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.primaryAccent
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = userProfile.analystName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = socColors.textPrimary
                        )
                        Text(
                            text = userProfile.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = socColors.primaryAccent
                        )
                    }
                    CyberBadge(text = "LEVEL ${userProfile.level}", color = socColors.tertiaryAccent)
                }
            }
        }

        // SOC Dark Mode & Visual Ergonomics (Dedicated Eye-Strain Reduction)
        item {
            CyberCard(
                borderColor = socColors.primaryAccent.copy(alpha = 0.4f),
                backgroundColor = socColors.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SOC DARK MODE & ERGONOMICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = socColors.primaryAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Forensic Eye-Strain Reduction Modes",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = socColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Calibrated for 8–12 hour SOC triage shifts to minimize optical fatigue, glare, and high-energy blue-light emissions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = socColors.textSecondary
                        )
                    }
                    CyberBadge(
                        text = currentTheme.badgeLabel,
                        color = socColors.primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Mode Selector Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SocThemeMode.values().forEach { mode ->
                        val isSelected = currentTheme == mode
                        val previewColors = getSocColorsForMode(mode)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) previewColors.primaryAccent.copy(alpha = 0.15f) else previewColors.terminalBg)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) previewColors.primaryAccent else previewColors.cardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(12.dp)
                                .testTag("theme_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color swatches indicator
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(previewColors.background)
                                            .border(1.dp, previewColors.primaryAccent, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(previewColors.primaryAccent)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(previewColors.tertiaryAccent)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (isSelected) previewColors.primaryAccent else socColors.textPrimary
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = socColors.textSecondary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = previewColors.primaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Integration Status
        item {
            CyberCard(
                borderColor = if (hasGeminiApiKey) socColors.tertiaryAccent.copy(alpha = 0.4f) else socColors.mitrePurple.copy(alpha = 0.4f),
                backgroundColor = socColors.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI SOC MENTOR ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = socColors.mitrePurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasGeminiApiKey) "Gemini 2.5 Flash API Connected" else "Dual-Mode: Built-in Senior SOC Engine Active",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = socColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (hasGeminiApiKey) "Live dynamic AI reasoning enabled with custom system instructions." else "Operating with built-in Senior Lead Detection Engineer knowledge base. Add GEMINI_API_KEY in Secrets for live generative mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = socColors.textSecondary
                        )
                    }
                    CyberBadge(
                        text = if (hasGeminiApiKey) "LIVE API" else "BUILT-IN",
                        color = if (hasGeminiApiKey) socColors.tertiaryAccent else socColors.mitrePurple
                    )
                }
            }
        }

        // Local Storage & Database Reset
        item {
            CyberCard(
                borderColor = socColors.alertRed.copy(alpha = 0.3f),
                backgroundColor = socColors.surface
            ) {
                Text(
                    text = "LAB DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.alertRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clear local Room database tables, reset XP and score metrics, and re-seed clean authentic incident telemetry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = socColors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = socColors.alertRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reset_lab_data_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = socColors.alertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Lab Data & Scenarios", color = socColors.alertRed, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // App Information
        item {
            CyberCard(backgroundColor = socColors.cardBg) {
                Text(
                    text = "ABOUT CLOUDSOC LAB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = socColors.primaryAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CloudSOC Lab v1.0.0 (Production Edition)\nArchitecture: Jetpack Compose + Compose Canvas + Room SQLite + Custom MaterialTheme Dark Modes + Gemini AI Studio Mentorship.\nBuilt for job-ready SOC Analyst & Cloud Incident Response preparation.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = socColors.textSecondary
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Lab Progress?", color = socColors.alertRed, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear your local incident triage progress, saved reports in the Room database, and reset your level XP.", color = socColors.textPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Lab progress reset successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = socColors.alertRed)
                ) {
                    Text("Reset", color = socColors.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = socColors.textMuted)
                }
            },
            containerColor = socColors.cardBg
        )
    }
}
