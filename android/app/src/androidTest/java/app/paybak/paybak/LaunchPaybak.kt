package app.paybak.paybak

import android.content.Intent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import app.paybak.paybak.data.UserProfile

/**
 * Launches [MainActivity] like `adb shell am start` with the debug extras (flow.md,
 * app-architecture §3.10). Close the scenario when done, e.g. `launchPaybak().use { … }`.
 *
 * @param startScreen A screen id of app-architecture §1; null starts normally, at Splash.
 * @param resetOnboarding Clears the saved profile and ledger first, as after a fresh install.
 * @param demoData Loads the demo at Figma parity without changing the start screen.
 * @param scenarios Seed scenarios applied after the demo loads.
 * @param now Pins the clock (`yyyy-MM-ddTHH:mm`, local).
 * @param pro Overrides the plan last.
 * @param link Opens an internal link as a notification tap would.
 * @param autoApprove Whether friends confirm the payments you record after 5 s (debug builds). Off
 *   unless a test asks, so a payment you record stays pending.
 */
fun launchPaybak(
    startScreen: String? = null,
    resetOnboarding: Boolean = true,
    demoData: Boolean = false,
    scenarios: List<String> = emptyList(),
    now: String? = null,
    pro: Boolean? = null,
    link: String? = null,
    autoApprove: Boolean = false,
): ActivityScenario<MainActivity> {
    val intent =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra("resetOnboarding", resetOnboarding)
            .putExtra("demoData", demoData)
            .putExtra("autoApprove", autoApprove)
    startScreen?.let { intent.putExtra("startScreen", it) }
    if (scenarios.isNotEmpty()) intent.putExtra("scenario", scenarios.joinToString(","))
    now?.let { intent.putExtra("now", it) }
    pro?.let { intent.putExtra("pro", it) }
    link?.let { intent.putExtra("link", it) }
    return ActivityScenario.launch(intent)
}

/** Waits until the flow.md screen [id] (its `screen.<id>` root) is the only one of its kind. */
fun ComposeTestRule.awaitScreen(id: String, timeoutMillis: Long = 15_000) {
    waitUntil(timeoutMillis) {
        onAllNodes(hasTestTag("screen.$id")).fetchSemanticsNodes().size == 1
    }
}

/**
 * Waits until a node tagged [tag] exists: for content the app loads off the main thread, which the
 * test rule doesn't wait for (the full currency list on Setup 2).
 */
fun ComposeTestRule.awaitTag(tag: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) { onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }
}

/** The app's stores. */
val paybakApp: PaybakApplication
    get() = ApplicationProvider.getApplicationContext()

/** The profile as the app has saved it. */
val savedProfile: UserProfile
    get() =
        ApplicationProvider.getApplicationContext<PaybakApplication>().profileStore.profile.value

/**
 * System back as the app receives it. A real key press would only hide the keyboard while it's up,
 * and screens that focus a field on arrival raise it at an unpredictable moment.
 */
fun ActivityScenario<MainActivity>.pressSystemBack() {
    onActivity { it.onBackPressedDispatcher.onBackPressed() }
}
