package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** What a [PbCategoryChip] shows before its label (`Leading`). */
sealed interface PbChipLeading {
    /** A 16 dp icon; the "Add" chip uses Plus. */
    data class Icon(val icon: PbIcon) : PbChipLeading

    /** A 24 dp avatar, for people pickers. */
    data class Avatar(val avatar: PbAvatarContent) : PbChipLeading
}

/**
 * `Control / Category Chip` (`PBCategoryChip`): a 36 dp filter or people chip. Selected is the
 * inverse fill; an avatar sits in a white circle on the grey chip and a grey one on the black chip.
 * The screen decides single or multiple selection.
 *
 * @param onClick Null for a chip that only labels (none in Figma).
 * @param onRemove Shows a trailing ✕ that removes the chip.
 */
@Composable
fun PbCategoryChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: PbChipLeading? = null,
    onRemove: (() -> Unit)? = null,
) {
    val press = rememberPressState(interactionSource = null)
    val fill =
        animatePressColor(
            when {
                selected && press.isPressed -> PbColors.Bg.InversePressed
                selected -> PbColors.Bg.Inverse
                press.isPressed -> PbColors.Bg.CardPressed
                else -> PbColors.Bg.Card
            },
            label = "PbCategoryChip fill",
        )
    val content = if (selected) PbColors.Text.Inverse else PbColors.Text.Primary
    val tap =
        if (onClick == null) Modifier
        else Modifier.pressable(press, enabled = true, onClick = onClick)
    Row(
        modifier =
            modifier
                .height(PbSize.ButtonSm)
                .clip(PbShapes.Pill)
                .background(fill)
                .then(tap)
                .semantics { this.selected = selected }
                .padding(
                    start =
                        when (leading) {
                            null -> PbSpace.S16
                            is PbChipLeading.Icon -> PbSpace.S12
                            is PbChipLeading.Avatar -> PbSpace.S6
                        },
                    end = PbSpace.S16,
                ),
        horizontalArrangement =
            Arrangement.spacedBy(if (leading is PbChipLeading.Avatar) PbSpace.S8 else PbSpace.S6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (leading) {
            is PbChipLeading.Icon ->
                PbIconImage(
                    leading.icon,
                    contentDescription = null,
                    size = PbSize.IconSm,
                    tint = content,
                )
            is PbChipLeading.Avatar ->
                PbAvatar(leading.avatar, size = PbAvatarSize.Xs, onCard = !selected)
            null -> Unit
        }
        Text(label, style = PbTextStyles.ButtonSmall, color = content, maxLines = 1)
        if (onRemove != null) {
            SmallIconButton(
                PbIcon.Close,
                contentDescription = stringResource(R.string.pb_remove, label),
                onClick = onRemove,
                size = PbSize.IconSm,
                tint = content,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbCategoryChipPreview() {
    Column(Modifier.padding(PbSpace.S16), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCategoryChip("Hair", onClick = {})
            PbCategoryChip("Hair", selected = true, onClick = {})
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCategoryChip("Add", onClick = {}, leading = PbChipLeading.Icon(PbIcon.Plus))
            PbCategoryChip(
                "Priya",
                selected = true,
                onClick = {},
                leading = PbChipLeading.Avatar(PbAvatarContent.Art(PbPeepHead.Priya)),
                onRemove = {},
            )
        }
    }
}
