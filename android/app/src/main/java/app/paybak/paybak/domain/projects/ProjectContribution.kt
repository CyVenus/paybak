package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.actions.PERCENT_TOTAL_BPS
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.ContributionDraft
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Contribution
import app.paybak.paybak.domain.model.ContributionRule
import kotlin.math.abs
import kotlinx.serialization.Serializable

/** Percent values are typed in percent and stored in basis points; this is their "currency". */
private const val PERCENT_UNITS = "INR"

/** Whether the typed values can be applied, and the helper under the rule control. */
data class ContributionCheck(val valid: Boolean, val helper: String)

/**
 * The contribution rule as edited on Project settings (screens-projects §6.7): Equal shows each
 * member's "25%"; Percent and Fixed turn the rows into fields ("25", "15000") that must add up.
 * The rule applies only while it adds up; until then the saved one stays in force.
 */
@Serializable
data class ContributionEdit(val rule: ContributionRule, val typed: Map<String, String>) {

    /** Percent must make 100 %; Fixed must make the budget (or be set for everyone without one). */
    fun check(members: List<String>, budget: Long?, currency: String): ContributionCheck {
        val helper = ContributionDraft.helper(rule)
        fun invalid(remainder: String) = ContributionCheck(false, "$helper $remainder")
        return when (rule) {
            ContributionRule.Equal -> ContributionCheck(true, helper)
            ContributionRule.Percent -> {
                val left = PERCENT_TOTAL_BPS - members.sumOf { minor(it, PERCENT_UNITS) }
                val amount = AmountEntry.text(abs(left), PERCENT_UNITS) + "%"
                when {
                    left == 0L -> ContributionCheck(true, helper)
                    left > 0 -> invalid("$amount left.")
                    else -> invalid("$amount over.")
                }
            }
            ContributionRule.Fixed -> {
                val amounts = members.map { minor(it, currency) }
                val left = budget?.let { it - amounts.sum() }
                val amount = Money.format(left ?: 0, currency)
                when {
                    left == null -> ContributionCheck(amounts.all { it > 0 }, helper)
                    left == 0L -> ContributionCheck(true, helper)
                    left > 0 -> invalid("$amount of the budget left.")
                    else -> invalid("$amount over the budget.")
                }
            }
        }
    }

    /** The saved rule: basis points (Percent) or minor units (Fixed). */
    fun contribution(members: List<String>, currency: String): Contribution =
        ContributionDraft.contribution(rule, typed, members, currency)

    private fun minor(personId: String, currency: String) =
        AmountEntry.minor(typed[personId].orEmpty(), currency)

    companion object {
        /** The saved rule as the fields show it. */
        fun of(contribution: Contribution, members: List<String>, currency: String) =
            ContributionEdit(
                contribution.rule,
                when (contribution.rule) {
                    ContributionRule.Equal -> emptyMap()
                    ContributionRule.Percent ->
                        members.associateWith {
                            AmountEntry.text(contribution.values[it] ?: 0, PERCENT_UNITS)
                        }
                    ContributionRule.Fixed ->
                        members.associateWith {
                            AmountEntry.text(contribution.values[it] ?: 0, currency)
                        }
                },
            )

        /**
         * Switching to [rule] starts from an equal split: 25 % each, or the budget (else what's
         * spent so far) ÷ n each, with the leftover units on the first members.
         */
        fun prefill(
            rule: ContributionRule,
            members: List<String>,
            budget: Long?,
            spent: Long,
            currency: String,
        ): ContributionEdit {
            fun equal(total: Long, units: String) =
                Splits.equal(total, members).shares.mapValues { AmountEntry.text(it.value, units) }
            return ContributionEdit(
                rule,
                when (rule) {
                    ContributionRule.Equal -> emptyMap()
                    ContributionRule.Percent -> equal(PERCENT_TOTAL_BPS, PERCENT_UNITS)
                    ContributionRule.Fixed -> equal(budget ?: spent, currency)
                },
            )
        }
    }
}
