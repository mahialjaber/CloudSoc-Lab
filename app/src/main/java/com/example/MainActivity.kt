package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.SocViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SocViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.currentThemeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                CloudSocApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSocApp(viewModel: SocViewModel) {
    val currentTab by viewModel.currentNavTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentTheme by viewModel.currentThemeMode.collectAsState()
    val socColors = SocTheme.colors

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = socColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(socColors.primaryAccent.copy(alpha = 0.2f))
                                .border(1.dp, socColors.primaryAccent, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = socColors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "CloudSOC Lab",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = socColors.textPrimary
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Quick SOC Dark Mode / Eye-Strain Reduction Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleNightWatchMode() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (currentTheme != SocThemeMode.STANDARD_CYBER)
                                        socColors.primaryAccent.copy(alpha = 0.25f)
                                    else
                                        socColors.cardBorder.copy(alpha = 0.3f)
                                )
                                .border(
                                    1.dp,
                                    if (currentTheme != SocThemeMode.STANDARD_CYBER) socColors.primaryAccent else socColors.cardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .testTag("toggle_soc_dark_mode_button")
                        ) {
                            Icon(
                                imageVector = when (currentTheme) {
                                    SocThemeMode.STANDARD_CYBER -> Icons.Default.NightlightRound
                                    SocThemeMode.NIGHT_WATCH_REDUCED_STRAIN -> Icons.Default.Visibility
                                    SocThemeMode.OLED_MIDNIGHT -> Icons.Default.Brightness2
                                    SocThemeMode.WARM_AMBER_TERMINAL -> Icons.Default.WbIncandescent
                                },
                                contentDescription = "Toggle SOC Eye-Strain Reduction Mode",
                                tint = socColors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        CyberBadge(text = "LVL ${userProfile.level}", color = socColors.tertiaryAccent)
                        CyberBadge(text = "${userProfile.streakDays}D STREAK", color = socColors.warningAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = socColors.surface,
                    titleContentColor = socColors.textPrimary
                )
            )
        },
        bottomBar = {
            CloudSocBottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectNavTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                MainNavTab.INCIDENT_LAB -> IncidentLabScreen(viewModel = viewModel)
                MainNavTab.LEARNING_PATH -> LearningPathScreen(viewModel = viewModel)
                MainNavTab.MITRE_ATTACK -> MitreAttackScreen(viewModel = viewModel)
                MainNavTab.INVESTIGATIONS -> InvestigationsScreen(viewModel = viewModel)
                MainNavTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                MainNavTab.PROGRESS -> ProgressScreen(viewModel = viewModel)
                MainNavTab.AI_MENTOR -> AiMentorScreen(viewModel = viewModel)
                MainNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CloudSocBottomNav(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit
) {
    val socColors = SocTheme.colors
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(socColors.surface)
            .border(1.dp, socColors.cardBorder)
            .horizontalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val (icon, color) = getTabVisuals(tab, socColors)

                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)) else null,
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) color else socColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) color else socColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun getTabVisuals(tab: MainNavTab, socColors: SocColors): Pair<ImageVector, Color> {
    return when (tab) {
        MainNavTab.DASHBOARD -> Pair(Icons.Default.Dashboard, socColors.primaryAccent)
        MainNavTab.INCIDENT_LAB -> Pair(Icons.Default.Science, socColors.alertRed)
        MainNavTab.LEARNING_PATH -> Pair(Icons.Default.School, socColors.mitrePurple)
        MainNavTab.MITRE_ATTACK -> Pair(Icons.Default.GridOn, socColors.tertiaryAccent)
        MainNavTab.INVESTIGATIONS -> Pair(Icons.Default.FolderSpecial, socColors.warningAmber)
        MainNavTab.REPORTS -> Pair(Icons.Default.Assessment, socColors.secondaryAccent)
        MainNavTab.PROGRESS -> Pair(Icons.Default.TrendingUp, socColors.tertiaryAccent)
        MainNavTab.AI_MENTOR -> Pair(Icons.Default.Psychology, socColors.mitrePurple)
        MainNavTab.SETTINGS -> Pair(Icons.Default.Settings, socColors.textMuted)
    }
}
