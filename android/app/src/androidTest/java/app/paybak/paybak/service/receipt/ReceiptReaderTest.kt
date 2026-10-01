package app.paybak.paybak.service.receipt

import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.R
import app.paybak.paybak.domain.model.ScanItem
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Real on-device text recognition of the bundled Leopold Cafe receipt (app-architecture §4.1). */
@RunWith(AndroidJUnit4::class)
class ReceiptReaderTest {
    @Test
    fun theBundledReceiptReadsAsDesigned() {
        val context = ApplicationProvider.getApplicationContext<PaybakApplication>()
        val photo = BitmapFactory.decodeResource(context.resources, R.drawable.art_receipt_full)
        val scan = runBlocking { ReceiptReader.read(photo) }!!
        assertEquals("Leopold Cafe", scan.merchant)
        assertEquals(LocalDate.of(2026, 9, 30), scan.date)
        assertEquals("13:15", scan.time)
        assertEquals(
            listOf(
                ScanItem("Chicken biryani", 43_000),
                ScanItem("Paneer tikka", 37_000),
                ScanItem("Fish and chips", 45_000),
                ScanItem("Chocolate brownie", 24_000),
                ScanItem("Masala fries", 24_000),
                ScanItem("Fresh lime soda ×3", 27_000),
            ),
            scan.items,
        )
        assertEquals(200_000L, scan.subtotal)
        assertEquals(listOf("GST 5%" to 10_000L), scan.taxes.map { it.label to it.amount })
        assertEquals(20_000L, scan.tip)
        assertEquals(230_000L, scan.total)
    }
}
