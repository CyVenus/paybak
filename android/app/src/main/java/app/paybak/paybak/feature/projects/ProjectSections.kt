package app.paybak.paybak.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.projects.BudgetFigures
import app.paybak.paybak.domain.projects.ComponentRow
import app.paybak.paybak.domain.projects.MemberRow
import app.paybak.paybak.domain.projects.PlanRow
import app.paybak.paybak.domain.projects.ProjectNotice
import app.paybak.paybak.domain.projects.ShareRow
import app.paybak.paybak.domain.projects.TransferRole
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbBarRow
import app.paybak.paybak.ui.components.PbBudgetCard
import app.paybak.paybak.ui.components.PbBudgetState
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbRowSurface
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbTransferRow
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** A titled section of the dashboard: the Title/3 header 8 dp above its card. */
@Composable
internal fun ProjectSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbSectionHeader(title)
        content()
    }
}

/** `Card / Budget` in its state, or just "Spent" for a project without a budget (§1.2). */
@Composable
internal fun BudgetSection(figures: BudgetFigures, closed: Boolean) {
    val modifier = Modifier.testTag("project.budget")
    if (figures.budget == null) {
        Column(
            modifier
                .fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
        ) {
            Text(
                stringResource(R.string.pb_spent),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
            Text(
                figures.spent,
                Modifier.testTag("project.budget.spent"),
                style = PbTextStyles.Title1,
                color = PbColors.Text.Primary,
            )
        }
        return
    }
    val percent = figures.percent.orEmpty()
    val left = figures.left.orEmpty()
    PbBudgetCard(
        spent = figures.spent,
        budget = figures.budget,
        progress = figures.progress,
        state =
            when {
                figures.over -> PbBudgetState.Over(left)
                closed -> PbBudgetState.Closed(percent, left)
                else -> PbBudgetState.OnTrack(percent, left, figures.projected)
            },
        modifier = modifier,
        planned = figures.planned,
    )
}

/** The lock notice of a closed or archived project, and the final plan's "Everyone is settled". */
@Composable
internal fun NoticeCard(notice: ProjectNotice, icon: PbIcon, testTag: String) {
    PbNoticeCard(
        body = notice.body,
        icon = icon,
        modifier = Modifier.testTag(testTag),
        title = notice.title,
    )
}

/**
 * The Components card: planned parts first with the tag icon and "—", then bought and done ones
 * with their payer's head (§3.5). Rows open the Edit component sheet while [onEdit] is set.
 */
@Composable
internal fun ComponentsCard(
    rows: List<ComponentRow>,
    avatarOf: (String) -> PbAvatarContent,
    onEdit: ((ComponentRow) -> Unit)?,
) {
    PbCard {
        if (rows.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(PbSpace.S16),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.projects_no_components),
                    Modifier.testTag("project.components.empty"),
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                    textAlign = TextAlign.Center,
                )
            }
            return@PbCard
        }
        rows.forEachIndexed { index, row ->
            val planned = row.payerId == null
            PbActivityRow(
                leading = row.payerId?.let(avatarOf) ?: PbAvatarContent.Symbol(PbIcon.Tag),
                title = row.component.name,
                modifier = Modifier.padding(horizontal = PbSpace.S16),
                subtitle = row.subtitle,
                amount = row.amount,
                amountColor = if (planned) PbColors.Text.Tertiary else PbColors.Text.Primary,
                badge = stringResource(row.component.status.labelRes()),
                badgeStyle =
                    when (row.component.status) {
                        ComponentStatus.Planned -> PbBadgeStyle.MutedOnCard
                        ComponentStatus.Bought -> PbBadgeStyle.OnCard
                        ComponentStatus.Done -> PbBadgeStyle.Inverse
                    },
                showDivider = index < rows.lastIndex,
                surface = PbRowSurface.OnCard,
                onClick = onEdit?.let { edit -> { edit(row) } },
                testTag = "project.component.${row.component.id}",
            )
        }
    }
}

internal fun ComponentStatus.labelRes(): Int =
    when (this) {
        ComponentStatus.Planned -> R.string.projects_status_planned
        ComponentStatus.Bought -> R.string.projects_status_bought
        ComponentStatus.Done -> R.string.projects_status_done
    }

/** "Paid vs fair share": the rule line, then each member's paid bar with the fair-share mark. */
@Composable
internal fun FairShareCard(
    rule: String,
    rows: List<ShareRow>,
    avatarOf: (String) -> PbAvatarContent,
) {
    Column(
        Modifier.fillMaxWidth()
            .background(PbColors.Bg.Card, PbShapes.Card)
            .padding(PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        Text(
            rule,
            Modifier.testTag("project.shareRule"),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
        )
        Column {
            rows.forEach { row ->
                PbBarRow(
                    title = row.name,
                    amount = row.value,
                    progress = row.fill,
                    leading = avatarOf(row.personId),
                    modifier = Modifier.testTag("project.share.${row.personId}"),
                    caption = row.caption,
                    balance =
                        when (row.standing) {
                            Standing.Owed -> PbBalance.Owed
                            Standing.Owe -> PbBalance.Owe
                            Standing.Settled -> null
                        },
                    mark = row.mark,
                    onCard = true,
                )
            }
        }
    }
}

/** The History card: opens the Activity log filtered to the project. */
@Composable
internal fun HistoryCard(onClick: () -> Unit) {
    PbCard {
        PbSettingRow(
            stringResource(R.string.projects_history),
            Modifier.testTag("project.history"),
            onClick = onClick,
            icon = PbIcon.Activity,
            showDivider = false,
        )
    }
}

/**
 * "Who owes whom" or the final plan's transfers, then the footnote. Rows you're part of open
 * Record payment (you pay) or Remind (you're paid); others' debts aren't tappable.
 */
@Composable
internal fun PlanCard(
    rows: List<PlanRow>,
    avatarOf: (String) -> PbAvatarContent,
    onTap: (PlanRow) -> Unit,
) {
    PbCard {
        rows.forEachIndexed { index, row ->
            PbTransferRow(
                title = row.title,
                amount = row.amount,
                from = avatarOf(row.transfer.debtorId),
                to = avatarOf(row.transfer.creditorId),
                modifier = Modifier.testTag("project.transfer.$index"),
                showDivider = index < rows.lastIndex,
                onClick =
                    if (row.role == TransferRole.Others) null
                    else ({ onTap(row) }),
            )
        }
    }
}

@Composable
internal fun Footnote(text: String) {
    Text(
        text,
        Modifier.testTag("project.footnote"),
        style = PbTextStyles.Footnote,
        color = PbColors.Text.Secondary,
    )
}

/** An archived project's members and where each ended up ("Settled"). */
@Composable
internal fun MembersCard(rows: List<MemberRow>, avatarOf: (String) -> PbAvatarContent) {
    PbCard {
        rows.forEachIndexed { index, row ->
            PbPersonRow(
                name = row.name,
                avatar = avatarOf(row.personId),
                modifier = Modifier.testTag("project.members.${row.personId}"),
                trailing = PbPersonTrailing.Status(row.status),
                size = PbPersonRowSize.Compact,
                showDivider = index < rows.lastIndex,
            )
        }
    }
}
