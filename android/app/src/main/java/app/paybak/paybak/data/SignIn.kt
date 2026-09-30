package app.paybak.paybak.data

/** The contact a sign-in code goes to, as saved with the profile. */
data class SignInContact(val method: SignInMethod, val value: String)

/** Something @ something . something, with no spaces (flow.md). */
private val EmailPattern = Regex("""[^\s@]+@[^\s@]+\.[^\s@]+""")

/** Digits, spaces and dashes, after an optional leading "+". */
private val PhonePattern = Regex("""\+?[\d -]+""")

private const val MIN_PHONE_DIGITS = 7

/** Figma: "phone numbers get +91" when typed without a country code. */
private const val DEFAULT_PHONE_PREFIX = "+91 "

/**
 * Reads the Sign in field. A plausible email is trimmed; a plausible phone number (at least 7
 * digits) keeps its own "+" prefix or gets "+91 ". Anything else is null, which keeps Send code
 * disabled.
 */
fun parseSignInContact(input: String): SignInContact? {
    val text = input.trim()
    return when {
        EmailPattern.matches(text) -> SignInContact(SignInMethod.Email, text)
        PhonePattern.matches(text) && text.count(Char::isDigit) >= MIN_PHONE_DIGITS ->
            SignInContact(
                SignInMethod.Phone,
                if (text.startsWith('+')) text else DEFAULT_PHONE_PREFIX + text,
            )
        else -> null
    }
}

/** The 6-digit sign-in code. There is no backend yet, so every code "sent" is [CORRECT]. */
object SignInCode {
    const val CORRECT = "000000"

    /** Resend unlocks this long after a code is sent. */
    const val RESEND_AFTER_SECONDS = 30

    fun isCorrect(code: String): Boolean = code == CORRECT
}
