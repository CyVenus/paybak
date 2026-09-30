package app.paybak.paybak.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import app.paybak.paybak.R
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `helpAnswer` route (screens-settings §11, proposal): a common question as the title and its
 * short answer in Body text.
 */
@Composable
fun HelpAnswerScreen(route: Route.HelpAnswer) {
    val questions = stringArrayResource(R.array.settings_faq_questions)
    val answers = stringArrayResource(R.array.settings_faq_answers)
    val index = route.index.coerceIn(questions.indices)
    SettingsPage(route.info.id, questions[index]) {
        Text(
            answers[index],
            Modifier.testTag("helpAnswer.body"),
            style = PbTextStyles.Body,
            color = PbColors.Text.Primary,
        )
    }
}
