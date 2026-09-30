package app.paybak.paybak.data

/** "name@bank": two or more of letters, digits, ".", "_" or "-", then "@" and a bank handle. */
private val UpiIdPattern = Regex("""[A-Za-z0-9._-]{2,}@[A-Za-z0-9]{2,}""")

/** Whether [text] (trimmed) looks like a UPI ID, e.g. "arjun@okaxis". */
fun isPlausibleUpiId(text: String): Boolean = UpiIdPattern.matches(text.trim())
