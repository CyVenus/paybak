package app.paybak.paybak.debug

import android.content.Intent
import android.util.Log
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.StartTarget
import app.paybak.paybak.data.ledger.actions.setPro
import app.paybak.paybak.debug.gallery.GalleryScreen
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.navigation.Destination
import java.time.LocalDateTime

/**
 * Debug-only launch hooks (flow.md "Debug-only hooks", app-architecture §3.10); this source set is
 * not in release builds.
 *
 * - `--ez resetOnboarding true` clears the profile and the ledger.
 * - `--es startScreen <id>` starts at any screen id of app-architecture §1 ([ScreenIds]). An app
 *   screen loads its demo scenario at Figma parity and opens its tab, stack and sheet; the owning
 *   screen reads the id for its in-screen state. Onboarding ids past Get Started seed the sample
 *   profile. `gallery [--ei galleryPage N]` opens the component gallery.
 * - `--ez demoData true` loads the demo (`D`) at Figma parity without changing the start screen.
 * - `--es scenario a,b` applies seed scenarios after that; `--es now 2026-09-30T21:15` pins the
 *   clock; `--es pro YES|NO` (or `--ez pro`) overrides the plan last.
 * - `--es link paybak://…` opens an internal link, as a notification tap does (MainActivity).
 */
object DebugLaunch {
    private const val TAG = "DebugLaunch"
    private const val EXTRA_START_SCREEN = "startScreen"
    private const val EXTRA_RESET_ONBOARDING = "resetOnboarding"
    private const val EXTRA_GALLERY_PAGE = "galleryPage"
    private const val EXTRA_DEMO_DATA = "demoData"
    private const val EXTRA_SCENARIO = "scenario"
    private const val EXTRA_NOW = "now"
    private const val EXTRA_PRO = "pro"
    const val GALLERY_ID = "gallery"

    /** Screens before sign-in don't read the profile, so starting there doesn't seed it. */
    private val preSignIn = setOf("splash", "welcome1", "welcome2", "welcome3", "getStarted")

    /**
     * Returns where to start, or null for the normal launch. Data changes happen only on a
     * [firstLaunch], not when the Activity is recreated.
     */
    fun startTarget(intent: Intent, app: PaybakApplication, firstLaunch: Boolean): StartTarget? {
        if (firstLaunch && intent.getBooleanExtra(EXTRA_RESET_ONBOARDING, false)) {
            app.resetAccount()
            DebugClock.pin(app, null)
        }
        DebugClock.restore(app)
        val id = intent.getStringExtra(EXTRA_START_SCREEN)
        if (id == GALLERY_ID) {
            val page = intent.getIntExtra(EXTRA_GALLERY_PAGE, 1)
            return StartTarget.Tool { GalleryScreen(initialPage = page) }
        }
        val target = id?.let { startAt(it, app, firstLaunch) }
        if (firstLaunch) {
            applyOverrides(intent, app, loadDemo = id == null)
            postLockScreen(id, app)
        }
        return target
    }

    /**
     * `lockConfirmRequest` posts Esha's claim and `lockReminder` tonight's Kabir reminder, so they
     * show over Home (activity §7).
     */
    private fun postLockScreen(id: String?, app: PaybakApplication) {
        val snapshot = app.ledger.snapshot.value
        when (id) {
            "lockConfirmRequest" ->
                snapshot.home.pendingClaims.firstOrNull()?.let {
                    app.notifications.postPaymentToConfirm(it.payment.id)
                }
            "lockReminder" ->
                snapshot.ledger.inbox
                    .lastOrNull { it.type == InboxType.PaymentReminder }
                    ?.let { app.notifications.postInboxItem(it.id) }
        }
    }

    private fun startAt(id: String, app: PaybakApplication, firstLaunch: Boolean): StartTarget? {
        val scenario = ScreenIds.scenarios[id]
        val destination = Destination.fromId(id)
        return when {
            scenario != null -> {
                if (firstLaunch) DemoData.load(app, scenario.seed, avatar = scenario.avatar)
                StartTarget.Flow(Destination.Main, scenario.mainState(), debugScreen = id)
            }
            destination != null && destination != Destination.Main -> {
                if (firstLaunch && id !in preSignIn) app.profileStore.update { SampleProfile }
                StartTarget.Flow(destination, debugScreen = id)
            }
            else -> {
                Log.w(TAG, "Unknown startScreen \"$id\"; starting normally")
                null
            }
        }
    }

    private fun applyOverrides(intent: Intent, app: PaybakApplication, loadDemo: Boolean) {
        if (loadDemo && intent.getBooleanExtra(EXTRA_DEMO_DATA, false))
            DemoData.load(app, DemoData.Default)
        intent.getStringExtra(EXTRA_NOW)?.let {
            DebugClock.pin(app, LocalDateTime.parse(it).atZone(app.clock.zone).toInstant())
        }
        intent
            .getStringExtra(EXTRA_SCENARIO)
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.forEach {
                DemoData.apply(app, it)
            }
        flag(intent, EXTRA_PRO)?.let(app.ledger::setPro)
    }

    /** `--ez key true` or `--es key YES|NO|true|false`. */
    private fun flag(intent: Intent, key: String): Boolean? {
        if (intent.extras?.containsKey(key) != true) return null
        val text = intent.getStringExtra(key) ?: return intent.getBooleanExtra(key, false)
        return text.equals("YES", ignoreCase = true) || text.equals("true", ignoreCase = true)
    }
}
