package app.paybak.paybak.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.paybak.paybak.BuildConfig
import app.paybak.paybak.R
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/** Where "Contact us" writes to (app-architecture §8: the address is still to be confirmed). */
object SupportContact {
    const val EMAIL = "support@paybak.app"
}

private const val PLAY_LISTING = "market://details?id=${BuildConfig.APPLICATION_ID}"
private const val PLAY_WEB =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

/**
 * The `helpFeedback` route (screens-settings §11): the five common questions (each opens its
 * answer), Contact us (a mail to support with the version) and Rate Paybak (the Play listing), and
 * the app's version from the build.
 */
@Composable
fun HelpScreen(route: Route.HelpFeedback) {
    val navigator = LocalMainNavigator.current
    val context = LocalContext.current
    val questions = stringArrayResource(R.array.settings_faq_questions)
    val version =
        stringResource(
            R.string.settings_help_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
        )
    val subject = stringResource(R.string.settings_help_mail_subject)
    val noMail = stringResource(R.string.settings_help_no_mail)

    SettingsPage(route, stringResource(R.string.settings_help_title)) {
        SettingsSection(stringResource(R.string.settings_help_questions)) {
            PbCard {
                questions.forEachIndexed { index, question ->
                    PbSettingRow(
                        question,
                        modifier = Modifier.testTag("helpFeedback.faq.$index"),
                        onClick = { navigator.openFrom(route, Route.HelpAnswer(index)) },
                        icon = PbIcon.Help,
                        showDivider = index != questions.lastIndex,
                        titleMaxLines = 2,
                    )
                }
            }
        }
        SettingsSection(stringResource(R.string.settings_help_touch)) {
            PbCard {
                PbSettingRow(
                    stringResource(R.string.settings_help_contact),
                    modifier = Modifier.testTag("helpFeedback.contact"),
                    onClick = {
                        if (!context.mailSupport(subject, version)) navigator.toast(noMail)
                    },
                    icon = PbIcon.Mail,
                )
                PbSettingRow(
                    stringResource(R.string.settings_help_rate),
                    modifier = Modifier.testTag("helpFeedback.rate"),
                    onClick = context::openPlayListing,
                    icon = PbIcon.Star,
                    showDivider = false,
                )
            }
        }
        Text(
            version,
            Modifier.fillMaxWidth().testTag("helpFeedback.version"),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Tertiary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Opens a mail to support with the version in the body; false with no mail app. */
private fun Context.mailSupport(subject: String, version: String): Boolean {
    val mail =
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportContact.EMAIL))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, "\n\n$version")
    return try {
        startActivity(mail)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

/** The Play Store listing, or its web page without the Play Store. */
private fun Context.openPlayListing() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_LISTING)))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_WEB)))
    }
}
