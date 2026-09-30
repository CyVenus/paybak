package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbRadius
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/** `Detent` of `Sheet / Container`. */
enum class PbSheetDetent {
    /** Floating, 8 dp from the sides and bottom, radius 40 all round, as tall as its content. */
    Medium,

    /** Full width from 8 dp below the status bar; the content fills the height and scrolls. */
    Large,
}

/** The optional search field at the top of a sheet ("Search categories"). */
data class PbSheetSearch(
    val value: String,
    val onValueChange: (String) -> Unit,
    val placeholder: String,
)

/**
 * `Sheet / Container` (`PBSheet`) presented over the 40 % scrim: every picker and form sheet. It
 * slides up, and closes with ✕, a tap on the scrim, a swipe down or system back, animating out
 * before [onDismiss] runs. The picker pattern is a [search] field and a [PbCard] of [PbSettingRow]s
 * with Check trailing. Close is tagged "[testTag].close".
 *
 * @param title The Title/3 title; with no title and [showClose] off the header goes, and the
 *   content starts 20 dp from the top (the Not received sheet).
 * @param content Receives `dismiss`, which animates the sheet out and then calls [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PbSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    detent: PbSheetDetent = PbSheetDetent.Medium,
    showClose: Boolean = true,
    search: PbSheetSearch? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val dismiss: () -> Unit = {
        scope
            .launch { sheetState.hide() }
            .invokeOnCompletion { if (!sheetState.isVisible) currentOnDismiss() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        scrimColor = PbColors.Bg.Scrim,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        val placement =
            when (detent) {
                PbSheetDetent.Medium -> Modifier.windowInsetsPadding(WindowInsets.ime).padding(8.dp)
                PbSheetDetent.Large -> Modifier.statusBarsPadding().padding(top = 8.dp)
            }
        PbSheetContainer(
            modifier = modifier.then(placement),
            title = title,
            detent = detent,
            onClose = if (showClose) dismiss else null,
            search = search,
            testTag = testTag,
        ) {
            content(dismiss)
        }
    }
}

/**
 * The white body of `Sheet / Container` on its own: the kit grabber, the header (title and glass
 * ✕), the optional search field and the content. [PbSheet] presents it; the gallery draws it in
 * place.
 */
@Composable
fun PbSheetContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    detent: PbSheetDetent = PbSheetDetent.Medium,
    onClose: (() -> Unit)? = null,
    search: PbSheetSearch? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val large = detent == PbSheetDetent.Large
    val shape =
        if (large) RoundedCornerShape(topStart = PbRadius.Sheet, topEnd = PbRadius.Sheet)
        else PbShapes.Sheet
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (large) Modifier.fillMaxHeight() else Modifier)
                .partTag(testTag)
                .clip(shape)
                .background(PbColors.Bg.Primary)
                .then(if (large) Modifier.navigationBarsPadding() else Modifier)
                .padding(
                    start = PbSpace.S16,
                    end = PbSpace.S16,
                    top = PbSpace.S8,
                    bottom = if (large) PbSpace.S0 else PbSpace.S28,
                ),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally)
                .size(60.dp, 4.dp)
                .background(PbColors.Bg.Indicator, PbShapes.Pill)
        )
        if (title != null || onClose != null) {
            Row(
                Modifier.fillMaxWidth().height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title.orEmpty(),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = PbTextStyles.Title3,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (onClose != null) {
                    PbGlassCloseButton(onClose, Modifier.partTag(testTag, "close"))
                }
            }
        }
        if (search != null) {
            PbTextField(
                value = search.value,
                onValueChange = search.onValueChange,
                placeholder = search.placeholder,
                leadingIcon = PbIcon.Search,
                onClear = { search.onValueChange("") },
                fieldModifier = Modifier.partTag(testTag, "search"),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().then(if (large) Modifier.weight(1f) else Modifier),
            content = content,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF9E9E9E, widthDp = 402)
@Composable
private fun PbSheetContainerPreview() {
    PbSheetContainer(
        Modifier.padding(8.dp),
        title = "Category",
        onClose = {},
        search = PbSheetSearch("", {}, "Search categories"),
    ) {
        PbCard {
            PbSettingRow("Food", icon = PbIcon.Food, trailing = PbSettingTrailing.Check(true))
            PbSettingRow(
                "Travel",
                icon = PbIcon.Car,
                trailing = PbSettingTrailing.Check(false),
                showDivider = false,
            )
        }
    }
}
