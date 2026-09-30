package app.paybak.paybak.debug

import android.content.Intent
import android.util.Log
import app.paybak.paybak.StartTarget
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.debug.gallery.GalleryScreen
import app.paybak.paybak.navigation.Destination

/**
 * Debug-only launch hooks (flow.md "Debug-only hooks"); this source set is not in release builds.
 *
 * - `--ez resetOnboarding true` clears the saved profile.
 * - `--es startScreen <id>` starts at a flow.md screen id. Starting past Get Started seeds the
 *   sample profile, so screens render like Figma.
 * - `--es startScreen gallery [--ei galleryPage N]` opens the component gallery at page N
 *   (1-based).
 */
object DebugLaunch {
    private const val TAG = "DebugLaunch"
    private const val EXTRA_START_SCREEN = "startScreen"
    private const val EXTRA_RESET_ONBOARDING = "resetOnboarding"
    private const val EXTRA_GALLERY_PAGE = "galleryPage"
    private const val GALLERY_ID = "gallery"

    /** Screens before sign-in don't read the profile, so starting there doesn't seed it. */
    private val preSignIn = setOf("splash", "welcome1", "welcome2", "welcome3", "getStarted")

    /**
     * Returns where to start, or null for the normal launch. Profile changes happen only on a
     * [firstLaunch], not when the Activity is recreated.
     */
    fun startTarget(
        intent: Intent,
        profileStore: ProfileStore,
        firstLaunch: Boolean,
    ): StartTarget? {
        if (firstLaunch && intent.getBooleanExtra(EXTRA_RESET_ONBOARDING, false)) {
            profileStore.reset()
        }
        val id = intent.getStringExtra(EXTRA_START_SCREEN) ?: return null
        if (id == GALLERY_ID) {
            val page = intent.getIntExtra(EXTRA_GALLERY_PAGE, 1)
            return StartTarget.Tool { GalleryScreen(initialPage = page) }
        }
        val destination = Destination.fromId(id)
        if (destination == null) {
            Log.w(TAG, "Unknown startScreen \"$id\"; starting normally")
            return null
        }
        if (firstLaunch && id !in preSignIn) {
            profileStore.update {
                SampleProfile.copy(onboardingComplete = destination is Destination.Home)
            }
        }
        return StartTarget.Flow(destination)
    }
}
