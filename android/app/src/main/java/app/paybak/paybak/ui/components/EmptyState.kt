package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** One button of [PbEmptyState]. */
data class PbEmptyAction(
    val label: String,
    val icon: PbIcon?,
    val onClick: () -> Unit,
    val testTag: String? = null,
)

/**
 * `Card / Empty State` (`PBEmptyState`) on a #F5F5F5 card: a 240 × 180 Rive illustration, a Title/2
 * title, a Body text and up to two actions (primary black, secondary white). The reused art
 * (app-architecture §5.3): First day for empty lists, AllSquare for "You’re all square."
 */
@Composable
fun PbEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    illustration: PaybakRiveAsset = PaybakRiveAsset.HomeFirstDay,
    primaryAction: PbEmptyAction? = null,
    secondaryAction: PbEmptyAction? = null,
    testTag: String? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .partTag(testTag)
            .clip(PbShapes.Card)
            .background(PbColors.Bg.Card)
            .padding(PbSpace.S24),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(240.dp, 180.dp), contentAlignment = Alignment.Center) {
            PaybakRiveIllustration(illustration)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = PbTextStyles.Title2,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
            Text(
                body,
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
        }
        if (primaryAction != null || secondaryAction != null) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            ) {
                primaryAction?.let {
                    PbButton(
                        it.label,
                        it.onClick,
                        Modifier.fillMaxWidth().partTag(it.testTag),
                        leadingIcon = it.icon,
                    )
                }
                secondaryAction?.let {
                    PbButton(
                        it.label,
                        it.onClick,
                        Modifier.fillMaxWidth().partTag(it.testTag),
                        style = PbButtonStyle.OnCard,
                        leadingIcon = it.icon,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbEmptyStatePreview() {
    PbEmptyState(
        "Nothing here yet.",
        "Add your first expense or invite a friend to get started.",
        Modifier.padding(PbSpace.S20),
        primaryAction = PbEmptyAction("Add expense", PbIcon.Plus, {}),
        secondaryAction = PbEmptyAction("Invite friends", PbIcon.UserAdd, {}),
    )
}
