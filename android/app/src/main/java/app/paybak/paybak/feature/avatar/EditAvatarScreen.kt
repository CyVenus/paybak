package app.paybak.paybak.feature.avatar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.domain.avatar.AvatarParts
import app.paybak.paybak.domain.avatar.AvatarSlot
import app.paybak.paybak.domain.avatar.pick
import app.paybak.paybak.domain.avatar.with
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.avatar.AvatarCrop
import app.paybak.paybak.ui.components.avatar.PbAvatarPartTile
import app.paybak.paybak.ui.components.avatar.PbAvatarStage
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/** The options grid: three columns, 13 dp apart both ways (screens-profile §3.1). */
private const val COLUMNS = 3
private val GridGap = 13.dp

/** A category change cross-fades the tiles (Figma SMART_ANIMATE 250 ms). */
private const val CATEGORY_FADE_MILLIS = 250

/**
 * The `editAvatar` route (screens-profile §3–4): build the custom character. Boy | Girl, a
 * category chip, then a tile; the stage previews the whole draft, Shuffle randomises, Save makes
 * the character the user's avatar everywhere. Back with unsaved changes asks first.
 */
@Composable
fun EditAvatarScreen(route: Route.EditAvatar) {
    val navigator = LocalMainNavigator.current
    val profileStore = LocalProfileStore.current
    val profile by profileStore.profile.collectAsState()
    val designed = rememberDebugStartScreen(*DesignedEditorStates.keys.toTypedArray())
    val state = rememberAvatarEditorState(profile.avatar, designed)
    var discarding by rememberSaveable { mutableStateOf(designed == "editAvatarDiscard") }
    val back: () -> Unit = {
        if (state.isDirty) discarding = true else navigator.back()
    }
    BackHandler(enabled = state.isDirty, onBack = back)

    EditAvatarContent(
        state = state,
        onBack = back,
        onSave = {
            profileStore.update { it.copy(avatar = AvatarChoice.Character(state.draft)) }
            navigator.back()
        },
        routeId = route.info.id,
    )
    if (discarding) {
        PbAlert(
            title = stringResource(R.string.profile_discard_title),
            message = stringResource(R.string.profile_discard_message),
            cancelLabel = stringResource(R.string.profile_discard_keep),
            actionLabel = stringResource(R.string.profile_discard),
            onCancel = { discarding = false },
            onAction = {
                discarding = false
                navigator.back()
            },
            testTag = "editAvatar.discard",
        )
    }
}

@Composable
private fun EditAvatarContent(
    state: AvatarEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    routeId: String,
) {
    val haptics = rememberHaptics()
    PbScreen(id = routeId) {
        PbPushHeader(
            title = stringResource(R.string.profile_editor_title),
            onBack = onBack,
            action = PbHeaderAction.Text(stringResource(R.string.pb_save), onClick = onSave),
            testTag = "editAvatar",
        )
        Spacer(Modifier.height(PbSpace.S16))
        PbAvatarStage(
            look = state.draft,
            onShuffle = {
                haptics.perform(HapticKind.Selection)
                state.shuffle()
            },
            shuffleLabel = stringResource(R.string.profile_editor_shuffle),
            modifier = Modifier.testTag("editAvatar.stage"),
            previewLabel = stringResource(R.string.profile_editor_preview),
            shuffleModifier = Modifier.testTag("editAvatar.shuffle"),
        )
        Spacer(Modifier.height(PbSpace.S16))
        PbSegmentedControl(
            options =
                listOf(
                    stringResource(R.string.profile_editor_boy),
                    stringResource(R.string.profile_editor_girl),
                ),
            selectedIndex = state.draft.gender.ordinal,
            onSelect = {
                haptics.perform(HapticKind.Selection)
                state.selectGender(AvatarGender.entries[it])
            },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("editAvatar.gender.boy", "editAvatar.gender.girl"),
        )
        Spacer(Modifier.height(PbSpace.S16))
        CategoryChips(state) {
            haptics.perform(HapticKind.Selection)
            state.selectSlot(it)
        }
        Spacer(Modifier.height(PbSpace.S16))
        AnimatedContent(
            targetState = state.draft.gender to state.slot,
            transitionSpec = {
                val millis =
                    if (initialState.first != targetState.first) PbMotion.SWAP_MILLIS
                    else CATEGORY_FADE_MILLIS
                fadeIn(tween(millis)) togetherWith fadeOut(tween(millis))
            },
            label = "Avatar options",
        ) { (gender, slot) ->
            OptionsGrid(state.draft.copy(gender = gender), slot) {
                haptics.perform(HapticKind.Selection)
                state.pick(it)
            }
        }
    }
}

/**
 * The scrolling chip row, clipped to the screen margins. The selected chip scrolls fully into view
 * (the Outfit states show the row scrolled to its end).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryChips(state: AvatarEditorState, onSelect: (AvatarSlot) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clipToBounds().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        state.slots.forEach { slot ->
            val selected = slot == state.slot
            val requester = remember { BringIntoViewRequester() }
            if (selected) LaunchedEffect(slot, state.draft.gender) { requester.bringIntoView() }
            PbCategoryChip(
                label = slot.label,
                modifier =
                    Modifier.bringIntoViewRequester(requester)
                        .testTag("editAvatar.category.${slot.id}"),
                selected = selected,
                onClick = { onSelect(slot) },
            )
        }
    }
}

/** The six options of [slot], each tile the [draft] with that option swapped in. */
@Composable
private fun OptionsGrid(draft: AvatarLook, slot: AvatarSlot, onPick: (String) -> Unit) {
    val crop = if (slot.showsBust) AvatarCrop.Bust else AvatarCrop.Head
    Column(verticalArrangement = Arrangement.spacedBy(GridGap)) {
        AvatarParts.options(draft.gender, slot).chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(GridGap)) {
                row.forEach { option ->
                    PbAvatarPartTile(
                        look = draft.with(slot, option.id),
                        crop = crop,
                        label = option.name,
                        selected = draft.pick(slot) == option.id,
                        onClick = { onPick(option.id) },
                        modifier = Modifier.weight(1f).testTag("editAvatar.option.${option.id}"),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun EditAvatarContentPreview() {
    PaybakTheme {
        val (draft, slot) = DesignedEditorStates.getValue("editAvatarBoyOutfit")
        EditAvatarContent(
            AvatarEditorState(draft, draft, slot),
            onBack = {},
            onSave = {},
            routeId = "editAvatar",
        )
    }
}
