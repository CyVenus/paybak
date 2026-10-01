package app.paybak.paybak.feature.recurring

import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon

/**
 * The icon of a recurring expense: Flame for gas and WiFi for internet (Figma's Cooking gas and
 * Wi-Fi rows), otherwise its category's icon (Rent → Home).
 */
internal fun ruleIcon(title: String, category: String): PbIcon {
    val words = title.lowercase().split(Regex("[^a-z-]+"))
    return when {
        "gas" in words -> PbIcon.Flame
        words.any { it in setOf("wi-fi", "wifi", "internet", "broadband") } -> PbIcon.WiFi
        else -> Category.of(category).pbIcon
    }
}
