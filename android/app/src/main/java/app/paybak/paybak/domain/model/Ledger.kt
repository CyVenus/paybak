package app.paybak.paybak.domain.model

import kotlinx.serialization.Serializable

/**
 * The ledger document (`ledger.json`, app-architecture §3.2): everything except the profile. It is
 * immutable; the store replaces it through `LedgerRepository.mutate`.
 */
@Serializable
data class Ledger(
    val schemaVersion: Int = SCHEMA_VERSION,
    val settings: Settings = Settings(),
    val people: List<Person> = emptyList(),
    val groups: List<Group> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val components: List<Component> = emptyList(),
    val recurringRules: List<RecurringRule> = emptyList(),
    val drafts: List<Draft> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val inbox: List<InboxItem> = emptyList(),
    /** Fair leftover-paise rotation counters by context key (domain.md §4.1). */
    val rotation: Map<String, Int> = emptyMap(),
    val scheduler: Scheduler = Scheduler(),
    // Lane fields: add optional fields with defaults below your lane's marker (§7.1).
    // lane A fields
    // lane B fields
    // lane C fields
    // lane D fields
) {
    /** True for a new account: no people and no records (Home "First day", domain.md §6.1). */
    val isEmpty: Boolean
        get() =
            people.isEmpty() &&
                groups.isEmpty() &&
                expenses.isEmpty() &&
                payments.isEmpty() &&
                loans.isEmpty()

    fun person(id: String): Person? = people.firstOrNull { it.id == id }

    fun group(id: String): Group? = groups.firstOrNull { it.id == id }

    fun expense(id: String): Expense? = expenses.firstOrNull { it.id == id }

    fun payment(id: String): Payment? = payments.firstOrNull { it.id == id }

    fun loan(id: String): Loan? = loans.firstOrNull { it.id == id }

    fun component(id: String): Component? = components.firstOrNull { it.id == id }

    fun rule(id: String): RecurringRule? = recurringRules.firstOrNull { it.id == id }

    fun draft(id: String): Draft? = drafts.firstOrNull { it.id == id }

    companion object {
        const val SCHEMA_VERSION = 1
    }
}

/** How far `tick` has run (domain.md §10). Null on a new account: the first tick just sets it. */
@Serializable data class Scheduler(val cursor: Moment? = null)

/** Replaces the element with [id] (keeping its position) by [transform]'s result. */
internal inline fun <T> List<T>.replacing(
    matches: (T) -> Boolean,
    transform: (T) -> T,
): List<T> = map { if (matches(it)) transform(it) else it }
