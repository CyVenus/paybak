package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.data.Currency
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.data.search
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbCurrencyRow
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetDetent
import app.paybak.paybak.ui.components.PbSheetSearch
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The major currencies "All currencies" lists first (add-expense §9.2), before every other. */
private val Majors = listOf("AUD", "GBP", "CAD", "EUR", "JPY", "SGD", "USD")

/**
 * The `pickCurrency` route sheet (`addExpenseCurrency`; add-expense §9): Recent (the default
 * currency, then those of your recent records and groups) and All currencies, searchable across
 * every ISO currency. A pick answers the request and closes the sheet.
 */
@Composable
fun CurrencyPickerSheet(route: Route.PickCurrency) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var picked by remember { mutableStateOf<String?>(null) }
    val all by
        produceState(emptyList<Currency>()) {
            value = withContext(Dispatchers.Default) { Currencies.all() }
        }
    val recent =
        (listOf(profile.defaultCurrency) +
                snapshot.recentCurrencies +
                listOfNotNull(route.selected))
            .distinct()
    val others =
        Majors.filter { it !in recent }.map { Currencies.currency(it) }.sortedBy { sheetName(it) } +
            all.filter { it.code !in recent && it.code !in Majors }
    PbSheet(
        onDismiss = {
            val code = picked
            if (code != null) navigator.complete(route.request.id, RouteResult.Currency(code))
            else navigator.dismissSheet()
        },
        title = route.title ?: "Currency",
        detent = PbSheetDetent.Large,
        search = PbSheetSearch(query, { query = it }, "Search currencies"),
        testTag = "currency.sheet",
    ) { dismiss ->
        val pick: (String) -> Unit = {
            picked = it
            dismiss()
        }
        val results = if (query.isBlank()) null else all.search(query)
        LazyColumn(Modifier.fillMaxWidth().testTag("screen.pickCurrency")) {
            if (results != null) {
                if (results.isEmpty()) {
                    item {
                        Text(
                            "No currencies match “$query”",
                            Modifier.fillMaxWidth().padding(top = PbSpace.S16),
                            style = PbTextStyles.Footnote,
                            color = PbColors.Text.Secondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(results, key = { it.code }) { CurrencyItem(it, route.selected, pick) }
            } else {
                item { PbSectionHeader("Recent") }
                items(recent, key = { "recent-$it" }) {
                    CurrencyItem(Currencies.currency(it), route.selected, pick)
                }
                item {
                    Column(Modifier.padding(top = PbSpace.S24 - PbSpace.S4)) {
                        PbSectionHeader("All currencies")
                    }
                }
                items(others, key = { it.code }) { CurrencyItem(it, route.selected, pick) }
            }
        }
    }
}

@Composable
private fun CurrencyItem(currency: Currency, selected: String?, onPick: (String) -> Unit) {
    val known = Money.currency(currency.code)
    Column(Modifier.padding(top = PbSpace.S4), verticalArrangement = Arrangement.Top) {
        PbCurrencyRow(
            symbol = if (known.name != currency.code) known.symbol else currency.tileSymbol,
            title = sheetName(currency),
            subtitle = currency.code,
            selected = currency.code == selected,
            onClick = { onPick(currency.code) },
            modifier = Modifier.testTag("currency.row.${currency.code}"),
        )
    }
}

/**
 * The sentence-case name this sheet uses ("Indian rupee", "UAE dirham"): the designed ones as Figma
 * writes them, others with their unit word in lower case ("Swiss franc").
 */
private fun sheetName(currency: Currency): String {
    val known = Money.currency(currency.code).name
    if (known != currency.code) return known
    val words = currency.name.split(' ')
    return if (words.size < 2) currency.name
    else (words.dropLast(1) + words.last().lowercase()).joinToString(" ")
}
