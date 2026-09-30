package app.paybak.paybak.data

import android.content.Context
import androidx.core.content.edit
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The saved [UserProfile], kept in SharedPreferences; a custom photo lives in the files directory.
 * [profile] always holds the latest value, and every change is written through.
 */
class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val filesDir = context.filesDir
    private val state = MutableStateFlow(read())

    val profile: StateFlow<UserProfile> = state.asStateFlow()

    fun update(transform: (UserProfile) -> UserProfile) {
        val next = transform(state.value)
        write(next)
        state.value = next
    }

    /** Clears the profile and deletes the saved photo, as if the app was just installed. */
    fun reset() {
        (state.value.avatar as? AvatarChoice.Photo)?.let { photoFile(it).delete() }
        prefs.edit { clear() }
        state.value = UserProfile()
    }

    fun photoFile(photo: AvatarChoice.Photo): File = File(filesDir, photo.fileName)

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
