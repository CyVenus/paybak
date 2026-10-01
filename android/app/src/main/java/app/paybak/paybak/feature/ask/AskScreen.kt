package app.paybak.paybak.feature.ask

import android.app.Activity
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addExpense
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.ask.AskAssistant
import app.paybak.paybak.domain.ask.AskChip
import app.paybak.paybak.domain.ask.AskPrompt
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ReminderTone
import app.paybak.paybak.domain.settle.remindDraft
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.service.speech.SpeechInput
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbComposer
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The chat's white fade under the header: solid over it, clear 44 dp below (Figma 90 → 150). */
private val FadeSolid = 28.dp
private val FadeClear = 88.dp

/**
 * Ask Paybak (askStart, askAnswer, askConfirm; insights §3): a full-screen chat opened from the
 * Home sparkle. The start state greets you with suggested prompts; each question gets an answer
 * from the live ledger (the on-device [AskAssistant]), with its cards and action chips. A drafted
 * expense waits for Save, which adds it in place; nothing leaves the device. The conversation lasts
 * for the session. Tagged `screen.ask`, parts `ask.*`.
 */
@Composable
fun AskScreen(route: Route.Ask) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val haptics = rememberHaptics()
    val assistant =
        remember(snapshot, profile.upiId) {
            AskAssistant(snapshot.view) {
                snapshot.view
                    .remindDraft(it, context = null, profile.upiId)
                    ?.message(ReminderTone.Friendly)
            }
        }
    val turns = AskChat.turns
    fun ask(question: String) {
        if (question.isBlank()) return
        turns += ChatTurn(question.trim(), assistant.answer(question))
    }

    val debugStart = rememberDebugStartScreen("askStart", "askAnswer", "askConfirm")
    LaunchedEffect(debugStart) {
        if (debugStart == null) return@LaunchedEffect
        turns.clear()
        if (debugStart != "askStart") ask(AskAssistant.WHO_OWES_ME)
        if (debugStart == "askConfirm") ask(DEMO_DRAFT)
    }
    // A draft opened in the Add expense form counts as saved once the form saves an expense
    // (one that no other card in the chat has claimed).
    LaunchedEffect(snapshot) {
        turns.forEachIndexed { index, turn ->
            val since = turn.editStartedAt ?: return@forEachIndexed
            val claimed = turns.mapNotNull { it.savedExpenseId }.toSet()
            val saved =
                snapshot.ledger.expenses
                    .filter { it.createdBy == ME && it.createdAt >= since && it.deletedAt == null }
                    .filter { it.id !in claimed }
                    .maxByOrNull { it.createdAt }
            if (saved != null)
                turns[index] = turn.copy(savedExpenseId = saved.id, editStartedAt = null)
        }
    }

    val actions =
        object : TurnActions {
            override fun save(index: Int) {
                val turn = turns.getOrNull(index) ?: return
                val draft = turn.answer.draft ?: return
                if (turn.savedExpenseId != null) return
                val id = ledger.addExpense(draft.draft)
                haptics.perform(HapticKind.Success)
                turns[index] = turn.copy(savedExpenseId = id)
            }

            override fun edit(index: Int) {
                val turn = turns.getOrNull(index) ?: return
                val draft = turn.answer.draft ?: return
                turns[index] = turn.copy(editStartedAt = ledger.clock.now())
                navigator.open(Route.AddExpense(AddExpenseArgs(draft = draft.draft)))
            }

            override fun view(expenseId: String) {
                navigator.dismissModal()
                navigator.open(Route.Expense(expenseId))
            }

            override fun person(personId: String) = navigator.open(Route.Friend(personId))

            override fun prompt(prompt: AskPrompt) = ask(prompt.text)

            override fun chip(chip: AskChip) {
                when (chip) {
                    is AskChip.Remind -> navigator.open(Route.Remind(chip.personId))
                    is AskChip.SeeInsights -> {
                        navigator.select(Tab.Activity)
                        navigator.activitySegment = ActivitySegment.Insights
                        navigator.insightsMonth = chip.month
                    }
                    is AskChip.SettleUp -> {
                        navigator.dismissModal()
                        navigator.open(Route.SettleUp(chip.groupId))
                    }
                    is AskChip.OpenGroup -> {
                        navigator.dismissModal()
                        navigator.open(Route.Group(chip.groupId))
                    }
                }
            }
        }

    var input by rememberSaveable { mutableStateOf("") }
    val focus = remember { MutableInteractionSource() }
    val focused by focus.collectIsFocusedAsState()
    val unavailable = stringResource(R.string.insights_ask_no_dictation)
    val dictate =
        rememberDictation(onText = { input = it }, onUnavailable = { navigator.toast(unavailable) })
    val scroll = rememberScrollState()
    LaunchedEffect(turns.size) {
        // Newest at the bottom: follow it once the new turn is laid out.
        snapshotFlow { scroll.maxValue }.collect { scroll.animateScrollTo(it) }
    }

    Box(Modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.ask")) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                if (turns.isEmpty()) {
                    AskStart(profile.firstName, assistant.prompts(), onPrompt = actions::prompt)
                } else {
                    Column(
                        Modifier.fillMaxSize()
                            .verticalScroll(scroll)
                            .padding(horizontal = PbLayout.ScreenMargin)
                            .padding(top = PbSize.Tap + PbSpace.S16, bottom = PbSpace.S16),
                        verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
                    ) {
                        turns.forEachIndexed { index, turn -> ChatTurnView(index, turn, actions) }
                    }
                    if (scroll.value > 0) HeaderFade()
                }
                PbModalHeader(
                    stringResource(R.string.insights_ask_title),
                    onClose = navigator::dismissModal,
                    modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
                    testTag = "ask",
                )
            }
            PbComposer(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    ask(input)
                    input = ""
                },
                placeholder = stringResource(R.string.insights_ask_placeholder),
                modifier =
                    if (focused) Modifier
                    else
                        Modifier.navigationBarsPadding()
                            .padding(horizontal = PbLayout.ScreenMargin),
                pinned = focused,
                onMic = dictate,
                interactionSource = focus,
                fieldModifier = Modifier.testTag("ask.composer"),
            )
        }
    }
}

/** The phrase the askConfirm start screen sends (Figma 11-06). */
private const val DEMO_DRAFT = "Add ₹600 for a cab, split with Esha and Dev"

/** The start state: the greeting, the suggested prompts and the privacy note above the composer. */
@Composable
private fun AskStart(firstName: String, prompts: List<AskPrompt>, onPrompt: (AskPrompt) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = PbLayout.ScreenMargin)) {
        Column(
            Modifier.fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = PbSize.Tap + PbSpace.S64),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S32),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
                PbAvatar(PbAvatarContent.Symbol(PbIcon.Sparkles), size = PbAvatarSize.Lg)
                Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                    Text(
                        if (firstName.isEmpty())
                            stringResource(R.string.insights_ask_greeting_anonymous)
                        else stringResource(R.string.insights_ask_greeting, firstName),
                        style = PbTextStyles.Title2,
                        color = PbColors.Text.Primary,
                    )
                    Text(
                        stringResource(R.string.insights_ask_intro),
                        style = PbTextStyles.Body,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
            if (prompts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                    Text(
                        stringResource(R.string.insights_ask_try),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Tertiary,
                    )
                    PromptCard(prompts, onPrompt)
                }
            }
        }
        Row(
            Modifier.align(Alignment.CenterHorizontally).padding(top = PbSpace.S16, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbIconImage(
                PbIcon.Lock,
                contentDescription = null,
                size = PbSize.IconSm,
                tint = PbColors.Icon.Tertiary,
            )
            Text(
                stringResource(R.string.insights_ask_privacy),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Tertiary,
            )
        }
    }
}

/** White over the header, fading out below it, so the chat scrolls away under the ✕ and title. */
@Composable
private fun HeaderFade() {
    Box(
        Modifier.fillMaxWidth()
            .height(FadeClear)
            .background(
                Brush.verticalGradient(
                    0f to PbColors.Bg.Primary,
                    FadeSolid.value / FadeClear.value to PbColors.Bg.Primary,
                    1f to PbColors.Bg.Primary.copy(alpha = 0f),
                )
            )
    )
}

/**
 * Dictation into the composer (insights §3.1): the system recogniser in its own UI; it fills the
 * field and never sends. [onUnavailable] runs where the device has no recogniser.
 */
@Composable
private fun rememberDictation(onText: (String) -> Unit, onUnavailable: () -> Unit): () -> Unit {
    val prompt = stringResource(R.string.insights_ask_placeholder)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
            ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    ?.let(onText)
            }
        }
    return {
        try {
            launcher.launch(SpeechInput.intent(prompt))
        } catch (_: ActivityNotFoundException) {
            onUnavailable()
        }
    }
}
