package app.paybak.paybak.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.edit
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * The saved [UserProfile], kept in the `profile` SharedPreferences; a custom photo lives in the
 * `profile` folder of the files directory. Those two are all that backups and device transfers copy
 * (res/xml/backup_rules.xml, res/xml/data_extraction_rules.xml). [profile] always holds the latest
 * value, and every change is written through.
 */
class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val resolver = context.contentResolver
    private val photoDir = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
    private val state = MutableStateFlow(read())

    val profile: StateFlow<UserProfile> = state.asStateFlow()

    /** Saves the profile [transform] returns. A photo the new avatar no longer uses is deleted. */
    fun update(transform: (UserProfile) -> UserProfile) {
        val previous = state.value
        val next = transform(previous)
        write(next)
        state.value = next
        if (next.avatar != previous.avatar) deletePhotos(except = next.avatar)
    }

    /** Clears the profile and deletes its photos, as if the app was just installed. */
    fun reset() {
        deletePhotos(except = AvatarChoice.None)
        prefs.edit { clear() }
        state.value = UserProfile()
    }

    fun photoFile(photo: AvatarChoice.Photo): File = File(photoDir, photo.fileName)

    /**
     * Saves the image at [uri] as a new photo: its centre square as a 512 px JPEG. The avatar only
     * changes when [update] picks the returned photo; a photo picked earlier and never used is
     * deleted. Null when [uri] isn't a readable image.
     */
    suspend fun importPhoto(uri: Uri): AvatarChoice.Photo? =
        withContext(Dispatchers.IO) {
            val bitmap = decodeSquarePhoto(resolver, uri, PHOTO_SIZE_PX) ?: return@withContext null
            deletePhotos(except = state.value.avatar)
            val photo = AvatarChoice.Photo("photo-${System.currentTimeMillis()}.jpg")
            photoFile(photo).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_JPEG_QUALITY, it)
            }
            photo
        }

    /** Reads a saved photo; null if its file is gone. */
    suspend fun loadPhoto(photo: AvatarChoice.Photo): Bitmap? =
        withContext(Dispatchers.IO) { BitmapFactory.decodeFile(photoFile(photo).path) }

    private fun deletePhotos(except: AvatarChoice) {
        val keep = (except as? AvatarChoice.Photo)?.fileName
        photoDir.listFiles()?.filter { it.name != keep }?.forEach(File::delete)
    }

    private fun read() =
        UserProfile(
            name = prefs.getString(KEY_NAME, null).orEmpty(),
            avatar = readAvatar(),
            currencyCode = prefs.getString(KEY_CURRENCY, null),
            upiId = prefs.getString(KEY_UPI, null).orEmpty(),
            notifications =
                prefs.getString(KEY_NOTIFICATIONS, null)?.let { saved ->
                    NotificationsChoice.entries.firstOrNull { it.name == saved }
                } ?: NotificationsChoice.Undecided,
            signInMethod =
                prefs.getString(KEY_SIGN_IN_METHOD, null)?.let { saved ->
                    SignInMethod.entries.firstOrNull { it.name == saved }
                },
            contact = prefs.getString(KEY_CONTACT, null).orEmpty(),
            onboardingComplete = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false),
        )

    private fun readAvatar(): AvatarChoice {
        prefs.getString(KEY_AVATAR_PHOTO, null)?.let {
            return AvatarChoice.Photo(it)
        }
        val preset = prefs.getInt(KEY_AVATAR_PRESET, -1)
        return if (preset >= 0) AvatarChoice.Preset(preset) else AvatarChoice.None
    }

    private fun write(profile: UserProfile) = prefs.edit {
        putString(KEY_NAME, profile.name)
        when (val avatar = profile.avatar) {
            AvatarChoice.None -> remove(KEY_AVATAR_PRESET).remove(KEY_AVATAR_PHOTO)
            is AvatarChoice.Preset ->
                putInt(KEY_AVATAR_PRESET, avatar.index).remove(KEY_AVATAR_PHOTO)
            is AvatarChoice.Photo ->
                putString(KEY_AVATAR_PHOTO, avatar.fileName).remove(KEY_AVATAR_PRESET)
        }
        putString(KEY_CURRENCY, profile.currencyCode)
        putString(KEY_UPI, profile.upiId)
        putString(KEY_NOTIFICATIONS, profile.notifications.name)
        putString(KEY_SIGN_IN_METHOD, profile.signInMethod?.name)
        putString(KEY_CONTACT, profile.contact)
        putBoolean(KEY_ONBOARDING_COMPLETE, profile.onboardingComplete)
    }

    private companion object {
        const val PREFS_NAME = "profile"
        const val PHOTO_DIR = "profile"
        const val PHOTO_SIZE_PX = 512
        const val PHOTO_JPEG_QUALITY = 90
        const val KEY_NAME = "name"
        const val KEY_AVATAR_PRESET = "avatar_preset"
        const val KEY_AVATAR_PHOTO = "avatar_photo"
        const val KEY_CURRENCY = "currency_code"
        const val KEY_UPI = "upi_id"
        const val KEY_NOTIFICATIONS = "notifications"
        const val KEY_SIGN_IN_METHOD = "sign_in_method"
        const val KEY_CONTACT = "contact"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
