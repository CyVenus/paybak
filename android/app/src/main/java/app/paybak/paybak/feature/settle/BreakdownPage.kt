package app.paybak.paybak.feature.settle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.view
import app.paybak.paybak.domain.settle.Breakdown
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The page behind a Home balance card (screens-settle §1–2): the total with Home's caption, a card
 * of people (each opens their friend page), the footnotes, and Settle up pinned at the bottom. The
 * total is black for money owed to you, grey for money you owe, and light grey when it's ₹0.
 *
 * @param id The route id: the root is tagged `screen.[id]`, rows "[id].row.<friendId>" and the
 *   button "[id].settleUp".
 */
@Composable
internal fun BreakdownPage(
    id: String,
    title: String,
    section: String,
    breakdown: Breakdown,
    balance: PbBalance,
) {
    val navigator = LocalMainNavigator.current
    val view = LocalLedger.current.view
    PbPushedPage(
        id = id,
        onBack = { navigator.back() },
        title = title,
        contentTop = PbSpace.S24,
        overlay = {
            PbButton(
                stringResource(R.string.pb_settle_up),
                onClick = { navigator.open(Route.SettleUp()) },
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .widthIn(max = PbLayout.MaxContentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = PbLayout.ScreenMargin)
                        .testTag("$id.settleUp"),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
            Text(
                breakdown.total,
                Modifier.testTag("$id.total"),
                style = PbTextStyles.Title1,
                color =
                    when {
                        breakdown.isEmpty -> PbColors.Text.Tertiary
                        balance == PbBalance.Owe -> PbColors.Text.Secondary
                        else -> PbColors.Text.Primary
                    },
            )
            Text(breakdown.caption, style = PbTextStyles.Body, color = PbColors.Text.Secondary)
        }
        if (!breakdown.isEmpty) {
            Spacer(Modifier.height(PbSpace.S24))
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                PbSectionHeader(section)
                PbCard {
                    breakdown.rows.forEachIndexed { index, row ->
                        PbPersonRow(
                            row.name,
                            view.person(row.friendId)?.avatarContent()
                                ?: PbAvatarContent.Initials(row.name.take(1)),
                            Modifier.testTag("$id.row.${row.friendId}"),
                            subtitle = row.subtitle,
                            trailing =
                                PbPersonTrailing.Amount(
                                    row.amount,
                                    balance,
                                    label = row.dueLabel,
                                    overdue = row.overdue,
                                ),
                            onCard = true,
                            onClick = { navigator.open(Route.Friend(row.friendId)) },
                            showDivider = index < breakdown.rows.lastIndex,
                        )
                    }
                }
                breakdown.footnotes.forEach {
                    Text(it, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
                }
            }
        }
        // Room for the pinned Settle up, so the last row scrolls clear of it.
        Spacer(Modifier.height(PbSize.ButtonLg))
    }
}
