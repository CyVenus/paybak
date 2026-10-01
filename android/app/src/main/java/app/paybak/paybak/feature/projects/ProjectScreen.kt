package app.paybak.paybak.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.data.ledger.lanes.addComponent
import app.paybak.paybak.data.ledger.lanes.archiveIfSettled
import app.paybak.paybak.data.ledger.lanes.deleteComponent
import app.paybak.paybak.data.ledger.lanes.editComponent
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.projects.ComponentForm
import app.paybak.paybak.domain.projects.ComponentRow
import app.paybak.paybak.domain.projects.PlanRow
import app.paybak.paybak.domain.projects.ProjectPage
import app.paybak.paybak.domain.projects.ProjectState
import app.paybak.paybak.domain.projects.TransferRole
import app.paybak.paybak.domain.projects.projectPage
import app.paybak.paybak.feature.groups.rememberAvatars
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.feature.settle.recordPaymentTo
import app.paybak.paybak.navigation.ActivityFilter
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.pinnedFooter
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbTitleHeader
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/** The white fade under the pinned button reaches this far above it (Figma: 96 from the bottom). */
private val FadeAboveButton = 10.dp

/**
 * The `project` route (screens-projects §3–§8): one dashboard in four states. Active shows the
 * budget with its projection, the parts, paid vs fair share, History and who owes whom, with Add
 * component pinned at the bottom; over budget turns the bar's end and the warning red. Closed locks
 * the parts and shows the final plan; once every payment in it is confirmed the project archives
 * into a read-only record with its members.
 */
@Composable
fun ProjectScreen(route: Route.Project) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val view = snapshot.view
    val projectId = route.groupId
    val page = remember(snapshot, projectId) { view.projectPage(projectId) }
    val start = rememberDebugStartScreen("projectAddComponent")
    var flow by
        rememberSaveable(stateSaver = ComponentFlow.Saver) {
            mutableStateOf(if (start != null) ComponentFlow() else null)
        }
    val archivedToast = stringResource(R.string.projects_toast_archived)
    val addedToast = stringResource(R.string.projects_toast_added)

    LaunchedEffect(page?.readyToArchive) {
        if (page?.readyToArchive == true) {
            ledger.archiveIfSettled(projectId)
            navigator.toast(archivedToast)
        }
    }

    PbPushedPage(
        id = "project",
        onBack = { navigator.back() },
        action =
            if (page?.editable == true) {
                PbHeaderAction.Icon(PbIcon.Settings, stringResource(R.string.projects_settings)) {
                    navigator.open(Route.ProjectSettings(projectId))
                }
            } else {
                null
            },
        actionTag = "settings",
        contentTop = PbSpace.S12,
        overlay = {
            if (page?.editable == true) {
                PinnedAddButton(Modifier.align(Alignment.BottomCenter)) {
                    flow = ComponentFlow()
                }
            }
        },
    ) {
        if (page == null) return@PbPushedPage
        Box(Modifier.testTag("project.state.${page.state.tagName}"))
        ProjectContent(
            page,
            view,
            onEdit = { row ->
                val form = ComponentForm.of(row.component, page.project.currency)
                flow = ComponentFlow(editingId = row.component.id, form = form)
            },
            onTransfer = { navigator.open(transferRoute(view, page, it)) },
            onHistory = {
                navigator.open(Route.ActivityLog(ActivityFilter.Project(projectId)))
            },
        )
        // Room for the pinned button: it rides 24 dp above the content's end.
        if (page.editable) Spacer(Modifier.height(PbSize.ButtonLg))
    }

    val current = flow ?: return
    val project = page?.project ?: return
    when (current.step) {
        ComponentStep.Editing ->
            ComponentSheet(
                current,
                currency = project.currency,
                members = project.memberIds,
                people = rememberPeopleDirectory(),
                nameOf = view::first,
                onChange = { flow = current.copy(form = it) },
                onSave = {
                    val editing = current.editingId
                    if (editing == null) {
                        ledger.addComponent(projectId, current.form)
                        navigator.toast(addedToast)
                    } else {
                        ledger.editComponent(editing, current.form)
                    }
                    flow = null
                },
                onDelete = { flow = current.copy(step = ComponentStep.Deleting) },
                onDismiss = {
                    flow =
                        if (current.dirty) current.copy(step = ComponentStep.Discarding) else null
                },
            )
        ComponentStep.Discarding ->
            PbAlert(
                title =
                    stringResource(
                        if (current.editingId == null) R.string.projects_discard_title
                        else R.string.projects_discard_changes_title
                    ),
                message =
                    stringResource(
                        if (current.editingId == null) R.string.projects_discard_message
                        else R.string.projects_discard_changes_message
                    ),
                cancelLabel = stringResource(R.string.projects_keep_editing),
                actionLabel = stringResource(R.string.projects_discard),
                onCancel = { flow = current.copy(step = ComponentStep.Editing) },
                onAction = { flow = null },
                testTag = "project.discardAlert",
            )
        ComponentStep.Deleting ->
            PbAlert(
                title = stringResource(R.string.projects_delete_title, current.initial.name),
                message = stringResource(R.string.projects_delete_message),
                cancelLabel = stringResource(R.string.projects_cancel),
                actionLabel = stringResource(R.string.projects_delete),
                onCancel = { flow = current.copy(step = ComponentStep.Editing) },
                onAction = {
                    current.editingId?.let(ledger::deleteComponent)
                    flow = null
                },
                testTag = "project.deleteAlert",
            )
    }
}

private val ProjectState.tagName: String
    get() =
        when (this) {
            ProjectState.Active -> "active"
            ProjectState.OverBudget -> "over"
            ProjectState.Closed -> "closed"
            ProjectState.Archived -> "archived"
        }

/** You pay: Record payment prefilled for the project. You're paid: Remind them about it. */
private fun transferRoute(view: LedgerView, page: ProjectPage, row: PlanRow): Route {
    val transfer = row.transfer
    val context = ReminderContext(groupId = page.project.id)
    return if (row.role == TransferRole.YouPay) {
        view.recordPaymentTo(transfer.creditorId, transfer.amount, page.project.currency, context)
    } else {
        Route.Remind(transfer.debtorId, context)
    }
}

/** The sections of the dashboard in the order its [ProjectPage.state] draws them (§10). */
@Composable
private fun ProjectContent(
    page: ProjectPage,
    view: LedgerView,
    onEdit: (ComponentRow) -> Unit,
    onTransfer: (PlanRow) -> Unit,
    onHistory: () -> Unit,
) {
    val project = page.project
    val people =
        (project.memberIds +
                page.components.mapNotNull { it.payerId } +
                page.plan.flatMap { listOf(it.transfer.debtorId, it.transfer.creditorId) })
            .distinct()
    val avatars = rememberAvatars(view, people)
    val avatarOf: (String) -> PbAvatarContent = { avatars[people.indexOf(it)] }
    Column(verticalArrangement = Arrangement.spacedBy(PbLayout.SectionGap)) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbTitleHeader(
                project.name,
                PbAvatarContent.Symbol(iconForKey(project.icon)),
                Modifier.testTag("project.title"),
                subtitle = page.subtitle,
                tag =
                    stringResource(R.string.projects_archived).takeIf {
                        page.state == ProjectState.Archived
                    },
                memberAvatars = avatars.take(project.memberIds.size),
            )
            page.notice?.let { NoticeCard(it, PbIcon.Lock, "project.notice") }
            BudgetSection(page.budget, closed = !page.editable)
        }
        if (page.editable) {
            Components(page, avatarOf, onEdit)
            page.shareRule?.let { rule ->
                ProjectSection(stringResource(R.string.projects_paid_vs_share)) {
                    FairShareCard(rule, page.shares, avatarOf)
                }
            }
            HistoryCard(onHistory)
            if (page.showsPlan) {
                Plan(stringResource(R.string.projects_who_owes_whom), page, avatarOf, onTransfer)
            }
        } else {
            Plan(stringResource(R.string.projects_final_plan), page, avatarOf, onTransfer)
            if (page.members.isNotEmpty()) {
                ProjectSection(stringResource(R.string.projects_members)) {
                    MembersCard(page.members, avatarOf)
                }
            }
            if (page.components.isNotEmpty()) Components(page, avatarOf, onEdit = null)
            HistoryCard(onHistory)
        }
    }
}

@Composable
private fun Components(
    page: ProjectPage,
    avatarOf: (String) -> PbAvatarContent,
    onEdit: ((ComponentRow) -> Unit)?,
) {
    ProjectSection(stringResource(R.string.projects_components)) {
        ComponentsCard(page.components, avatarOf, onEdit)
    }
}

/** Who owes whom (or the final plan): the transfers, or "Everyone is settled", and the footnote. */
@Composable
private fun Plan(
    title: String,
    page: ProjectPage,
    avatarOf: (String) -> PbAvatarContent,
    onTransfer: (PlanRow) -> Unit,
) {
    ProjectSection(title) {
        page.planNotice?.let { NoticeCard(it, PbIcon.CheckCircle, "project.planNotice") }
        if (page.plan.isNotEmpty()) PlanCard(page.plan, avatarOf, onTransfer)
        page.footnote?.let { Footnote(it) }
    }
}

/**
 * Add component, pinned to the bottom of the screen over a white fade that starts 10 dp above it
 * (§3.9). The fade takes no taps.
 */
@Composable
private fun PinnedAddButton(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier.matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to PbColors.Bg.Primary.copy(alpha = 0f),
                        0.45f to PbColors.Bg.Primary.copy(alpha = 0.85f),
                        1f to PbColors.Bg.Primary,
                    )
                )
        )
        Column(
            Modifier.widthIn(max = PbLayout.MaxContentWidth)
                .align(Alignment.TopCenter)
                .padding(horizontal = PbLayout.ScreenMargin)
                .padding(top = FadeAboveButton)
        ) {
            PbButton(
                stringResource(R.string.projects_add_component),
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().testTag("project.addComponent").pinnedFooter(),
                leadingIcon = PbIcon.Plus,
            )
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
