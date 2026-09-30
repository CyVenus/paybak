package app.paybak.paybak.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Type` of `Card / Balance`. */
enum class PbBalanceType(val icon: PbIcon, @param:StringRes val label: Int) {
    Owed(PbIcon.MoneyIn, R.string.pb_you_are_owed),
    Owe(PbIcon.MoneyOut, R.string.pb_you_owe),
    Settled(PbIcon.CheckCircle, R.string.pb_all_settled),
}

/**
 * `Card / Balance` (`PBBalanceCard`): a total on a #F5F5F5 card. Owed is black, Owe grey (with
 * U+2212), Settled light grey. A tap opens its breakdown. [badge] (top right, instead of the
 * chevron) and [action] ("Settle up") are off by default. Fill the width with [modifier].
 */
@Composable
fun PbBalanceCard(
    type: PbBalanceType,
    amount: String,
    caption: String,
    modifier: Modifier = Modifier,
    label: String = stringResource(type.label),
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true,
    badge: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
    testTag: String? = null,
) {
    val press = rememberPressState(null)
    val fill =
        animatePressColor(
            if (press.isPressed && onClick != null) PbColors.Bg.CardPressed else PbColors.Bg.Card,
            "Balance card",
        )
    Box(
        modifier
            .partTag(testTag)
            .clip(PbShapes.Card)
            .background(fill)
            .then(
                if (onClick != null) Modifier.pressable(press, enabled = true, onClick = onClick)
                else Modifier
            )
            .semantics(mergeDescendants = true) {}
            .padding(PbSpace.S16)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbIconImage(
                    type.icon,
                    contentDescription = null,
                    size = PbSize.IconSm,
                    tint = PbColors.Icon.Secondary,
                )
                Text(
                    label,
                    Modifier.weight(1f),
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (badge == null && showChevron && onClick != null) {
                    PbIconImage(
                        PbIcon.ChevronRight,
                        contentDescription = null,
                        size = PbSize.IconSm,
                        tint = PbColors.Icon.Tertiary,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    amount,
                    style = PbTextStyles.AmountLarge,
                    color =
                        when (type) {
                            PbBalanceType.Owed -> PbColors.Text.Primary
                            PbBalanceType.Owe -> PbColors.Text.Secondary
                            PbBalanceType.Settled -> PbColors.Text.Tertiary
                        },
                    maxLines = 1,
                )
                Text(
                    caption,
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (badge != null)
            PbBadge(badge, Modifier.align(Alignment.TopEnd), style = PbBadgeStyle.Overdue)
        if (action != null) {
            PbButton(
                action,
                onClick = onAction,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = PbSpace.S8),
                size = PbButtonSize.Small,
            )
        }
    }
}

/**
 * `Card / Balance Summary` (`PBBalanceSummary`): Home's Owed and Owe cards side by side and the
 * Settle up button under them. Parts are tagged "[testTag].owed", ".owe" and ".settleUp".
 */
@Composable
fun PbBalanceSummary(
    owedAmount: String,
    owedCaption: String,
    oweAmount: String,
    oweCaption: String,
    onOwed: () -> Unit,
    onOwe: () -> Unit,
    modifier: Modifier = Modifier,
    onSettleUp: (() -> Unit)? = null,
    testTag: String? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            PbBalanceCard(
                PbBalanceType.Owed,
                owedAmount,
                owedCaption,
                Modifier.weight(1f),
                onClick = onOwed,
                testTag = testTag?.let { "$it.owed" },
            )
            PbBalanceCard(
                PbBalanceType.Owe,
                oweAmount,
                oweCaption,
                Modifier.weight(1f),
                onClick = onOwe,
                testTag = testTag?.let { "$it.owe" },
            )
        }
        if (onSettleUp != null) {
            PbButton(
                stringResource(R.string.pb_settle_up),
                onClick = onSettleUp,
                modifier = Modifier.fillMaxWidth().partTag(testTag, "settleUp"),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbBalanceSummaryPreview() {
    Column(Modifier.padding(PbSpace.S20), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbBalanceSummary(
            "+₹2,900",
            "from 4 people",
            "−₹1,850",
            "across 2 groups",
            {},
            {},
            onSettleUp = {},
        )
        PbBalanceCard(PbBalanceType.Settled, "₹0", "Nothing pending", Modifier.fillMaxWidth())
        PbBalanceCard(
            PbBalanceType.Owed,
            "+₹800",
            "Movie tickets",
            Modifier.fillMaxWidth(),
            label = "Rohan owes you",
            badge = "Overdue 3 days",
        )
    }
}
