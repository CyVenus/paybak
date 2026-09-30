package app.paybak.paybak.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * The system share sheet (app-architecture §4): reminder messages, invite and QR links, exports.
 * Files must live in the cache's `exports/` folder (res/xml/file_paths.xml).
 */
object SystemShare {
    const val FILE_AUTHORITY = "app.paybak.paybak.files"

    fun shareText(context: Context, text: String, title: String? = null) {
        val send =
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, title))
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String? = null) {
        val uri = FileProvider.getUriForFile(context, FILE_AUTHORITY, file)
        val send =
            Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, title))
    }

    /** The folder exports are written to before sharing. */
    fun exportsDir(context: Context): File = File(context.cacheDir, "exports").apply { mkdirs() }
}
