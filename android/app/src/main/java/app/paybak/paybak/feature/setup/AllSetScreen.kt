package app.paybak.paybak.feature.setup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `allSet`: setup is done (screens-setup.md §5). It greets the user by first name; tapping a person
 * or the badge makes them jump. There is no way back into setup from here.
 */
@Composable
fun AllSetScreen(firstName: String, onGoHome: () -> Unit) {
    PbScreen(
        id = "allSet",
        footer = {
            PbButton(
                label = stringResource(R.string.all_set_go_home),
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().testTag("allSet.goHome"),
            )
        },
    ) {
        // Where a header would sit on the other steps.
        Spacer(Modifier.height(PbSize.Tap))
        PaybakRiveIllustration(
            PaybakRiveAsset.AllSet,
            Modifier.align(Alignment.CenterHorizontally).weight(1f, fill = false),
        )
        Spacer(Modifier.height(PbSpace.S32))
        PbTitleBlock(
            title = stringResource(R.string.all_set_headline, firstName),
            body = stringResource(R.string.all_set_body),
        )
    }
}
