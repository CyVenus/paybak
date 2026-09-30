package app.paybak.paybak.feature.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.data.Currency
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.search
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCurrencyRow
import app.paybak.paybak.ui.components.PbScrollEdgeFade
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

/** Space between the last row and the footer when the list is scrolled to its end. */
private val ListBottomPadding = 14.dp

/**
 * `setup2`: the home currency (screens-setup.md §2). The region's currency is suggested and
 * preselected (INR when the region has none); search covers every ISO currency in use. A choice
 * outside Suggested and Popular (saved earlier, or picked from search) stays listed under
 * Suggested, so the selection is always visible. Scrolling hides the keyboard; Continue stays put.
 */
@Composable
internal fun CurrencyStep(profileStore: ProfileStore, onContinue: () -> Unit) {
    val profile by profileStore.profile.collectAsState()
    val suggested = remember { Currencies.suggested() }
    val popular =
        remember(suggested) { Currencies.popular(excludingCode = suggested.currency.code) }
    val listedCodes = remember(popular) { popular.map(Currency::code) + suggested.currency.code }
    var selectedCode by rememberSaveable {
        mutableStateOf(profile.currencyCode ?: suggested.currency.code)
    }
    var pinnedCode by rememberSaveable {
        mutableStateOf(selectedCode.takeIf { it !in listedCodes })
    }
    val pinned = remember(pinnedCode) { pinnedCode?.let { Currencies.currency(it) } }
    var query by rememberSaveable { mutableStateOf("") }
    val all by
        produceState(emptyList<Currency>()) {
            value = withContext(Dispatchers.Default) { Currencies.all() }
        }
    val results = remember(all, query) { all.search(query) }

    val select = { currency: Currency ->
        selectedCode = currency.code
        if (currency.code !in listedCodes) pinnedCode = currency.code
    }
    val regionSubtitle = stringResource(R.string.setup2_region, suggested.currency.code)

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { it }
            .collect { focusManager.clearFocus() }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = PbLayout.ScreenMargin,
                        end = PbLayout.ScreenMargin,
                        bottom = ListBottomPadding,
                    ),
            ) {
                item(key = "intro") {
                    Column {
                        Spacer(Modifier.height(PbSpace.S24))
                        PbTitleBlock(
                            title = stringResource(R.string.setup2_headline),
                            body = stringResource(R.string.setup2_body),
                        )
                        Spacer(Modifier.height(PbSpace.S20))
                        PbTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = stringResource(R.string.setup2_search),
                            leadingIcon = PbIcon.Search,
                            onClear = { query = "" },
                            keyboardOptions =
                                KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    autoCorrectEnabled = false,
                                    imeAction = ImeAction.Search,
                                ),
                            keyboardActions =
                                KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            fieldModifier = Modifier.testTag("setup2.search"),
                        )
                        Spacer(Modifier.height(PbSpace.S20))
                    }
                }
                when {
                    query.isBlank() -> {
                        item(key = "suggested") {
                            PbSectionHeader(stringResource(R.string.setup2_suggested))
                        }
                        currencyRows(
                            "suggested",
                            listOfNotNull(pinned, suggested.currency),
                            selectedCode,
                            select,
                        ) {
                            if (it == suggested.currency && suggested.fromRegion) {
                                regionSubtitle
                            } else {
                                it.code
                            }
                        }
                        item(key = "popular") {
                            Column {
                                Spacer(Modifier.height(PbSpace.S16))
                                PbSectionHeader(stringResource(R.string.setup2_popular))
                            }
                        }
                        currencyRows("popular", popular, selectedCode, select)
                    }
                    results.isEmpty() ->
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.setup2_no_match, query.trim()),
                                style = PbTextStyles.Body,
                                color = PbColors.Text.Secondary,
                            )
                        }
                    else -> currencyRows("results", results, selectedCode, select)
                }
            }
            PbScrollEdgeFade(Modifier.align(Alignment.BottomCenter))
        }
        PbButton(
            label = stringResource(R.string.setup_continue),
            onClick = {
                profileStore.update { it.copy(currencyCode = selectedCode) }
                onContinue()
            },
            modifier =
                Modifier.padding(horizontal = PbLayout.ScreenMargin)
                    .fillMaxWidth()
                    .testTag("setup2.continue"),
        )
    }
}

/** A `Row / Currency` per currency; [section] keeps the item keys unique across the list. */
private fun LazyListScope.currencyRows(
    section: String,
    currencies: List<Currency>,
    selectedCode: String,
    onSelect: (Currency) -> Unit,
    subtitle: (Currency) -> String = Currency::code,
) {
    items(currencies, key = { "$section-${it.code}" }) { currency ->
        PbCurrencyRow(
            symbol = currency.tileSymbol,
            title = currency.name,
            subtitle = subtitle(currency),
            selected = currency.code == selectedCode,
            onClick = { onSelect(currency) },
            modifier = Modifier.testTag("setup2.row.${currency.code}"),
        )
    }
}
