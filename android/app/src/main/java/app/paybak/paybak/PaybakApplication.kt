package app.paybak.paybak

import android.app.Application
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.ledger.LedgerFile
import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.data.ledger.actions.clear
import app.paybak.paybak.domain.AppClock
import app.paybak.paybak.domain.calc.Rates
import app.paybak.paybak.navigation.DeepLinkInbox
import app.paybak.paybak.service.notifications.NotificationService
import app.rive.RiveLog
import app.rive.runtime.kotlin.core.Rive
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaybakApplication : Application() {
    /** Lives as long as the process: background saves and store observers. */
    private val appScope = MainScope()

    /** The saved profile, shared by every screen. */
    val profileStore: ProfileStore by lazy { ProfileStore(this) }

    /** The only source of "now" (real, or pinned by the debug hooks). */
    val clock: AppClock by lazy { AppClock() }

    /** The ledger: every record, read model and action (app-architecture §3). */
    val ledger: LedgerRepository by lazy {
        LedgerRepository(
            file = LedgerFile(File(filesDir, LEDGER_FILE), appScope),
            clock = clock,
            profile = profileStore.profile,
            rates = Rates.parse(assets.open(RATES_FILE).bufferedReader().use { it.readText() }),
            scope = appScope,
        )
    }

    /** Links from notification taps, opened once the app shows. */
    val links = DeepLinkInbox()

    /** Local notifications, kept in step with the ledger. */
    val notifications: NotificationService by lazy { NotificationService(this) }

    override fun onCreate() {
        super.onCreate()
        // Loads the native Rive libraries. Required before any app.rive API, and not automatic:
        // the AAR removes its androidx.startup initializer. Without it every illustration stays
        // blank.
        Rive.init(this)
        if (BuildConfig.DEBUG) {
            RiveLog.logger = RiveLog.LogcatLogger()
        }
        // Reads the ledger off the main thread while the first screen starts, then keeps the
        // notifications in step with it.
        appScope.launch {
            val ledger = withContext(Dispatchers.Default) { ledger }
            ledger.revision.collect { notifications.sync(ledger.snapshot.value) }
        }
    }

    /** Reset onboarding and Delete account: both stores start over. */
    fun resetAccount() {
        profileStore.reset()
        ledger.clear()
    }

    private companion object {
        const val LEDGER_FILE = "ledger.json"
        const val RATES_FILE = "rates.json"
    }
}
