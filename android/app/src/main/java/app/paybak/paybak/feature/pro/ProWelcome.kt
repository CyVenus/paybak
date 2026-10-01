package app.paybak.paybak.feature.pro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.feature.settings.SettingsSection
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate

/** The All set art at 2/3 scale fills this slot (screens-settings §3). */
private val IllustrationHeight = 200.dp

/**
 * Paybak Pro — Welcome (screens-settings §3): the trial has started (or the member's plan
 * status), what's now unlocked, and Done. [onManage], when the store's entitlement is active, adds
 * Manage subscription (RevenueCat's Customer Center). The whole screen sits inside the paywall
 * route, so its content is tagged `screen.proWelcome`.
 */
@Composable
internal fun ProWelcome(status: ProStatus?, onDone: () -> Unit, onManage: (() -> Unit)? = null) {
    PbScreen(
        id = "paywall",
        footer = {
            Column(
                Modifier.fillMaxWidth().padding(bottom = PbSpace.S16),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            ) {
                PbButton(
                    stringResource(R.string.settings_done),
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().testTag("proWelcome.done"),
                )
                onManage?.let {
                    PbButton(
                        stringResource(R.string.settings_pro_manage),
                        onClick = it,
                        modifier = Modifier.fillMaxWidth().testTag("proWelcome.manage"),
                        style = PbButtonStyle.Secondary,
                    )
                }
            }
        },
    ) {
        Column(
            Modifier.fillMaxWidth().testTag("screen.proWelcome").padding(top = PbSpace.S24),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
        ) {
            PaybakRiveIllustration(
                PaybakRiveAsset.AllSet,
                Modifier.align(Alignment.CenterHorizontally).height(IllustrationHeight),
            )
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                Text(
                    stringResource(R.string.settings_pro_welcome_title),
                    modifier = Modifier.testTag("proWelcome.title"),
                    style = PbTextStyles.Title1,
                    color = PbColors.Text.Primary,
                )
                status?.let {
                    Text(
                        statusLine(it),
                        modifier = Modifier.testTag("proWelcome.body"),
                        style = PbTextStyles.Body,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
            SettingsSection(stringResource(R.string.settings_pro_unlocked)) {
                PbCard {
                    ProFeature.entries.forEach {
                        PbSettingRow(
                            stringResource(it.title),
                            trailing = PbSettingTrailing.Check(selected = true),
                            icon = it.icon,
                            showDivider = it != ProFeature.entries.last(),
                        )
                    }
                }
            }
        }
    }
}

/** "Your free trial ends Wed 7 Oct.", the renewal lines and "Pro until …". */
@Composable
private fun statusLine(status: ProStatus): String {
    val day = Dates.day(status.day)
    return when (status) {
        is ProStatus.TrialEnds -> stringResource(R.string.settings_pro_trial_ends, day)
        is ProStatus.Renews ->
            when (status.period) {
                PlanPeriod.Yearly -> stringResource(R.string.settings_pro_renews_yearly, day)
                PlanPeriod.Monthly -> stringResource(R.string.settings_pro_renews_monthly, day)
                null -> stringResource(R.string.settings_pro_renews, day)
            }
        is ProStatus.Ends -> stringResource(R.string.settings_pro_ends, day)
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ProWelcomePreview() {
    PaybakTheme { ProWelcome(ProStatus.TrialEnds(LocalDate.of(2026, 10, 7)), onDone = {}) }
}
