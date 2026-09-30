package app.paybak.paybak.domain.model

import java.util.UUID

/** The user's own person id in every record (domain.md §0). */
const val ME = "me"

/** A new record id: a lowercase UUID (demo ids are readable, e.g. `p-rohan`). */
fun newId(): String = UUID.randomUUID().toString().lowercase()
