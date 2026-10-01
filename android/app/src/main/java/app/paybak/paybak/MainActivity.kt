package app.paybak.paybak

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import app.paybak.paybak.debug.DebugLaunch
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.rive.RiveHost
import app.paybak.paybak.ui.theme.PaybakTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val app: PaybakApplication
        get() = application as PaybakApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light theme only: dark system-bar icons over the white screens, whatever the system
        // theme.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)

        val start =
            DebugLaunch.startTarget(intent, app, firstLaunch = savedInstanceState == null)
                ?: StartTarget.Flow(onboardingStart(intent) ?: Destination.Splash)
        if (savedInstanceState == null) postLink(intent)
        setContent {
            RiveHost {
                PaybakTheme {
                    PaybakApp(app, start)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        postLink(intent)
    }

    override fun onStart() {
        super.onStart()
        // Launch and every return to the foreground run the scheduler (domain.md §10), once the
        // ledger is read (off the main thread).
        lifecycleScope.launch { withContext(Dispatchers.Default) { app.ledger }.tick() }
    }

    /** Where Sign out and Delete account restart the app, while onboarding isn't complete. */
    private fun onboardingStart(intent: Intent): Destination? =
        intent
            .getStringExtra(EXTRA_ONBOARDING_AT)
            ?.takeUnless { app.profileStore.profile.value.onboardingComplete }
            ?.let(Destination::fromId)

    /** A notification's internal link (app-architecture §2.6); ignored during onboarding. */
    private fun postLink(intent: Intent) {
        val link = intent.getStringExtra(EXTRA_LINK) ?: return
        if (app.profileStore.profile.value.onboardingComplete) app.links.post(link)
    }

    companion object {
        /** The intent extra every notification (and the debug `link` key) carries. */
        const val EXTRA_LINK = "link"

        /** The onboarding screen id a restart opens at (see `restartIntoOnboarding`). */
        const val EXTRA_ONBOARDING_AT = "onboardingAt"
    }
}
