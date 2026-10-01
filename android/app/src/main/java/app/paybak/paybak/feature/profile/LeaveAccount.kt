package app.paybak.paybak.feature.profile

import android.content.Context
import android.content.Intent
import app.paybak.paybak.MainActivity
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.navigation.Destination

/**
 * Sign out (screens-profile §2.3): the session goes (how the user signed in, the contact and the
 * finished onboarding), while the profile and the records stay on this device.
 */
fun UserProfile.signedOut(): UserProfile =
    copy(signInMethod = null, contact = "", onboardingComplete = false)

/**
 * Starts the app over in a fresh task at [destination] (Get Started after Sign out, Welcome after
 * Delete account): with onboarding no longer complete, nothing in the app is reachable with back.
 */
fun Context.restartIntoOnboarding(destination: Destination) {
    startActivity(
        Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ONBOARDING_AT, destination.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}
