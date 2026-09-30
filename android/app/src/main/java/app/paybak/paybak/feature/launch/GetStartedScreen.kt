package app.paybak.paybak.feature.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import app.paybak.paybak.R
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** "Terms" and "Privacy Policy": the same Footnote, darker and underlined (not bold). */
private val LegalLinkStyles =
    TextLinkStyles(
        SpanStyle(color = PbColors.Text.Secondary, textDecoration = TextDecoration.Underline)
    )

/**
 * `getStarted`: the sign-in choice (screens-launch.md §3). The Get Started Rive draws its own grey
 * card over the Figma slot, and tapping a person makes them jump with a light haptic. There is no
 * real Apple or Google sign-in yet, so those buttons continue straight away.
 */
@Composable
fun GetStartedScreen(
    onContinueWithApple: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmailOrPhone: () -> Unit,
) {
    PbScreen(
        id = "getStarted",
        content = {
            Spacer(Modifier.height(PbSpace.S24))
            PbLogo(PbLogoLayout.Horizontal, Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(PbSpace.S32))
            PaybakRiveIllustration(
                asset = PaybakRiveAsset.GetStarted,
                modifier = Modifier.align(Alignment.CenterHorizontally).weight(1f, fill = false),
            )
            Spacer(Modifier.height(PbSpace.S32))
            Text(
                text = stringResource(R.string.get_started_headline),
                modifier =
                    Modifier.fillMaxWidth().testTag("getStarted.headline").semantics {
                        heading()
                    },
                style = PbTextStyles.Title1,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PbSpace.S12))
            Text(
                text = stringResource(R.string.get_started_body),
                modifier = Modifier.fillMaxWidth(),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
        },
        footer = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbButton(
                    label = stringResource(R.string.get_started_apple),
                    onClick = onContinueWithApple,
                    modifier = Modifier.fillMaxWidth().testTag("getStarted.apple"),
                    leadingIcon = PbIcon.Apple,
                )
                PbButton(
                    label = stringResource(R.string.get_started_google),
                    onClick = onContinueWithGoogle,
                    modifier = Modifier.fillMaxWidth().testTag("getStarted.google"),
                    style = PbButtonStyle.Secondary,
                    leadingIcon = PbIcon.Google,
                )
                PbTextButton(
                    label = stringResource(R.string.get_started_email_or_phone),
                    onClick = onContinueWithEmailOrPhone,
                    modifier = Modifier.testTag("getStarted.email"),
                )
                LegalFootnote()
            }
        },
    )
}

/**
 * "By continuing, you agree to our Terms and Privacy Policy." The two links are marked up so URLs
 * can be added later; until then they do nothing.
 */
@Composable
private fun LegalFootnote() {
    val links =
        listOf(
            stringResource(R.string.get_started_terms),
            stringResource(R.string.get_started_privacy),
        )
    val sentence = stringResource(R.string.get_started_legal, *links.toTypedArray())
    val text =
        remember(sentence, links) {
            buildAnnotatedString {
                append(sentence)
                links.forEach { link ->
                    val start = sentence.indexOf(link)
                    addLink(
                        LinkAnnotation.Clickable(
                            tag = link,
                            styles = LegalLinkStyles,
                            linkInteractionListener = null,
                        ),
                        start = start,
                        end = start + link.length,
                    )
                }
            }
        }
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = PbTextStyles.Footnote,
        color = PbColors.Text.Tertiary,
        textAlign = TextAlign.Center,
    )
}
