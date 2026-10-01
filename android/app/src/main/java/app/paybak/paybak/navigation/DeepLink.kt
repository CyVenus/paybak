package app.paybak.paybak.navigation

import app.paybak.paybak.domain.model.PaymentMethod
import java.net.URI
import java.net.URLDecoder
import java.time.YearMonth

/**
 * An internal link carried by a notification or an inbox row (app-architecture §2.6). No URL scheme
 * is registered: links only travel inside the app.
 */
sealed interface DeepLink {
    /** The Activity timeline with the claim card on top; [notReceived] also opens its sheet. */
    data class Claim(val paymentId: String, val notReceived: Boolean = false) : DeepLink

    /** Record payment prefilled for a debt you owe. */
    data class RecordPayment(
        val toId: String,
        val amount: Long?,
        val groupId: String?,
        val method: PaymentMethod? = null,
    ) : DeepLink

    data class Insights(val month: YearMonth) : DeepLink

    data class Remind(val personId: String) : DeepLink

    data class Expense(val expenseId: String) : DeepLink

    data class Payment(val paymentId: String) : DeepLink

    data class RecurringDraft(val draftId: String) : DeepLink

    companion object {
        /** Null for anything that isn't a well-formed `paybak://` link. */
        fun parse(link: String): DeepLink? {
            val uri = runCatching { URI(link) }.getOrNull() ?: return null
            if (uri.scheme != "paybak") return null
            val query =
                uri.rawQuery
                    .orEmpty()
                    .split('&')
                    .filter { '=' in it }
                    .associate {
                        val (key, value) = it.split('=', limit = 2)
                        key to URLDecoder.decode(value, "UTF-8")
                    }
            val pathId = uri.path.orEmpty().trim('/').takeIf { it.isNotEmpty() }
            return when (uri.host) {
                "activity" ->
                    query["claim"]?.let {
                        Claim(it, notReceived = query["action"] == "notReceived")
                    }
                "record-payment" ->
                    query["to"]?.let {
                        RecordPayment(
                            toId = it,
                            amount = query["amount"]?.toLongOrNull(),
                            groupId = query["context"]?.removePrefix("group:"),
                            method =
                                PaymentMethod.entries.firstOrNull {
                                    it.name.equals(query["method"], ignoreCase = true)
                                },
                        )
                    }
                "insights" ->
                    query["month"]?.let {
                        runCatching { Insights(YearMonth.parse(it)) }.getOrNull()
                    }
                "remind" -> query["person"]?.let(::Remind)
                "expense" -> pathId?.let(::Expense)
                "payment" -> pathId?.let(::Payment)
                "recurring-draft" -> pathId?.let(::RecurringDraft)
                else -> null
            }
        }
    }
}
