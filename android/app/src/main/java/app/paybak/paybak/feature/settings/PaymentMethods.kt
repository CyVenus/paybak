package app.paybak.paybak.feature.settings

import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.data.isPlausibleUpiId
import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.domain.model.SavedPaymentMethod
import app.paybak.paybak.domain.model.newId

/**
 * Payment methods (screens-settings §4–5, §12.2): exactly one is primary, and friends only ever see
 * that one. The first method added becomes primary. [UserProfile.normalized] keeps `upiId` in step
 * with the primary UPI method whenever a new list is saved.
 */
fun UserProfile.addingUpi(upi: String): UserProfile = adding(
    SavedPaymentMethod(newId(), SavedMethodKind.Upi, value = upi.trim())
)

/** A bank account shown as "{bank} ···· {last 4 digits}". */
fun UserProfile.addingBank(bankName: String, accountNumber: String): UserProfile = adding(
    SavedPaymentMethod(
        newId(),
        SavedMethodKind.Bank,
        bankName = bankName.trim(),
        last4 = accountNumber.filter(Char::isDigit).takeLast(LAST_DIGITS),
    )
)

fun UserProfile.makingPrimary(id: String): UserProfile =
    copy(paymentMethods = paymentMethods.map { it.copy(primary = it.id == id) })

/** Removing the primary method makes the next one primary. */
fun UserProfile.removingMethod(id: String): UserProfile {
    val left = paymentMethods.filter { it.id != id }
    val primary = left.firstOrNull { it.primary } ?: left.firstOrNull()
    return copy(paymentMethods = left.map { it.copy(primary = it == primary) })
}

private fun UserProfile.adding(method: SavedPaymentMethod) =
    copy(paymentMethods = paymentMethods + method.copy(primary = paymentMethods.none { it.primary }))

/** Why a UPI ID can't be saved, or null when it can. */
enum class UpiProblem {
    /** No "@", or not "name@bank" (Figma's error: "Enter a UPI ID like name@bank"). */
    Invalid,

    /** The user already has it. */
    Duplicate,
}

fun UserProfile.upiProblem(text: String): UpiProblem? =
    when {
        !isPlausibleUpiId(text) -> UpiProblem.Invalid
        paymentMethods.any { it.value.equals(text.trim(), ignoreCase = true) } ->
            UpiProblem.Duplicate
        else -> null
    }

/** A bank account needs a name and at least the 4 digits friends will see. */
fun isValidBankAccount(bankName: String, accountNumber: String): Boolean =
    bankName.isNotBlank() && accountNumber.count(Char::isDigit) >= LAST_DIGITS

/**
 * The UPI tab's prefill (screens-settings §5): for a bank method whose UPI handle is known and not
 * added yet, the primary UPI ID's name at that bank's handle, e.g. "arjun@okhdfcbank" for HDFC
 * Bank. Empty when there's nothing to suggest.
 */
fun UserProfile.suggestedUpi(): String {
    val name = upiId.substringBefore('@').ifEmpty { username }
    if (name.isEmpty()) return ""
    return paymentMethods
        .filter { it.kind == SavedMethodKind.Bank }
        .mapNotNull { bank -> upiHandle(bank.bankName.orEmpty())?.let { "$name@$it" } }
        .firstOrNull { upi -> paymentMethods.none { it.value.equals(upi, ignoreCase = true) } }
        .orEmpty()
}

/** The UPI handle of a bank's own app (Google Pay's "ok" handles). */
private fun upiHandle(bankName: String): String? {
    val bank = bankName.lowercase()
    return BankHandles.entries.firstOrNull { (key) -> key in bank }?.value
}

private val BankHandles =
    linkedMapOf("hdfc" to "okhdfcbank", "icici" to "okicici", "sbi" to "oksbi", "axis" to "okaxis")

private const val LAST_DIGITS = 4
