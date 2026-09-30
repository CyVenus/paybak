package app.paybak.paybak.debug

import android.content.Context
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.seed.DemoProfile
import app.paybak.paybak.domain.seed.DemoSeed
import java.time.Instant
import java.time.LocalDate

/**
 * Loads the bundled demo (`assets/seed/demo.json`, debug builds only) into both stores
 * (app-architecture §3.8): at Figma parity (Wed 30 Sep 2026, clock pinned to 21:15) or around today
 * (load day = today, real clock).
 */
internal object DemoData {
    /** The debug menu's "Load demo data" and the `D` seed: the base plus Esha's pending claim. */
    val Default = listOf("eshaClaimsPayment")

    private var cached: DemoSeed? = null

    fun seed(context: Context): DemoSeed =
        cached
            ?: DemoSeed(
                    context.assets.open("seed/demo.json").bufferedReader().use { it.readText() }
                )
                .also { cached = it }

    fun load(
        app: PaybakApplication,
        scenarios: List<String>,
        atFigmaDate: Boolean = true,
        avatar: AvatarLook? = null,
    ) {
        val seed = seed(app)
        val zone = app.clock.zone
        val anchor = if (atFigmaDate) seed.figmaDay else LocalDate.now(zone)
        val now = if (atFigmaDate) seed.figmaNow(zone) else Instant.now()
        val loaded = seed.load(anchor, now, zone, scenarios)
        app.profileStore.update { it.withDemo(loaded.profile, avatar) }
        DebugClock.pin(app, if (atFigmaDate || loaded.now != now) loaded.now else null)
        app.ledger.replace(loaded.ledger)
    }

    /** Runs one more seed scenario on today's ledger ("Apply scenario…", `--es scenario`). */
    fun apply(app: PaybakApplication, name: String) {
        val (ledger, now) =
            seed(app)
                .applyScenario(
                    app.ledger.ledger.value,
                    name,
                    anchor = app.clock.today(),
                    now = app.clock.now(),
                    zone = app.clock.zone,
                    defaultCurrency = app.ledger.defaultCurrency,
                )
        if (now != app.clock.now()) DebugClock.pin(app, now)
        app.ledger.replace(ledger)
    }

    private fun UserProfile.withDemo(demo: DemoProfile, avatar: AvatarLook?) =
        copy(
            name = demo.name,
            avatar = avatar?.let(AvatarChoice::Character) ?: AvatarChoice.Preset(demo.avatar.index),
            currencyCode = demo.currencyCode,
            upiId = demo.upiID,
            signInMethod =
                SignInMethod.entries.firstOrNull {
                    it.name.equals(demo.signInMethod, ignoreCase = true)
                },
            contact = demo.contact,
            onboardingComplete = demo.onboardingComplete,
            username = demo.username,
            pronoun = demo.pronoun,
            paymentMethods = demo.paymentMethods,
            showPaymentToFriends = demo.showPaymentToFriends,
        )
}
