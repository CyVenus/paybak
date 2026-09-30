package app.paybak.paybak.service.export

import android.content.Context
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.settings.ExportPeriod
import app.paybak.paybak.domain.settings.exportCsv
import app.paybak.paybak.domain.settings.exportRecords
import app.paybak.paybak.service.SystemShare
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ExportFormat(val extension: String, val mimeType: String) {
    Csv("csv", "text/csv"),
    Pdf("pdf", "application/pdf"),
}

/**
 * Writes the records of a period and some Export rows (group ids, or `NO_GROUP`) as CSV or PDF
 * into the cache's `exports/` folder, for `SystemShare.shareFile` (app-architecture §4). The file
 * is named after the range: "Paybak records 1 Sep – 30 Sep 2026.pdf".
 */
object Exporter {
    private const val UTF8_BOM = "﻿"

    suspend fun export(
        context: Context,
        view: LedgerView,
        period: ExportPeriod,
        groups: Set<String>,
        format: ExportFormat,
    ): File =
        withContext(Dispatchers.IO) {
            val records = view.exportRecords(period, groups)
            val dir = SystemShare.exportsDir(context)
            dir.listFiles()?.forEach(File::delete)
            val file = File(dir, "Paybak records ${period.label}.${format.extension}")
            when (format) {
                ExportFormat.Csv ->
                    file.writeText(UTF8_BOM + exportCsv(records, view.defaultCurrency))
                ExportFormat.Pdf ->
                    file.outputStream().use {
                        RecordsPdf(context, view.defaultCurrency).write(period, records, it)
                    }
            }
            file
        }
}
