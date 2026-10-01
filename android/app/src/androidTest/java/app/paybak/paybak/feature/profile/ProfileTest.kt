package app.paybak.paybak.feature.profile

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.savedProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Profile and the avatar editor (screens-profile §7, app-architecture §6.4). */
@RunWith(AndroidJUnit4::class)
class ProfileTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    @Test
    fun editingTheAvatarKeepsEachGendersPicksAndSavesTheLook() {
        launchPaybak("profile").use {
            compose.awaitScreen("profile")
            tag("profile.editAvatar").performClick()
            compose.awaitScreen("editAvatar")
            tag("editAvatar.option.quiff").performClick()
            tag("editAvatar.option.quiff").assertIsSelected()
            tag("editAvatar.gender.girl").performClick()
            tag("editAvatar.option.ponytail").performClick()
            tag("editAvatar.gender.boy").performClick()
            tag("editAvatar.option.quiff").assertIsSelected()
            tag("editAvatar.option.curly").assertIsNotSelected()

            tag("editAvatar.back").performClick()
            compose.awaitTag("editAvatar.discard.cancel")
            tag("editAvatar.discard.cancel").performClick()
            awaitGone("editAvatar.discard")
            tag("editAvatar.action").performClick()
            compose.awaitScreen("profile")

            val look = (savedProfile.avatar as AvatarChoice.Character).look
            assertEquals("quiff", look.boy.hair)
            assertEquals("ponytail", look.girl.hair)

            // Reopened, the draft is the saved look, so Back leaves without asking.
            tag("profile.avatar").performClick()
            compose.awaitScreen("editAvatar")
            tag("editAvatar.option.quiff").assertIsSelected()
            tag("editAvatar.back").performClick()
            compose.awaitScreen("profile")
        }
    }

    @Test
    fun discardLeavesWithoutSaving() {
        launchPaybak("editAvatarDiscard").use {
            compose.awaitTag("editAvatar.discard.action")
            tag("editAvatar.discard.action").performClick()
            compose.awaitScreen("profile")
            assertEquals("curly", (savedProfile.avatar as AvatarChoice.Character).look.boy.hair)
        }
    }

    @Test
    fun theSettingsRowsOpenTheirScreens() {
        val rows =
            mapOf(
                "profile.row.payment" to "paymentDetails",
                "profile.row.currency" to "settingsCurrency",
                "profile.row.notifications" to "settingsNotifications",
                "profile.row.privacy" to "privacyData",
                "profile.row.help" to "helpFeedback",
            )
        launchPaybak("profile").use {
            compose.awaitScreen("profile")
            rows.forEach { (row, screen) ->
                tag(row).performClick()
                compose.awaitScreen(screen)
                tag("$screen.back").performClick()
                compose.awaitScreen("profile")
            }
            tag("profile.row.pro").performClick()
            compose.awaitScreen("paywall")
            tag("paywall.close").performClick()
            compose.awaitScreen("profile")
        }
    }

    @Test
    fun signingOutClearsTheSessionAndShowsGetStarted() {
        launchPaybak("profile").use {
            compose.awaitScreen("profile")
            tag("profile.signOut").performClick()
            compose.awaitTag("profile.signOutAlert.action")
            tag("profile.signOutAlert.action").performClick()
            compose.awaitScreen("getStarted", timeoutMillis = 20_000)
            assertFalse(savedProfile.onboardingComplete)
            assertEquals("Arjun Mehta", savedProfile.name)
        }
    }
}
