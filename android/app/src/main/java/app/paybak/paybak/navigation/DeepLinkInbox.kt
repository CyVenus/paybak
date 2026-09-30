package app.paybak.paybak.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Holds an internal link (a notification tap, the debug `link` key) until the app is showing and
 * opens it; links that arrive during onboarding are dropped by the caller.
 */
class DeepLinkInbox {
    private val latest = MutableStateFlow<String?>(null)

    val pending: Flow<String> = latest.filterNotNull()

    fun post(link: String) {
        latest.value = link
    }

    fun consume(link: String) {
        latest.compareAndSet(link, null)
    }
}
