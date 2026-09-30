package app.paybak.paybak.data.ledger

import android.util.Log
import androidx.core.util.AtomicFile
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.LedgerJson
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * `filesDir/ledger.json` (app-architecture §3.6). Reads happen once at launch; each write is
 * encoded and saved atomically on [Dispatchers.IO] through a conflated channel, so the caller never
 * waits, the last state always wins and writes stay in order. An unreadable file is moved aside as
 * `ledger.corrupt-<timestamp>.json` and the app starts empty.
 */
class LedgerFile(private val file: File, scope: CoroutineScope) {
    private val atomic = AtomicFile(file)
    private val pending = Channel<Ledger>(Channel.CONFLATED)

    init {
        scope.launch(Dispatchers.IO) {
            for (ledger in pending) save(LedgerJson.encodeToString(Ledger.serializer(), ledger))
        }
    }

    fun read(): Ledger {
        if (!file.exists()) return Ledger()
        return runCatching {
                LedgerJson.decodeFromString(
                    Ledger.serializer(),
                    atomic.readFully().decodeToString(),
                )
            }
            .getOrElse { error ->
                Log.e(TAG, "Unreadable ledger; starting empty", error)
                file.renameTo(
                    File(file.parentFile, "ledger.corrupt-${System.currentTimeMillis()}.json")
                )
                Ledger()
            }
    }

    fun write(ledger: Ledger) {
        pending.trySend(ledger)
    }

    private fun save(json: String) {
        val stream = atomic.startWrite()
        runCatching {
            stream.write(json.encodeToByteArray())
            atomic.finishWrite(stream)
        }
            .onFailure {
                atomic.failWrite(stream)
                Log.e(TAG, "Couldn't save the ledger", it)
            }
    }

    private companion object {
        const val TAG = "LedgerFile"
    }
}
