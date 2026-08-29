package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.SocTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual Progress Indicator for MITRE ATT&CK Mapping Tasks rendered using Jetpack Compose Canvas.
 * Displays real-time radial mapping coverage, target techniques hit, and precision metrics.
 */
@Composable
fun MitreTaskProgressIndicatorCanvas(
    mappedCount: Int,
    targetCount: Int,
    precisionPercentage: Int = 100,
    modifier: Modifier = Modifier
) {
    val socColors = SocTheme.colors
    val safeTarget = if (targetCount <= 0) 1 else targetCount
    val targetRatio = (mappedCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    // Animated progress transitions
    val animatedProgress by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "mitre_progress_arc"
    )

    val animatedPrecision by animateFloatAsState(
        targetValue = (precisionPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "precision_arc"
    )

    // Infinite breathing glow for the active radar reticle
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val primaryColor = if (animatedProgress >= 1f) socColors.primaryAccent else if (animatedProgress > 0.4f) socColors.primaryAccent else socColors.warningAmber
    val secondaryColor = socColors.mitrePurple
    val trackColor = socColors.cardBorder.copy(alpha = 0.4f)

    CyberCard(
        borderColor = primaryColor.copy(alpha = 0.4f),
        backgroundColor = socColors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MITRE ATT&CK MAPPING COMPLETION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = primaryColor
                    )
                }

                CyberBadge(
                    text = if (targetRatio >= 1f) "100% COMPLETE" else "${mappedCount}/${safeTarget} TARGETS",
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Radial Arc Meter + Calibration Ticks
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f - 12.dp.toPx()
                    val innerRadius = outerRadius - 16.dp.toPx()

                    // 1. Center pulsing radial ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = pulseAlpha * 0.35f),
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )

                    // 2. Draw 36 Calibration Ticks around the dial
                    val totalTicks = 36
                    for (i in 0 until totalTicks) {
                        val angleDeg = (i * (360f / totalTicks)) - 90f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val isCovered = (i.toFloat() / totalTicks.toFloat()) <= animatedProgress

                        val tickOuterR = outerRadius + 8.dp.toPx()
                        val tickInnerR = if (i % 6 == 0) outerRadius + 2.dp.toPx() else outerRadius + 5.dp.toPx()

                        val startX = (center.x + tickInnerR * cos(angleRad)).toFloat()
                        val startY = (center.y + tickInnerR * sin(angleRad)).toFloat()
                        val endX = (center.x + tickOuterR * cos(angleRad)).toFloat()
                        val endY = (center.y + tickOuterR * sin(angleRad)).toFloat()

                        drawLine(
                            color = if (isCovered) primaryColor.copy(alpha = 0.8f) else trackColor.copy(alpha = 0.4f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (i % 6 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // 3. Background Tracks (Outer and Inner)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = trackColor.copy(alpha = 0.25f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                        size = Size(innerRadius * 2, innerRadius * 2),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )

                    // 4. Outer Animated Sweep Arc (Target Completion)
                    if (animatedProgress > 0.01f) {
                        val outerGradient = Brush.sweepGradient(
                            0f to socColors.mitrePurple,
                            0.5f to socColors.primaryAccent,
                            1f to primaryColor
                        )

                        drawArc(
                            brush = outerGradient,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                            size = Size(outerRadius * 2, outerRadius * 2),
                            style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Glowing head beacon
                        val headAngle = Math.toRadians((animatedProgress * 360f - 90f).toDouble())
                        val headX = (center.x + outerRadius * cos(headAngle)).toFloat()
                        val headY = (center.y + outerRadius * sin(headAngle)).toFloat()
                        drawCircle(
                            color = primaryColor,
                            radius = 6.dp.toPx(),
                            center = Offset(headX, headY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(headX, headY)
                        )
                    }

                    // 5. Inner Secondary Arc (Precision / Accuracy Gauge)
                    if (animatedPrecision > 0.01f) {
                        drawArc(
                            color = secondaryColor.copy(alpha = 0.85f),
                            startAngle = -90f,
                            sweepAngle = animatedPrecision * 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                            size = Size(innerRadius * 2, innerRadius * 2),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Numerical Display & Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${(targetRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-0.5).sp
                        ),
                        color = primaryColor
                    )
                    Text(
                        text = "$mappedCount of $safeTarget Mapped",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = socColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Forensic Mapping Verdict Banner
            val (verdictTitle, verdictDesc, verdictColor) = when {
                targetRatio >= 1f -> Triple(
                    "KILL CHAIN FULLY MAPPED",
                    "All core adversary techniques correctly identified and correlated.",
                    socColors.primaryAccent
                )
                targetRatio >= 0.5f -> Triple(
                    "PARTIAL MITRE COVERAGE",
                    "Key tactics mapped. Review logs for persistence or exfiltration techniques.",
                    socColors.warningAmber
                )
                mappedCount > 0 -> Triple(
                    "INITIAL DISCOVERY ACTIVE",
                    "Correlate additional suspicious CloudTrail API calls or Sysmon parentage.",
                    socColors.mitrePurple
                )
                else -> Triple(
                    "AWAITING MITRE TAGGING",
                    "Select applicable tactics and techniques exhibited by this adversary below.",
                    socColors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(verdictColor.copy(alpha = 0.12f))
                    .border(1.dp, verdictColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (targetRatio >= 1f) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = verdictColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = verdictTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = verdictColor
                        )
                        Text(
                            text = verdictDesc,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = socColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kill-Chain Attack Stage Progression Rail drawn on Canvas.
 * Connects tactical phases with glowing nodes and links.
 */
@Composable
fun MitreKillChainFlowCanvas(
    selectedTactics: Set<String>,
    onTacticClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val socColors = SocTheme.colors
    val scrollState = rememberScrollState()

    val attackStages = listOf(
        Pair("Initial Access", "T1078"),
        Pair("Execution", "T1059"),
        Pair("Persistence", "T1098"),
        Pair("Privilege Escalation", "T1548"),
        Pair("Defense Evasion", "T1562"),
        Pair("Credential Access", "T1552"),
        Pair("Collection", "T1530"),
        Pair("Exfiltration", "T1537")
    )

    CyberCard(
        borderColor = socColors.cardBorder,
        backgroundColor = socColors.surface,
        modifier = modifier
    ) {
        Text(
            text = "ATTACK LIFECYCLE COVERAGE STAGES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = socColors.primaryAccent
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                attackStages.forEachIndexed { index, (stageName, sampleCode) ->
                    val isMapped = selectedTactics.any { it.equals(stageName, ignoreCase = true) }
                    val stageColor = if (isMapped) socColors.primaryAccent else socColors.textMuted

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Canvas Stage Node
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMapped) stageColor.copy(alpha = 0.18f) else socColors.terminalBg)
                                .border(1.dp, if (isMapped) stageColor else socColors.cardBorder, RoundedCornerShape(8.dp))
                                .then(if (onTacticClick != null) Modifier.clickable { onTacticClick(stageName) } else Modifier)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    if (isMapped) {
                                        drawCircle(
                                            color = stageColor.copy(alpha = 0.35f),
                                            radius = 6.dp.toPx()
                                        )
                                        drawCircle(
                                            color = stageColor,
                                            radius = 3.5.dp.toPx()
                                        )
                                    } else {
                                        drawCircle(
                                            color = stageColor,
                                            radius = 3.dp.toPx(),
                                            style = Stroke(width = 1.dp.toPx())
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = stageName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isMapped) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (isMapped) socColors.textPrimary else socColors.textMuted
                                    )
                                    Text(
                                        text = if (isMapped) "TAGGED" else sampleCode,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = stageColor
                                    )
                                }
                            }
                        }

                        // Connecting link line
                        if (index < attackStages.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Canvas(modifier = Modifier.size(width = 16.dp, height = 4.dp)) {
                                drawLine(
                                    color = if (isMapped) stageColor else socColors.cardBorder,
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Segmented MITRE ATT&CK Matrix Global Coverage Donut Chart.
 * Displays overall mastery breakdown across all MITRE tactics on Canvas.
 */
@Composable
fun MitreGlobalCoverageDonutCanvas(
    tacticCoverage: Map<String, Float>, // Tactic Name -> completion ratio (0f to 1f)
    modifier: Modifier = Modifier
) {
    val socColors = SocTheme.colors
    val totalTactics = tacticCoverage.size.coerceAtLeast(1)
    val averageCoverage = (tacticCoverage.values.sum() / totalTactics).coerceIn(0f, 1f)

    val animatedCoverage by animateFloatAsState(
        targetValue = averageCoverage,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "global_mitre_donut"
    )

    CyberCard(
        borderColor = socColors.mitrePurple.copy(alpha = 0.4f),
        backgroundColor = socColors.surface,
        modifier = modifier
    ) {
        Text(
            text = "MITRE ATT&CK MATRIX MASTERY WHEEL",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = socColors.mitrePurple
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Donut Canvas
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f - 6.dp.toPx()
                    val strokeWidth = 10.dp.toPx()

                    val tacticsList = tacticCoverage.entries.toList()
                    val segmentAngle = 360f / tacticsList.size
                    val gap = 4f // degrees gap between segments

                    tacticsList.forEachIndexed { i, entry ->
                        val startAngle = (i * segmentAngle) - 90f + (gap / 2f)
                        val sweepAngle = segmentAngle - gap
                        val ratio = entry.value.coerceIn(0f, 1f)

                        // Segment Background Track
                        drawArc(
                            color = socColors.cardBorder.copy(alpha = 0.35f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                            size = Size(outerRadius * 2, outerRadius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Segment Active Fill
                        if (ratio > 0.05f) {
                            val color = when {
                                ratio >= 0.8f -> socColors.primaryAccent
                                ratio >= 0.4f -> socColors.secondaryAccent
                                else -> socColors.mitrePurple
                            }

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle * ratio,
                                useCenter = false,
                                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                                size = Size(outerRadius * 2, outerRadius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Center Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedCoverage * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = socColors.primaryAccent
                    )
                    Text(
                        text = "Coverage",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = socColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Breakdown Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tacticCoverage.entries.take(4).forEach { (tactic, ratio) ->
                    val color = if (ratio >= 0.6f) socColors.primaryAccent else socColors.mitrePurple
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tactic,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = socColors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = color
                        )
                    }
                }
            }
        }
    }
}
