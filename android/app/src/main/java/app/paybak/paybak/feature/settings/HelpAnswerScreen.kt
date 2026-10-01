package app.paybak.paybak.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.paybak.paybak.R
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `helpAnswer` route (screens-settings §11, proposal): under a "Help" header, the whole common
 * question as a Title 3 heading (a long one would truncate in the header) and its short answer in
 * Body text.
 */
@Composable
fun HelpAnswerScreen(route: Route.HelpAnswer) {
    val questions = stringArrayResource(R.array.settings_faq_questions)
    val answers = stringArrayResource(R.array.settings_faq_answers)
    val index = route.index.coerceIn(questions.indices)
    SettingsPage(route, stringResource(R.string.settings_help_answer_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            Text(
                questions[index],
                Modifier.testTag("helpAnswer.question").semantics { heading() },
                style = PbTextStyles.Title3,
                color = PbColors.Text.Primary,
            )
            Text(
                answers[index],
                Modifier.testTag("helpAnswer.body"),
                style = PbTextStyles.Body,
                color = PbColors.Text.Primary,
            )
        }
    }
}
