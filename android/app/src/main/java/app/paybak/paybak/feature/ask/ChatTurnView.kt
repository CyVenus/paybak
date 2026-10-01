package app.paybak.paybak.feature.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.ask.AskChip
import app.paybak.paybak.domain.ask.AskPrompt
import app.paybak.paybak.domain.ask.DraftCard
import app.paybak.paybak.domain.ask.OwedPerson
import app.paybak.paybak.domain.ask.PromptKind
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.insights.InsightRow
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBarRow
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbChatBubble
import app.paybak.paybak.ui.components.PbChatRole
import app.paybak.paybak.ui.components.PbDraftExpenseCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.components.rememberUserAvatar
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** What the chat's cards and chips do; [index] is the turn's place in the chat. */
internal interface TurnActions {
    fun save(index: Int)

    fun edit(index: Int)

    fun view(expenseId: String)

    fun person(personId: String)

    fun prompt(prompt: AskPrompt)

    fun chip(chip: AskChip)
}

/**
 * One exchange (insights §3.3–3.4): your bubble and the reply 16 below it, then what came with the
 * reply 12 apart: the people card, a share bar, the drafted reminder, the drafted expense, the
 * suggestions again, and the action chips. Parts are tagged `ask.message.<n>` (your question is
 * 2n, the reply 2n + 1), `ask.answerCard`, `ask.draft` and `ask.chip.remind.<name>`.
 */
@Composable
internal fun ChatTurnView(index: Int, turn: ChatTurn, actions: TurnActions) {
    val answer = turn.answer
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbChatBubble(turn.question, PbChatRole.User, Modifier.testTag("ask.message.${index * 2}"))
            PbChatBubble(
                answer.text,
                PbChatRole.Assistant,
                Modifier.testTag("ask.message.${index * 2 + 1}"),
            )
        }
        if (answer.people.isNotEmpty()) PeopleCard(answer.people, actions::person)
        answer.share?.let { ShareCard(it) }
        answer.message?.let { MessageCard(it) }
        answer.draft?.let { DraftExpense(it, index, turn.savedExpenseId, actions) }
        if (answer.suggestions.isNotEmpty()) PromptCard(answer.suggestions, actions::prompt)
        if (answer.chips.isNotEmpty()) Chips(answer.chips, actions::chip)
    }
}

/** Who owes you: a compact row per person, the overdue badge under the amount. */
@Composable
private fun PeopleCard(people: List<OwedPerson>, onPerson: (String) -> Unit) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val currency = LocalLedger.current.defaultCurrency
    PbCard(Modifier.testTag("ask.answerCard")) {
        people.forEachIndexed { i, owed ->
            val person = snapshot.view.person(owed.personId) ?: return@forEachIndexed
            PbPersonRow(
                name = person.firstName,
                avatar = person.avatarContent(),
                trailing =
                    PbPersonTrailing.Amount(
                        Money.format(owed.amount, currency),
                        overdue = owed.overdue,
                    ),
                size = PbPersonRowSize.Compact,
                onClick = { onPerson(owed.personId) },
                showDivider = i < people.lastIndex,
            )
        }
    }
}

/** A category's bar ("How much did I spend on food…"). */
@Composable
private fun ShareCard(row: InsightRow) {
    val currency = LocalLedger.current.defaultCurrency
    PbCard(Modifier.testTag("ask.answerCard")) {
        PbBarRow(
            title = row.label,
            amount = Money.format(row.amount, currency),
            progress = row.percent / 100f,
            leading = PbAvatarContent.Symbol(iconForKey(row.icon ?: "tag")),
            modifier = Modifier.padding(horizontal = PbSpace.S16),
            caption = "${row.percent}%",
            onCard = true,
        )
    }
}

/** The drafted reminder, as the Remind sheet will send it. */
@Composable
private fun MessageCard(message: String) {
    PbCard(Modifier.testTag("ask.reminder")) {
        Text(
            message,
            Modifier.padding(PbSpace.S16),
            style = PbTextStyles.Body,
            color = PbColors.Text.Primary,
        )
    }
}

@Composable
private fun DraftExpense(card: DraftCard, index: Int, savedId: String?, actions: TurnActions) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val store = LocalProfileStore.current
    val profile by store.profile.collectAsState()
    val you = rememberUserAvatar(profile, store)
    PbDraftExpenseCard(
        title = card.draft.title,
        amount = card.amountText,
        icon = iconForKey(card.categoryIcon),
        paidLine = card.paidLine,
        splitLine = card.splitLine,
        eachLine = card.eachLine,
        members =
            card.personIds.take(MAX_STACK).map { id ->
                if (id == ME) you
                else snapshot.view.person(id)?.avatarContent() ?: PbAvatarContent.Initials("?")
            },
        saved = savedId != null,
        onSave = { actions.save(index) },
        onEdit = { actions.edit(index) },
        onView = { savedId?.let(actions::view) },
        testTag = "ask.draft",
    )
}

/** The suggested prompts: tapping one sends it. Tagged `ask.prompt.<n>`. */
@Composable
internal fun PromptCard(prompts: List<AskPrompt>, onPrompt: (AskPrompt) -> Unit) {
    PbCard {
        prompts.forEachIndexed { i, prompt ->
            PbSettingRow(
                prompt.text,
                Modifier.testTag("ask.prompt.$i"),
                onClick = { onPrompt(prompt) },
                icon = prompt.kind.icon,
                showDivider = i < prompts.lastIndex,
                titleMaxLines = 2,
            )
        }
    }
}

private val PromptKind.icon: PbIcon
    get() =
        when (this) {
            PromptKind.WhoOwesMe -> PbIcon.People
            PromptKind.Spend -> PbIcon.Food
            PromptKind.Due -> PbIcon.Calendar
            PromptKind.Reminder -> PbIcon.Bell
        }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chips(chips: List<AskChip>, onChip: (AskChip) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        chips.forEach { chip ->
            val (label, icon, tag) =
                when (chip) {
                    is AskChip.Remind ->
                        Triple(
                            stringResource(R.string.insights_ask_remind, chip.name),
                            PbIcon.Bell,
                            "ask.chip.remind.${chip.name.lowercase()}",
                        )
                    is AskChip.SeeInsights ->
                        Triple(stringResource(R.string.insights_ask_see_insights), PbIcon.Chart, "ask.chip.insights")
                    is AskChip.SettleUp ->
                        Triple(stringResource(R.string.pb_settle_up), PbIcon.Wallet, "ask.chip.settleUp")
                    is AskChip.OpenGroup ->
                        Triple(
                            stringResource(R.string.insights_ask_open, chip.name),
                            PbIcon.Groups,
                            "ask.chip.group",
                        )
                }
            PbButton(
                label,
                onClick = { onChip(chip) },
                modifier = Modifier.testTag(tag),
                style = PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
                leadingIcon = icon,
            )
        }
    }
}

/** Avatar / Stack shows at most four heads. */
private const val MAX_STACK = 4
