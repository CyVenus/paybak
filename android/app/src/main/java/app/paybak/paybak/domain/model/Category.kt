package app.paybak.paybak.domain.model

/** The fixed category list in picker order (domain.md §1.12); [icon] is the icon key. */
enum class Category(val id: String, val label: String, val icon: String) {
    Food("food", "Food", "food"),
    Travel("travel", "Travel", "car"),
    Stays("stays", "Stays", "bed"),
    Fun("fun", "Fun", "ticket"),
    Rent("rent", "Rent", "home"),
    Bills("bills", "Bills", "bolt"),
    Shopping("shopping", "Shopping", "shopping-bag"),
    Other("other", "Other", "tag");

    companion object {
        fun of(id: String): Category = entries.firstOrNull { it.id == id } ?: Other
    }
}
