package app.paybak.paybak.feature.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.coerceAtMost
import app.paybak.paybak.R
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.feature.profile.rememberPhotoImage
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarOption
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbScreenBody
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.components.keyboardWithGap
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import kotlinx.coroutines.launch

/** Five heads and the camera share the row. */
private const val AVATAR_OPTIONS = 6

/**
 * `setup1`: name and avatar (screens-setup.md §1). The avatar is optional (initials are the
 * fallback); Continue needs a name. The camera option opens the system photo picker and keeps
 * showing the chosen photo, even after another option is selected.
 */
@Composable
internal fun NameStep(profileStore: ProfileStore, onContinue: () -> Unit) {
    val profile by profileStore.profile.collectAsState()
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var avatar by
        rememberSaveable(stateSaver = AvatarChoiceSaver) { mutableStateOf(profile.avatar) }
    var photoFile by rememberSaveable {
        mutableStateOf((profile.avatar as? AvatarChoice.Photo)?.fileName)
    }
    val photo = photoFile?.let(AvatarChoice::Photo)
    val photoImage = rememberPhotoImage(photo, profileStore)

    val scope = rememberCoroutineScope()
    val picker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    profileStore.importPhoto(uri)?.let { imported ->
                        photoFile = imported.fileName
                        avatar = imported
                    }
                }
            }
        }
    val onCamera = {
        if (photo != null && avatar != photo) {
            avatar = photo
        } else {
            picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    val canContinue = name.isNotBlank()
    val submit = {
        if (canContinue) {
            profileStore.update { it.copy(name = name.trim(), avatar = avatar) }
            onContinue()
        }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    PbScreenBody(
        modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
        footer = {
            PbButton(
                label = stringResource(R.string.setup_continue),
                onClick = submit,
                modifier =
                    Modifier.windowInsetsPadding(WindowInsets.keyboardWithGap)
                        .fillMaxWidth()
                        .testTag("setup1.continue"),
                enabled = canContinue,
            )
        },
    ) {
        Spacer(Modifier.height(PbSpace.S24))
        PbTitleBlock(
            title = stringResource(R.string.setup1_headline),
            body = stringResource(R.string.setup1_body),
        )
        Spacer(Modifier.height(PbSpace.S24))
        AvatarPicker(
            selected = avatar,
            photoImage = photoImage,
            onSelectPreset = { avatar = AvatarChoice.Preset(it) },
            onCamera = onCamera,
        )
        Spacer(Modifier.height(PbSpace.S20))
        PbTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.setup1_name_label),
            placeholder = stringResource(R.string.setup1_name_placeholder),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            fieldModifier =
                Modifier.focusRequester(focus).testTag("setup1.name").semantics {
                    contentType = ContentType.PersonFullName
                },
        )
    }
}

/**
 * The five heads and the camera, spread across the width. On screens too narrow for six 56 dp
 * options with 4 dp gaps, the options shrink.
 */
@Composable
private fun AvatarPicker(
    selected: AvatarChoice,
    photoImage: ImageBitmap?,
    onSelectPreset: (Int) -> Unit,
    onCamera: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val optionSize =
            ((maxWidth - PbSpace.S4 * (AVATAR_OPTIONS - 1)) / AVATAR_OPTIONS).coerceAtMost(
                PbSize.AvatarLg
            )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbPeepHead.Presets.forEachIndexed { index, head ->
                PbAvatarOption(
                    content = PbAvatarContent.Art(head),
                    selected = selected == AvatarChoice.Preset(index),
                    onClick = { onSelectPreset(index) },
                    contentDescription = stringResource(R.string.setup1_avatar, index + 1),
                    modifier = Modifier.testTag("setup1.avatar.$index"),
                    size = optionSize,
                )
            }
            PbAvatarOption(
                content =
                    photoImage?.let(PbAvatarContent::Photo)
                        ?: PbAvatarContent.Symbol(PbIcon.Camera),
                selected = selected is AvatarChoice.Photo,
                onClick = onCamera,
                contentDescription = stringResource(R.string.setup1_photo),
                modifier = Modifier.testTag("setup1.camera"),
                size = optionSize,
            )
        }
    }
}

/** Saves an [AvatarChoice] as "none", "preset:<index>" or "photo:<file name>". */
private val AvatarChoiceSaver =
    Saver<AvatarChoice, String>(
        save = { choice ->
            when (choice) {
                AvatarChoice.None -> "none"
                is AvatarChoice.Preset -> "preset:${choice.index}"
                is AvatarChoice.Photo -> "photo:${choice.fileName}"
            }
        },
        restore = { saved ->
            val (kind, value) = saved.split(':', limit = 2).let { it[0] to it.getOrNull(1) }
            when (kind) {
                "preset" -> value?.toIntOrNull()?.let(AvatarChoice::Preset)
                "photo" -> value?.let(AvatarChoice::Photo)
                else -> null
            } ?: AvatarChoice.None
        },
    )
