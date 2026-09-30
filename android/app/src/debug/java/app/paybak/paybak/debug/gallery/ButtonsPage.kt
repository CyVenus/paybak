package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbAddButton
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbIconButtonStyle
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.PbTextButtonStyle
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

@Composable
internal fun ButtonsPage() {
    GalleryPage {
        PbButtonStyle.entries.forEach { style ->
            GallerySection("Button / ${style.name}") {
                val label = if (style == PbButtonStyle.Destructive) "Delete" else "Continue"
                val content =
                    @Composable {
                        PbButtonSize.entries.forEach { size ->
                            StateRow { state ->
                                PbButton(
                                    label = label,
                                    onClick = {},
                                    style = style,
                                    size = size,
                                    enabled = state != ControlState.Disabled,
                                    interactionSource =
                                        if (state == ControlState.Pressed) rememberPressedSource()
                                        else null,
                                )
                            }
                        }
                        StateRow { state ->
                            PbButton(
                                label = label,
                                onClick = {},
                                style = style,
                                size = PbButtonSize.Small,
                                leadingIcon = PbIcon.Plus,
                                enabled = state != ControlState.Disabled,
                                interactionSource =
                                    if (state == ControlState.Pressed) rememberPressedSource()
                                    else null,
                            )
                        }
                    }
                if (style == PbButtonStyle.OnCard) OnCard(content) else content()
            }
        }
        GallerySection("Stretched with brand icons (Get Started)") {
            PbButton(
                "Continue with Apple",
                onClick = {},
                Modifier.fillMaxWidth(),
                leadingIcon = PbIcon.Apple,
            )
            PbButton(
                "Continue with Google",
                onClick = {},
                Modifier.fillMaxWidth(),
                style = PbButtonStyle.Secondary,
                leadingIcon = PbIcon.Google,
            )
        }
        GallerySection("Button / Text") {
            PbTextButtonStyle.entries.forEach { style ->
                StateRow { state ->
                    PbTextButton(
                        label = if (style == PbTextButtonStyle.Secondary) "Skip" else "See all",
                        onClick = {},
                        style = style,
                        trailingChevron = style != PbTextButtonStyle.Secondary,
                        enabled = state != ControlState.Disabled,
                        interactionSource =
                            if (state == ControlState.Pressed) rememberPressedSource() else null,
                    )
                }
            }
        }
        GallerySection("Button / Icon: default · pressed · badge") {
            PbIconButtonStyle.entries.forEach { style ->
                val content =
                    @Composable {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PbIconButton(PbIcon.Bell, "Notifications", onClick = {}, style = style)
                            PbIconButton(
                                PbIcon.Bell,
                                "Notifications",
                                onClick = {},
                                style = style,
                                interactionSource = rememberPressedSource(),
                            )
                            PbIconButton(
                                PbIcon.Bell,
                                "Notifications",
                                onClick = {},
                                style = style,
                                badge = true,
                            )
                            GalleryLabel(style.name)
                        }
                    }
                if (style == PbIconButtonStyle.Glass) GlassBackdrop(content) else content()
            }
        }
        GallerySection("Button / Add: default · pressed") {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
                PbAddButton(onClick = {})
                PbAddButton(onClick = {}, interactionSource = rememberPressedSource())
            }
        }
    }
}

internal enum class ControlState {
    Default,
    Pressed,
    Disabled,
}

/** One sample per [ControlState], labelled; wraps when the samples are wide. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StateRow(sample: @Composable (ControlState) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        ControlState.entries.forEach { state ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                sample(state)
                GalleryLabel(state.name)
            }
        }
    }
}

/** Diagonal stripes behind glass buttons, so the translucent fill and highlight show. */
@Composable
private fun GlassBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(76.dp).stripes(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.padding(horizontal = PbSpace.S12)) { content() }
    }
}
