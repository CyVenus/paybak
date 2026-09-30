package app.paybak.paybak.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * `Sheet / Action Row` (`PBSheetRow`): a 72 dp row of the Add sheet: a 44 dp icon tile, title and
 * subtitle, and a chevron. Pressed fills the row #F5F5F5 and turns the tile white.
 */
@Composable
fun PbSheetRow(
    title: String,
    subtitle: String,
    icon: PbIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
) {
    val press = rememberPressState(null)
    val rowFill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Card else PbColors.Bg.Card.copy(alpha = 0f),
            "Sheet row",
        )
    val tileFill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Primary else PbColors.Bg.Card,
            "Sheet row tile",
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(rowFill, PbShapes.Card)
                .pressable(press, enabled = true, onClick = onClick)
                .padding(horizontal = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(PbSize.Tap).background(tileFill, PbShapes.Tile),
            contentAlignment = Alignment.Center,
        ) {
            PbIconImage(icon, contentDescription = null)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                title,
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showChevron) {
            PbIconImage(
                PbIcon.ChevronRight,
                contentDescription = null,
                size = PbSize.IconMd,
                tint = PbColors.Icon.Tertiary,
            )
        }
    }
}

/** One action of the ＋ Add sheet. */
enum class PbAddAction(
    @param:StringRes val title: Int,
    @param:StringRes val subtitle: Int,
    val icon: PbIcon,
    val tag: String,
) {
    Expense(
        R.string.shell_add_expense,
        R.string.shell_add_expense_subtitle,
        PbIcon.Receipt,
        "expense",
    ),
    Payment(
        R.string.shell_add_payment,
        R.string.shell_add_payment_subtitle,
        PbIcon.Exchange,
        "payment",
    ),
    Lend(R.string.shell_add_lend, R.string.shell_add_lend_subtitle, PbIcon.Lend, "lend"),
    Group(R.string.shell_add_group, R.string.shell_add_group_subtitle, PbIcon.Groups, "group"),
}

/**
 * The rows of `Sheet / Action Sheet` (`PBAddSheet`); present them in a [PbSheet] titled "Add". Rows
 * are tagged "[testTag].<expense|payment|lend|group>".
 */
@Composable
fun PbAddSheetRows(
    onAction: (PbAddAction) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Column(modifier.fillMaxWidth()) {
        PbAddAction.entries.forEach { action ->
            PbSheetRow(
                title = stringResource(action.title),
                subtitle = stringResource(action.subtitle),
                icon = action.icon,
                onClick = { onAction(action) },
                modifier =
                    if (testTag != null) Modifier.testTag("$testTag.${action.tag}") else Modifier,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 386)
@Composable
private fun PbAddSheetPreview() {
    PbSheetContainer(
        Modifier.padding(PbSpace.S8),
        title = stringResource(R.string.shell_add_title),
        onClose = {},
    ) {
        PbAddSheetRows(onAction = {})
    }
}
