package app.paybak.paybak.service.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** A person from the phone's contacts: a name and a phone number or email. */
data class DeviceContact(val name: String, val contact: String)

/**
 * The phone's contacts, matched against people on Paybak (simulated directory; app-architecture
 * §4). STUB owned by lane B (M4): it lists nobody yet.
 */
object ContactsDirectory {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun contacts(context: Context): List<DeviceContact> = emptyList()
}
