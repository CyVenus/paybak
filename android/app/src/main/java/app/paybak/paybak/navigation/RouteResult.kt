package app.paybak.paybak.navigation

import app.paybak.paybak.domain.model.Day as DayValue
import app.paybak.paybak.domain.model.ReceiptResult
import app.paybak.paybak.domain.model.RepeatRule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What a cross-lane picker returns through the navigator's result channel (§2.7). */
@Serializable
sealed interface RouteResult {
    @Serializable @SerialName("people") data class People(val personIds: List<String>) : RouteResult

    @Serializable @SerialName("person") data class Person(val personId: String) : RouteResult

    @Serializable @SerialName("currency") data class Currency(val code: String) : RouteResult

    /** A picked date; null = "None" (due dates). */
    @Serializable @SerialName("day") data class Day(val day: DayValue?) : RouteResult

    /** A picked group; null = "None". */
    @Serializable @SerialName("group") data class Group(val groupId: String?) : RouteResult

    /** The Repeat sheet's rule; null = "Never". */
    @Serializable @SerialName("repeatRule") data class Repeat(val rule: RepeatRule?) : RouteResult

    @Serializable @SerialName("receipt") data class Receipt(val result: ReceiptResult) : RouteResult
}
