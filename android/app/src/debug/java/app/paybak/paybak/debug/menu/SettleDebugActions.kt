package app.paybak.paybak.debug.menu

/**
 * The debug menu's Settle section, owned by lane B (app-architecture §3.10). Settle up needs none of
 * its own: the friend's side of a payment (a claim, Confirm, Not received) is in the core Friend’s
 * side section.
 */
internal val SettleDebugActions: List<DebugAction> = emptyList()
