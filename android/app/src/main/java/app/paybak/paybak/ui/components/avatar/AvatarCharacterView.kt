package app.paybak.paybak.ui.components.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.ui.theme.PbColors

/**
 * The Head crop of a custom character inside an avatar circle (screens-profile §1.5). PLACEHOLDER
 * owned by lane C (M8): it draws the [initials] until the part compositor replaces this file. Keep
 * the signature.
 */
@Composable
fun AvatarCharacterHead(
    look: AvatarLook,
    initials: String,
    initialsStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(initials, style = initialsStyle, color = PbColors.Text.Primary, maxLines = 1)
    }
}
