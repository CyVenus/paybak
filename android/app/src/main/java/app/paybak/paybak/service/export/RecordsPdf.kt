package app.paybak.paybak.service.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import app.paybak.paybak.R
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.settings.ExportPeriod
import app.paybak.paybak.domain.settings.ExportRecord
import app.paybak.paybak.domain.settings.ExportType
import java.io.OutputStream

/** A4 in PostScript points. */
private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val MARGIN = 40f
private const val FOOTER_Y = PAGE_HEIGHT - 24f
private const val LINE = 15f

/** Table columns: left edges (Amount is right-aligned to its column's end). */
private const val COL_DATE = MARGIN
private const val COL_TITLE = 96f
private const val COL_PAID_BY = 256f
private const val COL_AMOUNT_END = 408f
private const val COL_SHARES = 420f
private const val WIDTH_TITLE = 150f
private const val WIDTH_PAID_BY = 80f
private const val WIDTH_SHARES = PAGE_WIDTH - MARGIN - COL_SHARES

private const val INK = 0xFF0A0A0A.toInt()
private const val SECONDARY = 0xFF6B6B6B.toInt()
private const val RULE = 0xFFEBEBEB.toInt()

/**
 * The PDF export (screens-settings §9, proposal): "Paybak records", the range, then one section
 * per group with a table (Date · Title · Paid by · Amount · Each person's share) and its
 * expenses total, on A4 pages with the footer "Paybak never moves money."
 */
internal class RecordsPdf(context: Context, private val defaultCurrency: String) {
    private val regular = font(context, R.font.manrope_regular)
    private val bold = font(context, R.font.manrope_bold)

    private val title = paint(bold, 22f, INK)
    private val subtitle = paint(regular, 12f, SECONDARY)
    private val heading = paint(bold, 14f, INK)
    private val label = paint(bold, 9f, SECONDARY)
    private val body = paint(regular, 10f, INK)
    private val total = paint(bold, 10f, INK)
    private val rule = Paint().apply { color = RULE; strokeWidth = 1f }

    private val document = PdfDocument()
    private var page: PdfDocument.Page? = null
    private var y = 0f

    fun write(period: ExportPeriod, records: List<ExportRecord>, out: OutputStream) {
        newPage()
        canvas.drawText("Paybak records", MARGIN, y + 22f, title)
        y += 42f
        canvas.drawText(period.label, MARGIN, y, subtitle)
        y += 28f
        if (records.isEmpty()) canvas.drawText("No records in this range.", MARGIN, y, body)
        records.groupBy { it.group }.forEach { (group, rows) -> section(group.name, rows) }
        finishPage()
        document.writeTo(out)
        document.close()
    }

    private fun section(name: String, rows: List<ExportRecord>) {
        ensureSpace(LINE * 4)
        canvas.drawText(name, MARGIN, y, heading)
        y += 20f
        columnLabels()
        rows.forEach(::row)
        val spent = rows.filter { it.type == ExportType.Expense }.sumOf { it.defaultAmount }
        ensureSpace(LINE)
        canvas.drawText("Expenses total", COL_TITLE, y, total)
        drawRight(Money.format(spent, defaultCurrency), COL_AMOUNT_END, total)
        y += LINE * 2
    }

    private fun columnLabels() {
        canvas.drawText("DATE", COL_DATE, y, label)
        canvas.drawText("TITLE", COL_TITLE, y, label)
        canvas.drawText("PAID BY", COL_PAID_BY, y, label)
        drawRight("AMOUNT", COL_AMOUNT_END, label)
        canvas.drawText("EACH PERSON’S SHARE", COL_SHARES, y, label)
        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rule)
        y += LINE
    }

    private fun row(record: ExportRecord) {
        val shares =
            record.shares.map { (who, share) -> "$who ${Money.format(share, record.currency)}" }
        val height = LINE * shares.size.coerceAtLeast(1)
        if (ensureSpace(height)) columnLabels()
        canvas.drawText(Dates.short(record.date), COL_DATE, y, body)
        canvas.drawText(fit(record.title, WIDTH_TITLE, body), COL_TITLE, y, body)
        canvas.drawText(fit(record.paidBy, WIDTH_PAID_BY, body), COL_PAID_BY, y, body)
        drawRight(Money.format(record.amount, record.currency), COL_AMOUNT_END, body)
        shares.forEachIndexed { index, text ->
            canvas.drawText(fit(text, WIDTH_SHARES, body), COL_SHARES, y + index * LINE, body)
        }
        y += height + 4f
    }

    /** Starts a new page when [height] doesn't fit on this one; true if it did. */
    private fun ensureSpace(height: Float): Boolean {
        if (y + height <= FOOTER_Y - LINE * 2) return false
        finishPage()
        newPage()
        return true
    }

    private fun newPage() {
        val number = document.pages.size + 1
        page =
            document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create())
        y = MARGIN
    }

    private fun finishPage() {
        page?.let {
            it.canvas.drawText("Paybak never moves money.", MARGIN, FOOTER_Y, subtitle)
            document.finishPage(it)
        }
        page = null
    }

    private val canvas: Canvas
        get() = checkNotNull(page).canvas

    private fun drawRight(text: String, end: Float, paint: Paint) =
        canvas.drawText(text, end - paint.measureText(text), y, paint)

    /** [text] cut with an ellipsis to fit [width]. */
    private fun fit(text: String, width: Float, paint: Paint): String {
        if (paint.measureText(text) <= width) return text
        val count = paint.breakText(text, true, width - paint.measureText("…"), null)
        return text.take(count).trimEnd() + "…"
    }

    private fun font(context: Context, id: Int): Typeface =
        ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT

    private fun paint(typeface: Typeface, size: Float, color: Int) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
        }
}
