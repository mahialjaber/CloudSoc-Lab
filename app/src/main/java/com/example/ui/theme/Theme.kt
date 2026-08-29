package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SOC Theme Modes tailored for security analysts and long forensic triage sessions.
 */
enum class SocThemeMode(
    val displayName: String,
    val description: String,
    val badgeLabel: String
) {
    STANDARD_CYBER(
        displayName = "Standard Cyber SOC",
        description = "High-contrast neon cyan and electric telemetry for standard environments.",
        badgeLabel = "STANDARD"
    ),
    NIGHT_WATCH_REDUCED_STRAIN(
        displayName = "SOC Night Watch (Eye-Strain Reduction)",
        description = "Subdued low-luminance phosphor emerald and warm amber to reduce optical fatigue during 12h graveyard shifts.",
        badgeLabel = "LOW BLUE-LIGHT"
    ),
    OLED_MIDNIGHT(
        displayName = "OLED Tactical Midnight",
        description = "Pure pitch black (#000000) canvas for true zero pixel emission on AMOLED screens.",
        badgeLabel = "OLED 0% LUX"
    ),
    WARM_AMBER_TERMINAL(
        displayName = "Warm Amber Terminal",
        description = "Zero high-energy blue-light spectrum with soothing phosphor amber CRT aesthetics.",
        badgeLabel = "0% BLUE LIGHT"
    )
}

/**
 * Extended semantic design tokens for the SOC Analyst interface.
 */
data class SocColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val terminalBg: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val alertRed: Color,
    val warningAmber: Color,
    val mitrePurple: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val codeText: Color,
    val isEyeStrainReductionActive: Boolean,
    val mode: SocThemeMode
)

// Standard Cyber Dark Color Scheme
private val StandardCyberSocColors = SocColors(
    background = CyberBg,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    cardBg = CyberCardBg,
    cardBorder = CyberCardBorder,
    terminalBg = CyberTerminalBg,
    primaryAccent = CyberCyan,
    secondaryAccent = CyberElectricBlue,
    tertiaryAccent = CyberNeonGreen,
    alertRed = CyberAlertRed,
    warningAmber = CyberWarningAmber,
    mitrePurple = CyberPurple,
    textPrimary = CyberTextPrimary,
    textSecondary = CyberTextSecondary,
    textMuted = CyberTextMuted,
    codeText = CyberCodeText,
    isEyeStrainReductionActive = false,
    mode = SocThemeMode.STANDARD_CYBER
)

// Night Watch (Eye-Strain Reduction) Colors
private val NightWatchSocColors = SocColors(
    background = NightWatchBg,
    surface = NightWatchSurface,
    surfaceVariant = NightWatchSurfaceVariant,
    cardBg = NightWatchCardBg,
    cardBorder = NightWatchCardBorder,
    terminalBg = NightWatchTerminalBg,
    primaryAccent = NightWatchEmerald,
    secondaryAccent = NightWatchTeal,
    tertiaryAccent = NightWatchWarmAmber,
    alertRed = NightWatchSoftRed,
    warningAmber = NightWatchWarmAmber,
    mitrePurple = NightWatchSoftPurple,
    textPrimary = NightWatchTextPrimary,
    textSecondary = NightWatchTextSecondary,
    textMuted = NightWatchTextMuted,
    codeText = NightWatchCodeText,
    isEyeStrainReductionActive = true,
    mode = SocThemeMode.NIGHT_WATCH_REDUCED_STRAIN
)

// OLED Tactical Midnight Colors
private val OledSocColors = SocColors(
    background = OledBg,
    surface = OledSurface,
    surfaceVariant = OledSurfaceVariant,
    cardBg = OledCardBg,
    cardBorder = OledCardBorder,
    terminalBg = OledTerminalBg,
    primaryAccent = OledAccentGreen,
    secondaryAccent = OledAccentCyan,
    tertiaryAccent = NightWatchEmerald,
    alertRed = CyberAlertRed,
    warningAmber = CyberWarningAmber,
    mitrePurple = CyberPurple,
    textPrimary = OledTextPrimary,
    textSecondary = OledTextSecondary,
    textMuted = OledTextMuted,
    codeText = OledAccentGreen,
    isEyeStrainReductionActive = true,
    mode = SocThemeMode.OLED_MIDNIGHT
)

// Warm Amber CRT Colors
private val AmberSocColors = SocColors(
    background = AmberTerminalBg,
    surface = AmberSurface,
    surfaceVariant = AmberSurfaceVariant,
    cardBg = AmberCardBg,
    cardBorder = AmberCardBorder,
    terminalBg = AmberTerminalBoxBg,
    primaryAccent = AmberPrimary,
    secondaryAccent = AmberSecondary,
    tertiaryAccent = AmberTertiary,
    alertRed = AmberAlert,
    warningAmber = AmberPrimary,
    mitrePurple = AmberSecondary,
    textPrimary = AmberTextPrimary,
    textSecondary = AmberTextSecondary,
    textMuted = AmberTextMuted,
    codeText = AmberPrimary,
    isEyeStrainReductionActive = true,
    mode = SocThemeMode.WARM_AMBER_TERMINAL
)

fun getSocColorsForMode(mode: SocThemeMode): SocColors {
    return when (mode) {
        SocThemeMode.STANDARD_CYBER -> StandardCyberSocColors
        SocThemeMode.NIGHT_WATCH_REDUCED_STRAIN -> NightWatchSocColors
        SocThemeMode.OLED_MIDNIGHT -> OledSocColors
        SocThemeMode.WARM_AMBER_TERMINAL -> AmberSocColors
    }
}

fun createM3ColorScheme(socColors: SocColors): ColorScheme {
    return darkColorScheme(
        primary = socColors.primaryAccent,
        onPrimary = socColors.background,
        primaryContainer = socColors.primaryAccent.copy(alpha = 0.2f),
        onPrimaryContainer = socColors.primaryAccent,
        secondary = socColors.secondaryAccent,
        onSecondary = socColors.textPrimary,
        secondaryContainer = socColors.surfaceVariant,
        onSecondaryContainer = socColors.textPrimary,
        tertiary = socColors.tertiaryAccent,
        onTertiary = socColors.background,
        tertiaryContainer = socColors.tertiaryAccent.copy(alpha = 0.15f),
        onTertiaryContainer = socColors.tertiaryAccent,
        error = socColors.alertRed,
        onError = socColors.textPrimary,
        errorContainer = socColors.alertRed.copy(alpha = 0.2f),
        onErrorContainer = socColors.alertRed,
        background = socColors.background,
        onBackground = socColors.textPrimary,
        surface = socColors.surface,
        onSurface = socColors.textPrimary,
        surfaceVariant = socColors.surfaceVariant,
        onSurfaceVariant = socColors.textSecondary,
        outline = socColors.cardBorder,
        outlineVariant = socColors.cardBorder.copy(alpha = 0.6f)
    )
}

val LocalSocColors = staticCompositionLocalOf { StandardCyberSocColors }

object SocTheme {
    val colors: SocColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSocColors.current
}

@Composable
fun MyApplicationTheme(
    themeMode: SocThemeMode = SocThemeMode.STANDARD_CYBER,
    content: @Composable () -> Unit
) {
    val socColors = getSocColorsForMode(themeMode)
    val m3ColorScheme = createM3ColorScheme(socColors)

    CompositionLocalProvider(LocalSocColors provides socColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
