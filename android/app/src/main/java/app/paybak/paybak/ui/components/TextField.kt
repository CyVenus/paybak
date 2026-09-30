package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Width of the focus and error rings drawn by the input field and code digits. */
internal val FieldRingWidth = 1.5.dp

/**
 * `Control / Input Field` (`PBTextField`): a 52 dp #F5F5F5 field with an optional label above and
 * helper below. States follow Figma: Default (placeholder), Focused (black ring), Filled, Error
 * (red ring and red helper) and Disabled. The ring is drawn inside the field as an overlay, so the
 * text stays 16 dp from the edge in every state (README rule 6).
 *
 * @param helper Helper text; in the error state it carries the error message.
 */
@Composable
fun PbTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helper: String? = null,
    isError: Boolean = false,
    leadingIcon: PbIcon? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val ring by
        animateColorAsState(
            targetValue =
                when {
                    !enabled -> Color.Transparent
                    isError -> PbColors.Border.Destructive
                    focused -> PbColors.Border.Strong
                    else -> Color.Transparent
                },
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbTextField ring",
        )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        if (label != null) {
            Text(
                text = label,
                style = PbTextStyles.Subheadline,
                color = if (enabled) PbColors.Text.Secondary else PbColors.Text.Disabled,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle =
                PbTextStyles.Body.copy(
                    color = if (enabled) PbColors.Text.Primary else PbColors.Text.Disabled
                ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            interactionSource = source,
            cursorBrush = SolidColor(PbColors.Text.Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(PbSize.ButtonLg)
                            .background(PbColors.Bg.Card, PbShapes.Input)
                            .border(FieldRingWidth, ring, PbShapes.Input)
                            .padding(horizontal = PbSpace.S16),
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        PbIconImage(
                            icon = leadingIcon,
                            contentDescription = null,
                            size = PbSize.IconMd,
                            tint = if (enabled) PbColors.Icon.Secondary else PbColors.Icon.Tertiary,
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = PbTextStyles.Body,
                                color =
                                    if (enabled) PbColors.Text.Tertiary else PbColors.Text.Disabled,
                                maxLines = 1,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
        if (helper != null) {
            Text(
                text = helper,
                style = PbTextStyles.Footnote,
                color =
                    when {
                        !enabled -> PbColors.Text.Disabled
                        isError -> PbColors.Text.Destructive
                        else -> PbColors.Text.Tertiary
                    },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbTextFieldPreview() {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PbTextField(
            value = "",
            onValueChange = {},
            label = "Email",
            placeholder = "you@example.com",
            helper = "We’ll send a 6-digit code.",
        )
        PbTextField(
            value = "you@example.com",
            onValueChange = {},
            label = "Email",
            helper = "We’ll send a 6-digit code.",
            isError = true,
        )
        PbTextField(
            value = "",
            onValueChange = {},
            placeholder = "Search currencies",
            leadingIcon = PbIcon.Search,
        )
    }
}
