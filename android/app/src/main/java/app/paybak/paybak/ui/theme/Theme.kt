package app.paybak.paybak.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Paybak's only theme: light, Manrope, Paybak tokens. Components read the tokens directly
 * ([PbColors], [PbTextStyles], …); the Material scheme is mapped onto them so any Material building
 * block used later (for example a debug `DropdownMenu`) matches.
 */
@Composable
fun PaybakTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PaybakColorScheme, typography = PaybakMaterialTypography) {
        CompositionLocalProvider(
            LocalReduceMotion provides rememberSystemReduceMotion(),
            LocalTextSelectionColors provides PaybakSelectionColors,
            content = content,
        )
    }
}

private val PaybakColorScheme =
    lightColorScheme(
        primary = PbColors.Bg.Inverse,
        onPrimary = PbColors.Text.Inverse,
        secondary = PbColors.Bg.Card,
        onSecondary = PbColors.Text.Primary,
        background = PbColors.Bg.Primary,
        onBackground = PbColors.Text.Primary,
        surface = PbColors.Bg.Primary,
        onSurface = PbColors.Text.Primary,
        surfaceVariant = PbColors.Bg.Card,
        onSurfaceVariant = PbColors.Text.Secondary,
        surfaceContainer = PbColors.Bg.Primary,
        outline = PbColors.Border.Strong,
        outlineVariant = PbColors.Border.Subtle,
        error = PbColors.Text.Destructive,
        onError = PbColors.Text.Inverse,
        scrim = PbColors.Bg.Scrim,
    )

private val PaybakMaterialTypography =
    Typography(
        displayLarge = PbTextStyles.AmountDisplay,
        headlineLarge = PbTextStyles.Title1,
        headlineMedium = PbTextStyles.Title2,
        titleLarge = PbTextStyles.Title3,
        titleMedium = PbTextStyles.Headline,
        bodyLarge = PbTextStyles.Body,
        bodyMedium = PbTextStyles.Subheadline,
        bodySmall = PbTextStyles.Footnote,
        labelLarge = PbTextStyles.ButtonSmall,
        labelMedium = PbTextStyles.Caption1,
        labelSmall = PbTextStyles.Caption2,
    )

/** Caret, handles and selection in black, like the Figma code-digit caret. */
private val PaybakSelectionColors =
    TextSelectionColors(
        handleColor = PbColors.Text.Primary,
        backgroundColor = PbColors.Text.Primary.copy(alpha = 0.2f),
    )
