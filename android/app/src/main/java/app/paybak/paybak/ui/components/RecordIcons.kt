package app.paybak.paybak.ui.components

import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.ui.icons.PbIcon

/** A category's icon (domain.md §1.12). */
val Category.pbIcon: PbIcon
    get() = iconForKey(icon)

/**
 * The icon for a stored icon key (categories, group types and projects: `plane`, `home`, `drone`
 * …); the tag icon for anything unknown.
 */
fun iconForKey(key: String): PbIcon =
    when (key) {
        "food" -> PbIcon.Food
        "car" -> PbIcon.Car
        "bed" -> PbIcon.Bed
        "ticket" -> PbIcon.Ticket
        "home" -> PbIcon.Home
        "bolt" -> PbIcon.Bolt
        "shopping-bag" -> PbIcon.ShoppingBag
        "plane" -> PbIcon.Plane
        "people" -> PbIcon.People
        "drone" -> PbIcon.Drone
        "package" -> PbIcon.Package
        "wifi" -> PbIcon.WiFi
        "flame" -> PbIcon.Flame
        else -> PbIcon.Tag
    }
