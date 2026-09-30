package app.paybak.paybak.data

import java.util.Calendar

/**
 * The Home greeting: "Good morning/afternoon/evening, {first name}" from the local time
 * (05:00–11:59 morning, 12:00–16:59 afternoon, otherwise evening).
 */
fun greeting(
    firstName: String,
    hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
): String {
    val partOfDay =
        when (hourOfDay) {
            in 5..11 -> "morning"
            in 12..16 -> "afternoon"
            else -> "evening"
        }
    return "Good $partOfDay, $firstName"
}
