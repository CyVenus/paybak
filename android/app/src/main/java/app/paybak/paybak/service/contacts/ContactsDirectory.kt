package app.paybak.paybak.service.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.core.content.ContextCompat
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * The phone's contacts and the simulated Paybak directory (app-architecture §4). Real contacts are
 * read once READ_CONTACTS is granted (Settings › Privacy asks for it). Debug builds add the demo's
 * simulated directory (`assets/groups/directory.json`: Paybak accounts and address-book entries,
 * so Add friend shows Kabir, Meera and Ananya as Figma draws it); release builds have no such file.
 */
object ContactsDirectory {
    private const val SIMULATED_FILE = "groups/directory.json"

    @Serializable
    private data class Simulated(
        val accounts: List<DirectoryAccount> = emptyList(),
        val contacts: List<DeviceContact> = emptyList(),
    )

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    /** The address book: the simulated entries, then the phone's own contacts. */
    suspend fun contacts(context: Context): List<DeviceContact> =
        withContext(Dispatchers.IO) {
            simulated(context).contacts +
                if (hasPermission(context)) deviceContacts(context) else emptyList()
        }

    /** Who is on Paybak, given your [people] (friends and guests). */
    suspend fun directory(context: Context, people: List<Person>): Directory =
        withContext(Dispatchers.IO) { Directory(people, simulated(context).accounts) }

    private fun simulated(context: Context): Simulated =
        runCatching {
                context.assets.open(SIMULATED_FILE).bufferedReader().use {
                    LedgerJson.decodeFromString(Simulated.serializer(), it.readText())
                }
            }
            .getOrDefault(Simulated())

    private fun deviceContacts(context: Context): List<DeviceContact> {
        val resolver = context.contentResolver
        fun read(uri: android.net.Uri, nameColumn: String, valueColumn: String) = buildList {
            resolver.query(uri, arrayOf(nameColumn, valueColumn), null, null, null)?.use { cursor ->
                val name = cursor.getColumnIndex(nameColumn)
                val value = cursor.getColumnIndex(valueColumn)
                while (cursor.moveToNext()) {
                    val contact = cursor.getString(value) ?: continue
                    add(DeviceContact(cursor.getString(name) ?: contact, contact))
                }
            }
        }
        return read(Phone.CONTENT_URI, Phone.DISPLAY_NAME, Phone.NUMBER) +
            read(Email.CONTENT_URI, Email.DISPLAY_NAME_PRIMARY, Email.ADDRESS)
    }
}
