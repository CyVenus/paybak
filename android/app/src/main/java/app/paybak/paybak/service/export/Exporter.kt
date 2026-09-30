package app.paybak.paybak.service.export

import android.content.Context
import app.paybak.paybak.domain.calc.LedgerView
import java.io.File
import java.time.LocalDate

enum class ExportFormat(val extension: String, val mimeType: String) {
    Csv("csv", "text/csv"),
    Pdf("pdf", "application/pdf"),
}

/**
 * Writes the records of a range and some groups as CSV or PDF into the cache's `exports/` folder,
 * for `SystemShare.shareFile` (app-architecture §4). STUB owned by lane C (M8): it writes nothing
 * yet.
 */
object Exporter {
    suspend fun export(
        context: Context,
        view: LedgerView,
        start: LocalDate,
        end: LocalDate,
        groups: Set<String>,
        format: ExportFormat,
    ): File? = null
}
