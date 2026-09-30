package app.paybak.paybak.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * Which way a balance goes. Owed reads "+₹700" in black, Owe "−₹700" (U+2212) in grey. Amount texts
 * never carry the sign: the components draw it.
 */
enum class PbBalance(internal val sign: String, internal val color: Color) {
    Owed("+", PbColors.Text.Primary),
    Owe("−", PbColors.Text.Secondary),
}

/** [amount] with [balance]'s sign and colour, in Amount/Medium. */
@Composable
internal fun SignedAmount(amount: String, balance: PbBalance, modifier: Modifier = Modifier) {
    Text(
        text = balance.sign + amount,
        modifier = modifier,
        style = PbTextStyles.AmountMedium,
        color = balance.color,
        maxLines = 1,
    )
}
