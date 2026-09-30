package app.paybak.paybak.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize

/**
 * The 65 Figma icons (`Icon / …` on "02 Components"), HugeIcons stroke-rounded on a 24 grid.
 * Drawables keep the Figma names: `Icon / Chevron Left` is `ic_chevron_left`.
 *
 * [tintable] is false for the brand exceptions that must keep their official colours.
 */
enum class PbIcon(@param:DrawableRes val resId: Int, val tintable: Boolean = true) {
    Home(R.drawable.ic_home),
    Groups(R.drawable.ic_groups),
    Plus(R.drawable.ic_plus),
    Activity(R.drawable.ic_activity),
    Profile(R.drawable.ic_profile),
    Bell(R.drawable.ic_bell),
    ChevronRight(R.drawable.ic_chevron_right),
    ChevronLeft(R.drawable.ic_chevron_left),
    Settings(R.drawable.ic_settings),
    Mail(R.drawable.ic_mail),
    Receipt(R.drawable.ic_receipt),
    Food(R.drawable.ic_food),
    Bolt(R.drawable.ic_bolt),
    Wallet(R.drawable.ic_wallet),
    MoneyIn(R.drawable.ic_money_in),
    MoneyOut(R.drawable.ic_money_out),
    Exchange(R.drawable.ic_exchange),
    Lend(R.drawable.ic_lend),
    Calendar(R.drawable.ic_calendar),
    Check(R.drawable.ic_check),
    CheckCircle(R.drawable.ic_check_circle),
    Close(R.drawable.ic_close),
    Alert(R.drawable.ic_alert),
    UserAdd(R.drawable.ic_user_add),
    People(R.drawable.ic_people),

    /** Filled Apple logo: tint it black or white only. */
    Apple(R.drawable.ic_apple),

    /** Official four-colour Google "G": never tinted. */
    Google(R.drawable.ic_google, tintable = false),
    Search(R.drawable.ic_search),
    Camera(R.drawable.ic_camera),
    Copy(R.drawable.ic_copy),
    Car(R.drawable.ic_car),
    Bed(R.drawable.ic_bed),
    Ticket(R.drawable.ic_ticket),
    ShoppingBag(R.drawable.ic_shopping_bag),
    Tag(R.drawable.ic_tag),
    Split(R.drawable.ic_split),
    Note(R.drawable.ic_note),
    ArrowRight(R.drawable.ic_arrow_right),
    ArrowUp(R.drawable.ic_arrow_up),
    Sparkles(R.drawable.ic_sparkles),
    Plane(R.drawable.ic_plane),
    Drone(R.drawable.ic_drone),
    Package(R.drawable.ic_package),
    QrCode(R.drawable.ic_qr_code),
    Scan(R.drawable.ic_scan),
    Link(R.drawable.ic_link),
    Share(R.drawable.ic_share),
    Logout(R.drawable.ic_logout),
    Repeat(R.drawable.ic_repeat),
    Flag(R.drawable.ic_flag),
    Delete(R.drawable.ic_delete),
    Restore(R.drawable.ic_restore),
    Chart(R.drawable.ic_chart),
    Mic(R.drawable.ic_mic),
    Image(R.drawable.ic_image),
    Flame(R.drawable.ic_flame),
    WiFi(R.drawable.ic_wi_fi),
    Crown(R.drawable.ic_crown),
    Bank(R.drawable.ic_bank),
    Download(R.drawable.ic_download),
    Star(R.drawable.ic_star),

    /** Official WhatsApp logo (share sheet only): never tinted. */
    WhatsApp(R.drawable.ic_whatsapp, tintable = false),
    Shuffle(R.drawable.ic_shuffle),
    Lock(R.drawable.ic_lock),
    Help(R.drawable.ic_help),
}

/**
 * Draws [icon] at [size]. The whole 24-grid drawing scales, so the 1.5 stroke becomes 1.25 at 20 dp
 * and 1.0 at 16 dp, exactly like Figma. [tint] is ignored for the brand exceptions.
 */
@Composable
fun PbIconImage(
    icon: PbIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = PbSize.IconLg,
    tint: Color = PbColors.Icon.Primary,
) {
    Image(
        painter = painterResource(icon.resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = if (icon.tintable) ColorFilter.tint(tint) else null,
    )
}

@Preview
@Composable
private fun PbIconImagePreview() {
    PbIconImage(PbIcon.Bell, contentDescription = null)
}
