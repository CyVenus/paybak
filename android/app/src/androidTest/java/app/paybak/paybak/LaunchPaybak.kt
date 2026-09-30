package app.paybak.paybak

import android.content.Intent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import app.paybak.paybak.data.UserProfile

/**
 * Launches [MainActivity] like `adb shell am start` with the flow.md debug extras. Close the
 * scenario when done, e.g. `launchPaybak().use { … }`.
 *
 * @param startScreen A flow.md screen id; null starts normally, at Splash.
 * @param resetOnboarding Clears the saved profile first, as after a fresh install.
 */
fun launchPaybak(
    startScreen: String? = null,
    resetOnboarding: Boolean = true,
): ActivityScenario<MainActivity> {
    val intent =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra("resetOnboarding", resetOnboarding)
    startScreen?.let { intent.putExtra("startScreen", it) }
    return ActivityScenario.launch(intent)
}

/** Waits until the flow.md screen [id] (its `screen.<id>` root) is the only one of its kind. */
fun ComposeTestRule.awaitScreen(id: String, timeoutMillis: Long = 10_000) {
    waitUntil(timeoutMillis) {
        onAllNodes(hasTestTag("screen.$id")).fetchSemanticsNodes().size == 1
    }
}

/** The profile as the app has saved it. */
val savedProfile: UserProfile
    get() =
        ApplicationProvider.getApplicationContext<PaybakApplication>().profileStore.profile.value
