package app.paybak.paybak.feature.profile

import android.content.Context
import android.content.Intent
import app.paybak.paybak.MainActivity
import app.paybak.paybak.data.UserProfile

/**
 * Sign out (screens-profile §2.3): the session goes (how the user signed in, the contact and the
 * finished onboarding), while the profile and the records stay on this device.
 */
fun UserProfile.signedOut(): UserProfile =
    copy(signInMethod = null, contact = "", onboardingComplete = false)

/**
 * Starts the app over in a fresh task, after Sign out or Delete account: with onboarding no longer
 * complete, the splash leads into onboarding and nothing in the app is reachable with back.
 */
fun Context.restartIntoOnboarding() {
    startActivity(
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}
