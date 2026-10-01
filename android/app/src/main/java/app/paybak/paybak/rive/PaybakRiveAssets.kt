package app.paybak.paybak.rive

import androidx.annotation.RawRes
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R

/**
 * One config per Paybak `.riv` file (rive.md). The illustration files also hold Rive's own logo
 * artboards, so always load the artboard and its state machine by name.
 *
 * @property stateMachine The state machine to drive; the illustrations name theirs after the
 *   artboard.
 * @property viewSize The artboard size, which is the size of the Rive view.
 * @property slotSize The Figma layout slot. Get Started, Notifications and AllSquare add 12 dp of
 *   bleed on every side, so their view is centred on the slot and overflows it.
 * @property tapTrigger The generic "something was tapped" trigger, observed for a light haptic;
 *   null when the file has no listeners.
 */
enum class PaybakRiveAsset(
    @param:RawRes val resId: Int,
    val artboard: String,
    val stateMachine: String,
    val viewSize: DpSize,
    val slotSize: DpSize,
    val tapTrigger: String?,
) {
    Onboarding(
        resId = R.raw.paybak_onboarding,
        artboard = "Onboarding",
        stateMachine = "Onboarding",
        viewSize = DpSize(362.dp, 340.dp),
        slotSize = DpSize(362.dp, 340.dp),
        tapTrigger = null,
    ),
    GetStarted(
        resId = R.raw.paybak_getstarted,
        artboard = "Get Started",
        stateMachine = "Get Started",
        viewSize = DpSize(386.dp, 284.dp),
        slotSize = DpSize(362.dp, 260.dp),
        tapTrigger = "personTapped",
    ),
    Notifications(
        resId = R.raw.paybak_notifications,
        artboard = "Notifications",
        stateMachine = "Notifications",
        viewSize = DpSize(386.dp, 324.dp),
        slotSize = DpSize(362.dp, 300.dp),
        tapTrigger = "bellTapped",
    ),
    AllSet(
        resId = R.raw.paybak_allset,
        artboard = "All Set",
        stateMachine = "All Set",
        viewSize = DpSize(362.dp, 300.dp),
        slotSize = DpSize(362.dp, 300.dp),
        tapTrigger = "personTapped",
    ),
    HomeFirstDay(
        resId = R.raw.paybak_homefirstday,
        artboard = "First Day",
        stateMachine = "First Day",
        viewSize = DpSize(240.dp, 180.dp),
        slotSize = DpSize(240.dp, 180.dp),
        tapTrigger = "characterTapped",
    ),
    HomeAllSquare(
        resId = R.raw.paybak_home_allset,
        artboard = "AllSquare",
        stateMachine = "AllSquare",
        viewSize = DpSize(264.dp, 204.dp),
        slotSize = DpSize(240.dp, 180.dp),
        tapTrigger = "tapped",
    ),

    /**
     * The payer's full-screen "payment approved" scene (`paybak-payment.riv`, PaymentApprovedHost).
     * `main` is built with Rive layouts: drawn with `Fit.Layout` it takes the screen's size in dp,
     * paints it white and keeps the palm at the bottom. No view model, inputs or listeners.
     */
    Payment(
        resId = R.raw.paybak_payment,
        artboard = "main",
        stateMachine = "State Machine 1",
        viewSize = DpSize(402.dp, 874.dp),
        slotSize = DpSize(402.dp, 874.dp),
        tapTrigger = null,
    );

    companion object {
        /** Onboarding's number property: the Welcome step, 1…3. */
        const val STEP_PROPERTY = "step"
    }
}
