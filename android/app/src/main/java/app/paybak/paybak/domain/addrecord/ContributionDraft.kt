package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.actions.PERCENT_TOTAL_BPS
import app.paybak.paybak.domain.model.Contribution
import app.paybak.paybak.domain.model.ContributionRule

/**
 * A new project's contribution rule (record-lend-group §6.4, §8.3): Equal shows each member's
 * share; Percent and Fixed take a typed value per member ("25", "4000").
 */
object ContributionDraft {
    /** 100 % ÷ [members]: "25%", "33.3%". */
    fun equalShare(members: Int): String {
        if (members <= 0) return "0%"
        val tenths = Math.round(1000.0 / members)
        return if (tenths % 10 == 0L) "${tenths / 10}%" else "${tenths / 10}.${tenths % 10}%"
    }

    /** Percent must add up to 100 %; Fixed needs an amount for everyone. */
    fun isValid(
        rule: ContributionRule,
        typed: Map<String, String>,
        members: List<String>,
    ): Boolean =
        when (rule) {
            ContributionRule.Equal -> true
            ContributionRule.Percent ->
                members.sumOf { basisPoints(typed[it]) } == PERCENT_TOTAL_BPS
            ContributionRule.Fixed ->
                members.all { AmountEntry.minor(typed[it].orEmpty(), "INR") > 0 }
        }

    /** The saved rule: basis points (Percent) or minor units of [currency] (Fixed). */
    fun contribution(
        rule: ContributionRule,
        typed: Map<String, String>,
        members: List<String>,
        currency: String,
    ): Contribution =
        Contribution(
            rule,
            when (rule) {
                ContributionRule.Equal -> emptyMap()
                ContributionRule.Percent -> members.associateWith { basisPoints(typed[it]) }
                ContributionRule.Fixed ->
                    members.associateWith { AmountEntry.minor(typed[it].orEmpty(), currency) }
            },
        )

    /** The helper under the Contribution control; [valid] false turns it red. */
    fun helper(rule: ContributionRule): String =
        when (rule) {
            ContributionRule.Equal -> "Everyone pays the same share of what’s spent."
            ContributionRule.Percent -> "Set each person’s share. Shares must add up to 100%."
            ContributionRule.Fixed -> "Set a fixed amount for each person."
        }

    private fun basisPoints(text: String?): Long = AmountEntry.minor(text.orEmpty(), "INR")
}
