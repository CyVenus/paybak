package app.paybak.paybak.service.qr

/**
 * What Paybak's QR codes hold: the user's invite link (the kit's `PbQrCodeCard` draws it). Lane B
 * (M4) owns it.
 */
object QrCode {
    private const val INVITE_PREFIX = "https://paybak.app/i/"

    fun inviteLink(username: String): String = INVITE_PREFIX + username

    /** The username in a scanned invite link, or null for anything else. */
    fun usernameFrom(link: String): String? =
        link
            .trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("paybak.app/i/")
            .takeIf { it != link.trim() && it.isNotEmpty() && '/' !in it }
}
