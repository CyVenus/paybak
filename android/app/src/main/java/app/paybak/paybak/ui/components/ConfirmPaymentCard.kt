package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Figma's smart animate from Pending to Confirmed: 250 ms, ease out. */
private const val CONFIRM_MILLIS = 250

/**
 * `Card / Confirm Payment` (`PBConfirmPaymentCard`): the receiver's confirm card on Home and in
 * Activity. Pending ("Esha says she paid you ₹700") offers Confirm and Not received; once
 * [confirmed] the buttons collapse (120 → 72 dp), the texts cross-fade to the confirmed ones and
 * the check fades in, in 250 ms (instant under reduce motion). Parts are tagged "[testTag].confirm"
 * and "[testTag].notReceived".
 */
@Composable
fun PbConfirmPaymentCard(
    avatar: PbAvatarContent,
    title: String,
    detail: String,
    confirmedTitle: String,
    confirmedDetail: String,
    confirmed: Boolean,
    onConfirm: () -> Unit,
    onNotReceived: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val millis = if (LocalReduceMotion.current) 0 else CONFIRM_MILLIS
    Column(
        modifier
            .fillMaxWidth()
            .partTag(testTag)
            .clip(PbShapes.Card)
            .background(PbColors.Bg.Card)
            .padding(PbSpace.S16)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(avatar, onCard = true)
            Crossfade(
                targetState = confirmed,
                modifier =
                    Modifier.weight(1f).semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                    },
                animationSpec = tween(millis),
                label = "Confirm payment texts",
            ) { done ->
                Column {
                    Text(
                        text = if (done) confirmedTitle else title,
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (done) confirmedDetail else detail,
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AnimatedVisibility(
                visible = confirmed,
                enter = fadeIn(tween(millis)),
                exit = fadeOut(tween(millis)),
            ) {
                PbIconImage(PbIcon.CheckCircle, contentDescription = null)
            }
        }
        AnimatedVisibility(
            visible = !confirmed,
            enter =
                fadeIn(tween(millis)) + expandVertically(tween(millis, easing = PbMotion.EaseOut)),
            exit =
                fadeOut(tween(millis)) + shrinkVertically(tween(millis, easing = PbMotion.EaseOut)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = PbSpace.S12),
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
            ) {
                PbButton(
                    stringResource(R.string.pb_confirm),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).partTag(testTag, "confirm"),
                    size = PbButtonSize.Small,
                )
                PbButton(
                    stringResource(R.string.pb_not_received),
                    onClick = onNotReceived,
                    modifier = Modifier.weight(1f).partTag(testTag, "notReceived"),
                    style = PbButtonStyle.OnCard,
                    size = PbButtonSize.Small,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbConfirmPaymentCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        listOf(false, true).forEach { confirmed ->
            PbConfirmPaymentCard(
                avatar = PbAvatarContent.Art(PbPeepHead.Esha),
                title = "Esha says she paid you ₹700",
                detail = "Dinner at Olive Garden · UPI · 9:12 pm",
                confirmedTitle = "Esha paid you ₹700",
                confirmedDetail = "Dinner at Olive Garden · UPI · Confirmed",
                confirmed = confirmed,
                onConfirm = {},
                onNotReceived = {},
            )
        }
    }
}
