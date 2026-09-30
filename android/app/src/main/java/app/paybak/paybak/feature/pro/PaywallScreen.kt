package app.paybak.paybak.feature.pro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.data.ledger.actions.startTrial
import app.paybak.paybak.data.ledger.actions.subscribe
import app.paybak.paybak.domain.model.PlanPeriod
import app.paybak.paybak.feature.settings.TileRow
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAppMark
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbPlanCard
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbAppMarkSize
import app.paybak.paybak.ui.components.pressable
import app.paybak.paybak.ui.components.rememberPressState
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The paywall's Yearly → Welcome step (Figma DISSOLVE 300 ms). */
private const val WELCOME_FADE_MILLIS = 300

/**
 * The `paywall` route (screens-settings §2–3): the Pro paywall, then the Welcome once a (mock)
 * trial or subscription starts. A Pro member opening it sees the Welcome as their plan's status.
 * Welcome's Done (and system back there) closes the modal and continues to [Route.Paywall]'s
 * `continueTo`, the feature that asked for Pro.
 */
@Composable
fun PaywallScreen(route: Route.Paywall) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val haptics = rememberHaptics()
    var welcome by rememberSaveable { mutableStateOf(snapshot.isPro) }
    var nothingToRestore by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = welcome) { navigator.finishPaywall() }

    Crossfade(welcome, animationSpec = tween(WELCOME_FADE_MILLIS), label = "Paywall") {
        showWelcome ->
        if (showWelcome) {
            ProWelcome(
                status = snapshot.ledger.settings.entitlement.status(
                    snapshot.view.today,
                    snapshot.view.zone,
                ),
                onDone = navigator::finishPaywall,
            )
        } else {
            Paywall(
                onClose = navigator::dismissModal,
                onPurchase = { plan ->
                    when (plan) {
                        PlanPeriod.Yearly -> ledger.startTrial()
                        PlanPeriod.Monthly -> ledger.subscribe(PlanPeriod.Monthly)
                    }
                    haptics.perform(HapticKind.Success)
                    welcome = true
                },
                onRestore = {
                    if (ledger.snapshot.value.isPro) welcome = true else nothingToRestore = true
                },
            )
        }
    }
    if (nothingToRestore) {
        ProNotice(stringResource(R.string.settings_pro_nothing_to_restore)) {
            nothingToRestore = false
        }
    }
}

/** The paywall itself: the pitch, the five Pro features, the two plans and the CTA. */
@Composable
private fun Paywall(
    onClose: () -> Unit,
    onPurchase: (PlanPeriod) -> Unit,
    onRestore: () -> Unit,
) {
    var plan by rememberSaveable { mutableStateOf(PlanPeriod.Yearly) }
    PbScreen(
        id = "paywall",
        footer = {
            Purchase(plan, onSelect = { plan = it }, onPurchase = { onPurchase(plan) }, onRestore)
        },
    ) {
        PbModalHeader(title = null, onClose = onClose, testTag = "paywall")
        Spacer(Modifier.height(PbSpace.S4))
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PbAppMark(PbAppMarkSize.Splash)
            Column(
                verticalArrangement = Arrangement.spacedBy(PbSpace.S4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.settings_pro_title),
                    style = PbTextStyles.Title1,
                    color = PbColors.Text.Primary,
                )
                Text(
                    stringResource(R.string.settings_pro_subtitle),
                    style = PbTextStyles.Body,
                    color = PbColors.Text.Secondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(PbSpace.S20))
        ProFeature.entries.forEach {
            TileRow(it.icon, stringResource(it.title), stringResource(it.detail))
        }
    }
}

/** The plans, the CTA, its small print and the legal links. */
@Composable
private fun Purchase(
    plan: PlanPeriod,
    onSelect: (PlanPeriod) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
) {
    val yearly = plan == PlanPeriod.Yearly
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S20)) {
            PbPlanCard(
                period = stringResource(R.string.settings_plan_yearly),
                price = stringResource(R.string.settings_plan_yearly_price),
                detail = stringResource(R.string.settings_plan_yearly_detail),
                selected = yearly,
                onClick = { onSelect(PlanPeriod.Yearly) },
                modifier = Modifier.weight(1f).testTag("paywall.plan.yearly"),
                badge = stringResource(R.string.settings_plan_yearly_badge),
            )
            PbPlanCard(
                period = stringResource(R.string.settings_plan_monthly),
                price = stringResource(R.string.settings_plan_monthly_price),
                detail = stringResource(R.string.settings_plan_monthly_detail),
                selected = !yearly,
                onClick = { onSelect(PlanPeriod.Monthly) },
                modifier = Modifier.weight(1f).testTag("paywall.plan.monthly"),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PbButton(
                stringResource(
                    if (yearly) R.string.settings_pro_start_trial
                    else R.string.settings_pro_subscribe_monthly
                ),
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth().testTag("paywall.cta"),
            )
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(
                        if (yearly) R.string.settings_pro_yearly_small_print
                        else R.string.settings_pro_monthly_small_print
                    ),
                    modifier = Modifier.testTag("paywall.smallPrint"),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                    textAlign = TextAlign.Center,
                )
                LegalLinks(onRestore)
            }
        }
        Spacer(Modifier.height(PbSpace.S8))
    }
}

/**
 * Restore purchases, Terms and Privacy: tertiary Footnote links, 18 dp tall with 44 dp tap targets.
 * Terms and Privacy have no pages yet, so they do nothing (like Get Started's).
 */
@Composable
private fun LegalLinks(onRestore: () -> Unit) {
    Row(
        Modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S24),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
                Triple(R.string.settings_pro_restore, "paywall.restore", onRestore),
                Triple(R.string.settings_pro_terms, "paywall.terms", {}),
                Triple(R.string.settings_pro_privacy, "paywall.privacy", {}),
            )
            .forEach { (label, tag, onClick) -> LegalLink(stringResource(label), onClick, tag) }
    }
}

/** A Footnote link in `text/tertiary`; its 44 dp tap target overhangs the 18 dp row. */
@Composable
private fun LegalLink(label: String, onClick: () -> Unit, testTag: String) {
    val press = rememberPressState(interactionSource = null)
    Box(
        Modifier.wrapContentHeight(unbounded = true)
            .height(PbSize.Tap)
            .graphicsLayer { alpha = if (press.isPressed) PRESSED_ALPHA else 1f }
            .pressable(press, enabled = true, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary, maxLines = 1)
    }
}

/** Text buttons fade to half while pressed (README rule 11). */
private const val PRESSED_ALPHA = 0.5f

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun PaywallPreview() {
    PaybakTheme { Paywall(onClose = {}, onPurchase = {}, onRestore = {}) }
}
