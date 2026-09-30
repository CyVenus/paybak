package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The 56 dp leading of a [PbAmountHero] (`Leading`). */
sealed interface PbHeroLeading {
    /** The category icon (Icon) or the other person on a loan (Avatar). */
    data class Single(val content: PbAvatarContent) : PbHeroLeading

    /** Payer → receiver (Pair). */
    data class FromTo(val from: PbAvatarContent, val to: PbAvatarContent) : PbHeroLeading
}

/**
 * `Header / Amount Hero` (`PBAmountHero`): the left-aligned hero of an expense, payment or loan
 * detail: the leading, the Title/2 title (wrapping), the amount in Title/1, the meta line and up to
 * three chips: grey [tags] (group, category) and a black [status] ("Disputed", "Paid back"). Status
 * chips are black or grey, never red.
 */
@Composable
fun PbAmountHero(
    title: String,
    amount: String,
    meta: String,
    leading: PbHeroLeading,
    modifier: Modifier = Modifier,
    tags: List<String> = emptyList(),
    status: String? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        when (leading) {
            is PbHeroLeading.Single -> PbAvatar(leading.content, size = PbAvatarSize.Lg)
            is PbHeroLeading.FromTo ->
                PbAvatarPair(leading.from, leading.to, size = PbAvatarPairSize.Large)
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = PbTextStyles.Title2,
                color = PbColors.Text.Primary,
            )
            Text(amount, style = PbTextStyles.Title1, color = PbColors.Text.Primary)
            Text(meta, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        }
        if (tags.isNotEmpty() || status != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                tags.forEach { PbBadge(it) }
                if (status != null) PbBadge(status, style = PbBadgeStyle.Inverse)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbAmountHeroPreview() {
    PbAmountHero(
        title = "Seafood dinner at Britto’s",
        amount = "₹6,500",
        meta = "Paid by you · 22 Sep",
        leading = PbHeroLeading.Single(PbAvatarContent.Symbol(PbIcon.Food)),
        tags = listOf("Goa Trip", "Food"),
        status = "Disputed",
    )
}
