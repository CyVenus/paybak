package app.paybak.paybak.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.data.ledger.actions.updateSettings
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCurrencyRow
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The `settingsCurrency` route (screens-settings §6): the default currency for totals, new groups
 * and new expenses (changed through the currency picker; stored records keep theirs), the Keep
 * balances per currency switch and how exchange rates work.
 */
@Composable
fun CurrencySettingsScreen(route: Route.SettingsCurrency) {
    val navigator = LocalMainNavigator.current
    val profileStore = LocalProfileStore.current
    val profile by profileStore.profile.collectAsState()
    val ledger = LocalLedger.current
    val settings = ledger.collectSnapshot().value.ledger.settings
    val currency = Currencies.currency(profile.defaultCurrency)
    val picker = remember { PickRequest() }
    val pickerTitle = stringResource(R.string.settings_currency_default)
    RouteResultEffect(picker.id) { result ->
        if (result is RouteResult.Currency) {
            profileStore.update { it.copy(currencyCode = result.code) }
        }
    }

    SettingsPage(route.info.id, stringResource(R.string.settings_currency_title)) {
        SettingsSection(
            stringResource(R.string.settings_currency_default),
            footer = stringResource(R.string.settings_currency_default_footer),
        ) {
            PbCard {
                PbCurrencyRow(
                    symbol = currency.tileSymbol,
                    title = currency.code,
                    subtitle = sentenceCase(currency.name),
                    selected = true,
                    onClick = {
                        navigator.open(
                            Route.PickCurrency(picker, selected = currency.code, title = pickerTitle)
                        )
                    },
                    modifier =
                        Modifier.padding(horizontal = PbSpace.S16)
                            .testTag("settingsCurrency.default"),
                    onCard = true,
                )
            }
        }
        SettingsSection(
            stringResource(R.string.settings_currency_balances),
            footer = stringResource(R.string.settings_currency_per_currency_footer),
        ) {
            PbCard {
                ToggleRow(
                    stringResource(R.string.settings_currency_per_currency),
                    checked = settings.keepBalancesPerCurrency,
                    onCheckedChange = { on ->
                        ledger.updateSettings { it.copy(keepBalancesPerCurrency = on) }
                    },
                    modifier = Modifier.testTag("settingsCurrency.perCurrency"),
                    showDivider = false,
                )
            }
        }
        SettingsSection(stringResource(R.string.settings_currency_rates)) {
            SettingsInfoLine(PbIcon.Exchange, stringResource(R.string.settings_currency_rates_info))
        }
    }
}

/**
 * The Currency screen's own casing (screens-settings §6): "Indian Rupee" → "Indian rupee", "US
 * Dollar" → "US dollar". Acronyms stay upper case.
 */
internal fun sentenceCase(name: String): String =
    name.split(' ').mapIndexed { index, word ->
        if (index == 0 || word.all { !it.isLetter() || it.isUpperCase() }) word else word.lowercase()
    }.joinToString(" ")
