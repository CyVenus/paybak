package app.paybak.paybak.feature.insights

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.insights.InsightsPage
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbNoticeLayout
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbPalette
import app.paybak.paybak.ui.theme.PbSpace

/** Where the Pro notice sits below the hero card's top (Figma: 358 − 226). */
private val NoticeOffset = 132.dp

/**
 * The free plan's Insights (insights §2.5): the real report stays underneath but unreadable (its
 * text and bars blurred at 40 %, the hero card's fill sharp) under a white 60 % veil that blocks
 * taps, with the centred Pro notice on top. "See Pro" opens the paywall; once Pro, the report shows
 * in place.
 */
@Composable
internal fun LockedReport(page: InsightsPage, onSeePro: () -> Unit) {
    Box(Modifier.fillMaxWidth().testTag("insights.locked")) {
        Column {
            HeroCard(page, content = Modifier.unreadable())
            Spacer(Modifier.height(PbSpace.S16))
            CategorySection(page, rows = Modifier.unreadable())
        }
        Box(
            Modifier.matchParentSize().background(PbPalette.White60).pointerInput(Unit) {
                detectTapGestures {}
            }
        )
        PbNoticeCard(
            body = stringResource(R.string.insights_locked_body),
            icon = PbIcon.Lock,
            modifier = Modifier.padding(top = NoticeOffset),
            title = stringResource(R.string.insights_locked_title),
            badge = stringResource(R.string.insights_pro),
            layout = PbNoticeLayout.Centered,
            primaryAction = PbNoticeAction(stringResource(R.string.insights_see_pro), onSeePro),
            testTag = "insights.notice",
        )
    }
}

/**
 * Figma's opacity 0.4 + layer blur 16. Android 11 and older have no blur, so the parts fade further
 * instead. Hidden from TalkBack.
 */
private fun Modifier.unreadable(): Modifier =
    clearAndSetSemantics {}
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.alpha(BLURRED_ALPHA).blur(16.dp, BlurredEdgeTreatment.Unbounded)
        } else {
            Modifier.alpha(FADED_ALPHA)
        }
    )

private const val BLURRED_ALPHA = 0.4f
private const val FADED_ALPHA = 0.15f
