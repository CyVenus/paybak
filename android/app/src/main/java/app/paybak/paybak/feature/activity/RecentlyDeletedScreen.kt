package app.paybak.paybak.feature.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.restoreExpense
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.data.ledger.view
import app.paybak.paybak.domain.calc.DeletedRow
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbRowSurface
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `recentlyDeleted` route (activity §5): deleted expenses wait here for 30 days, newest
 * deletion first, and anyone in the group can restore them. Restore counts the expense again
 * everywhere and logs it in its History.
 */
@Composable
fun RecentlyDeletedScreen(route: Route.RecentlyDeleted) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val rows = snapshot.recentlyDeleted
    PbPinnedHeaderScreen(
        testTag = "screen.recentlyDeleted",
        header = {
            PbPushHeader(
                stringResource(R.string.shell_recently_deleted),
                onBack = { navigator.back() },
                testTag = "recentlyDeleted",
            )
        },
    ) {
        Text(
            stringResource(R.string.activity_deleted_helper),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
        )
        if (rows.isEmpty()) {
            Text(
                stringResource(R.string.activity_deleted_empty),
                Modifier.fillMaxWidth().testTag("recentlyDeleted.empty"),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
            return@PbPinnedHeaderScreen
        }
        PbCard(Modifier.testTag("recentlyDeleted.list")) {
            Column(Modifier.padding(vertical = PbSpace.S8)) { DeletedRows(rows) }
        }
    }
}

private const val NBSP = '\u00A0'

/** One On Card row per deleted expense, its Restore button on the right (activity §5). */
@Composable
private fun DeletedRows(rows: List<DeletedRow>) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val restored = stringResource(R.string.activity_toast_restored)
    rows.forEachIndexed { index, row ->
        val category = Category.of(ledger.view.expense(row.expenseId)?.category.orEmpty())
        PbActivityRow(
            leading = PbAvatarContent.Symbol(category.pbIcon),
            title = row.title,
            modifier = Modifier.padding(horizontal = PbSpace.S16),
            subtitle = row.caption,
            // "24 days left" wraps as one piece, as in Figma.
            detail = row.detail.replace(row.daysLeft, row.daysLeft.replace(' ', NBSP)),
            action = stringResource(R.string.activity_restore),
            onAction = {
                ledger.restoreExpense(row.expenseId)
                navigator.toast(restored)
            },
            actionTestTag = "recentlyDeleted.restore.${row.expenseId}",
            showDivider = index < rows.lastIndex,
            surface = PbRowSurface.OnCard,
            testTag = "recentlyDeleted.row.${row.expenseId}",
        )
    }
}
