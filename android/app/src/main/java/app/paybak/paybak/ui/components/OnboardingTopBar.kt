package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `Navigation / Onboarding Top Bar` (`PBOnboardingTopBar`): an optional back chevron on the left
 * and an optional Skip on the right. It keeps its 44 dp height when both are hidden, so nothing
 * below moves when Skip disappears on the last Welcome step.
 *
 * @param skipModifier Applied to the Skip button, e.g. its test tag.
 */
@Composable
fun PbOnboardingTopBar(
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showSkip: Boolean = false,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    skipModifier: Modifier = Modifier,
) {
    NavigationRow(modifier) {
        if (showBack) PbBackButton(onBack)
        Spacer(Modifier.weight(1f))
        SkipButton(visible = showSkip, onClick = onSkip, modifier = skipModifier)
    }
}

/** The 44 dp row both navigation headers share. */
@Composable
internal fun NavigationRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(PbSize.Tap),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** `Button / Icon` Plain with the chevron: the in-app back button. */
@Composable
internal fun PbBackButton(onClick: () -> Unit) {
    PbIconButton(
        PbIcon.ChevronLeft,
        contentDescription = stringResource(R.string.pb_back),
        onClick = onClick,
    )
}

/**
 * Secondary text button "Skip" that fades in and out in place. It stops taking taps as soon as it
 * starts to hide.
 */
@Composable
internal fun SkipButton(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(PbMotion.SWAP_MILLIS)),
        exit = fadeOut(tween(PbMotion.SWAP_MILLIS)),
    ) {
        PbTextButton(
            stringResource(R.string.pb_skip),
            onClick = { if (visible) onClick() },
            modifier = modifier,
            style = PbTextButtonStyle.Secondary,
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbOnboardingTopBarPreview() {
    Column(Modifier.width(362.dp), verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbOnboardingTopBar(showSkip = true)
        PbOnboardingTopBar(showBack = true)
    }
}
