package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IncidentSeverity
import com.example.ui.theme.*

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SocTheme.colors.cardBorder,
    backgroundColor: Color = SocTheme.colors.cardBg,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(16.dp)

    Column(modifier = cardModifier) {
        content()
    }
}

@Composable
fun CyberBadge(
    text: String,
    color: Color = SocTheme.colors.primaryAccent,
    backgroundColor: Color = color.copy(alpha = 0.15f)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = color
        )
    }
}

@Composable
fun SeverityBadge(severity: IncidentSeverity) {
    val socColors = SocTheme.colors
    val (color, bg) = when (severity) {
        IncidentSeverity.CRITICAL -> Pair(socColors.alertRed, socColors.alertRed.copy(alpha = 0.18f))
        IncidentSeverity.HIGH -> Pair(CyberOrange, CyberOrange.copy(alpha = 0.18f))
        IncidentSeverity.MEDIUM -> Pair(socColors.warningAmber, socColors.warningAmber.copy(alpha = 0.18f))
        IncidentSeverity.LOW -> Pair(socColors.primaryAccent, socColors.primaryAccent.copy(alpha = 0.18f))
        IncidentSeverity.INFORMATIONAL -> Pair(socColors.textMuted, socColors.surface)
    }
    CyberBadge(text = severity.displayName, color = color, backgroundColor = bg)
}

@Composable
fun TerminalBox(
    codeText: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    onCopy: (() -> Unit)? = null
) {
    val socColors = SocTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(socColors.terminalBg)
            .border(1.dp, socColors.cardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(socColors.primaryAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = socColors.primaryAccent
                    )
                }
                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy query",
                            tint = socColors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = codeText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp
            ),
            color = socColors.codeText
        )
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = SocTheme.colors.primaryAccent,
    modifier: Modifier = Modifier
) {
    val socColors = SocTheme.colors
    CyberCard(
        modifier = modifier,
        borderColor = accentColor.copy(alpha = 0.3f),
        backgroundColor = socColors.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = socColors.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = accentColor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = socColors.textSecondary
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CyberSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val socColors = SocTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(socColors.primaryAccent, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = socColors.textPrimary
                )
            }
            if (actionText != null && onActionClick != null) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = socColors.primaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = socColors.textSecondary,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun LinearScoreBar(
    label: String,
    value: Int, // 0 - 100
    color: Color = SocTheme.colors.primaryAccent,
    modifier: Modifier = Modifier
) {
    val socColors = SocTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = socColors.textSecondary
            )
            Text(
                text = "$value%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = socColors.surface
        )
    }
}
