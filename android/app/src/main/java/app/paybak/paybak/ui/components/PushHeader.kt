package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import app.paybak.paybak.ui.theme.pbMaterial

/** The trailing action of a [PbPushHeader] (`Trailing`). Without one the header is back-only. */
sealed interface PbHeaderAction {
    /**
     * A glass capsule with a Headline label ("Save"). [wide] is for long labels ("Mark all read"):
     * 12 dp sides, at most 122 dp, and the title narrows to 102 dp so 8 dp always separate them.
     */
    data class Text(val label: String, val onClick: () -> Unit, val wide: Boolean = false) :
        PbHeaderAction

    /** A glass icon button (the gear on 10-01). */
    data class Icon(val icon: PbIcon, val contentDescription: String, val onClick: () -> Unit) :
        PbHeaderAction
}

/**
 * `Navigation / Push Header` (`PBPushHeader`): the 44 dp header of a pushed screen: the glass back
 * button, a centred Headline title and an optional glass action. On a long scrolling screen, pin it
 * and give it a `bg/primary` background. Parts are tagged "[testTag].back" and "[testTag].action".
 *
 * @param onBack Pops the screen, like system back (wire `BackHandler` to the same action).
 */
@Composable
fun PbPushHeader(
    title: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: PbHeaderAction? = null,
    testTag: String? = null,
) {
    Box(modifier.fillMaxWidth().height(PbSize.Tap).partTag(testTag)) {
        PbIconButton(
            PbIcon.ChevronLeft,
            contentDescription = stringResource(R.string.pb_back),
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart).partTag(testTag, "back"),
            style = PbIconButtonStyle.Glass,
        )
        if (title != null) {
            val wide = (action as? PbHeaderAction.Text)?.wide == true
            HeaderTitle(
                title,
                width = if (wide) 102.dp else 200.dp,
                Modifier.align(Alignment.Center),
            )
        }
        val actionModifier = Modifier.align(Alignment.CenterEnd).partTag(testTag, "action")
        when (action) {
            is PbHeaderAction.Text -> GlassTextAction(action, actionModifier)
            is PbHeaderAction.Icon ->
                PbIconButton(
                    action.icon,
                    contentDescription = action.contentDescription,
                    onClick = action.onClick,
                    modifier = actionModifier,
                    style = PbIconButtonStyle.Glass,
                )
            null -> Unit
        }
    }
}

/** The centred one-line Headline title of the push and modal headers. */
@Composable
internal fun HeaderTitle(title: String, width: Dp, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier.width(width).semantics { heading() },
        style = PbTextStyles.Headline,
        color = PbColors.Text.Primary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** The glass capsule text action; pressed fills it `bg/card`, like the glass icon button. */
@Composable
private fun GlassTextAction(action: PbHeaderAction.Text, modifier: Modifier = Modifier) {
    val press = rememberPressState(interactionSource = null)
    val fill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Card else PbColors.Bg.Glass,
            label = "Header action fill",
        )
    Box(
        modifier =
            modifier
                .height(PbSize.Tap)
                .widthIn(min = PbSize.Tap, max = if (action.wide) 122.dp else Dp.Unspecified)
                .pbMaterial(PbMaterial.GlassSmall, PbShapes.Pill, fill)
                .pressable(press, enabled = true, onClick = action.onClick)
                .padding(horizontal = if (action.wide) PbSpace.S12 else PbSpace.S16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = action.label,
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbPushHeaderPreview() {
    Column(Modifier.padding(PbSpace.S20), verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbPushHeader("Edit avatar", onBack = {}, action = PbHeaderAction.Text("Save", onClick = {}))
        PbPushHeader(
            "Goa Trip",
            onBack = {},
            action = PbHeaderAction.Icon(PbIcon.Settings, "Settings", onClick = {}),
        )
        PbPushHeader(
            "Notifications",
            onBack = {},
            action = PbHeaderAction.Text("Mark all read", onClick = {}, wide = true),
        )
    }
}
