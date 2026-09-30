package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Figma's smart animate from Pending to Saved: 250 ms, ease out. */
private const val SAVE_MILLIS = 250

/**
 * `Chat / Draft Expense` (`PBDraftExpenseCard`): the expense Ask Paybak drafted: category icon,
 * title and amount, then who paid, the split with its members and each share. Pending offers Save
 * and Edit; once [saved], "Expense added" with View replaces them in 250 ms (instant under reduce
 * motion). Nothing is saved without the tap, and the chat stays put: no toast, no navigation. Parts
 * are tagged "[testTag].save", "[testTag].edit" and "[testTag].view".
 *
 * @param members 2–4 heads for the split's avatar stack.
 */
@Composable
fun PbDraftExpenseCard(
    title: String,
    amount: String,
    icon: PbIcon,
    paidLine: String,
    splitLine: String,
    eachLine: String,
    members: List<PbPeepHead>,
    saved: Boolean,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val millis = if (LocalReduceMotion.current) 0 else SAVE_MILLIS
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .partTag(testTag)
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(PbAvatarContent.Symbol(icon), onCard = true)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(amount, style = PbTextStyles.AmountMedium, color = PbColors.Text.Primary)
        }
        PbDivider()
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Text(paidLine, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    splitLine,
                    Modifier.weight(1f),
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                )
                PbAvatarStack(members)
            }
            Text(eachLine, style = PbTextStyles.Subheadline, color = PbColors.Text.Primary)
        }
        AnimatedContent(
            targetState = saved,
            transitionSpec = {
                fadeIn(tween(millis, easing = PbMotion.EaseOut)) togetherWith
                    fadeOut(tween(millis, easing = PbMotion.EaseOut)) using
                    SizeTransform { _, _ -> tween(millis, easing = PbMotion.EaseOut) }
            },
            label = "Draft expense actions",
        ) { done ->
            if (done) {
                SavedStatus(onView, testTag)
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                ) {
                    PbButton(
                        stringResource(R.string.pb_save),
                        onClick = onSave,
                        modifier = Modifier.weight(1f).partTag(testTag, "save"),
                        size = PbButtonSize.Small,
                    )
                    PbButton(
                        stringResource(R.string.pb_edit),
                        onClick = onEdit,
                        modifier = Modifier.partTag(testTag, "edit"),
                        style = PbButtonStyle.OnCard,
                        size = PbButtonSize.Small,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedStatus(onView: () -> Unit, testTag: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(PbSize.Tap),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbIconImage(PbIcon.CheckCircle, contentDescription = null, size = PbSize.IconMd)
        Text(
            text = stringResource(R.string.pb_expense_added),
            modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
            maxLines = 1,
        )
        PbTextButton(
            stringResource(R.string.pb_view),
            onClick = onView,
            modifier = Modifier.partTag(testTag, "view"),
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbDraftExpenseCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        listOf(false, true).forEach { saved ->
            PbDraftExpenseCard(
                title = "Cab",
                amount = "₹600",
                icon = PbIcon.Car,
                paidLine = "Paid by you · Today",
                splitLine = "Split equally with Esha and Dev",
                eachLine = "₹200 each",
                members = listOf(PbPeepHead.Arjun, PbPeepHead.Esha, PbPeepHead.Dev),
                saved = saved,
                onSave = {},
                onEdit = {},
                onView = {},
            )
        }
    }
}
