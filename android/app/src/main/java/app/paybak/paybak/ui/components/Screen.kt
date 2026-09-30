package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace

/** The least space between a screen's content and its footer (Figma's `Spacer(minLength: 24)`). */
private val MinFooterGap = PbSpace.S24

/**
 * A white full-screen page laid out like the Figma frames: [content] from the top of the safe area
 * and [footer] at the bottom (see [PbScreenBody]), with the 20 dp screen margins, centred at
 * [PbLayout.MaxContentWidth] on wide screens. The root is tagged `screen.<id>` for UI tests.
 *
 * @param id The flow.md screen id.
 * @param modifier Applied to the whole screen, e.g. for gestures anywhere on it.
 * @param resizeForKeyboard See [PbScreenFrame].
 */
@Composable
fun PbScreen(
    id: String,
    modifier: Modifier = Modifier,
    resizeForKeyboard: Boolean = true,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    PbScreenFrame(id = id, modifier = modifier, resizeForKeyboard = resizeForKeyboard) {
        PbScreenBody(
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            footer = footer,
            content = content,
        )
    }
}

/**
 * The root of every screen: white, tagged `screen.<id>`, with a column in the safe area centred at
 * [PbLayout.MaxContentWidth] on wide screens. It has no side margins: [PbScreen] adds them, while
 * screens that slide part of their content edge to edge (the setup steps) build on this directly.
 *
 * @param resizeForKeyboard True: the column ends at the keyboard's top, so a footer rides it.
 *   False: the keyboard covers the bottom of the screen, and a footer that should ride it pads
 *   itself with [keyboardWithGap].
 */
@Composable
fun PbScreenFrame(
    id: String,
    modifier: Modifier = Modifier,
    resizeForKeyboard: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets =
        if (resizeForKeyboard) {
            WindowInsets.safeDrawing
        } else {
            WindowInsets.systemBars.union(WindowInsets.displayCutout)
        }
    Box(
        modifier = modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.$id"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier.windowInsetsPadding(insets)
                    .widthIn(max = PbLayout.MaxContentWidth)
                    .fillMaxSize(),
            content = content,
        )
    }
}

/**
 * [content] from the top and [footer] at the bottom, at least 24 dp apart, filling the space it's
 * given. When that is short, the content gives way first: a child weighted with `fill = false`,
 * such as an illustration, gets only the height left over. If the content still doesn't fit, it
 * scrolls, and the footer's actions stay on screen.
 */
@Composable
fun PbScreenBody(
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        val scroll = rememberScrollState()
        // At least the height above the footer, which the content's weighted children share.
        // Scrolling is off while everything fits, so there is no overscroll stretch.
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scroll, enabled = scroll.maxValue > 0),
            content = content,
        )
        Spacer(Modifier.height(MinFooterGap))
        footer()
    }
}

/**
 * The keyboard plus the 12 dp gap Figma keeps between it and a bottom CTA (Sign in, Setup 1). Pad
 * the CTA with it (`Modifier.windowInsetsPadding(WindowInsets.keyboardWithGap)`): the CTA then sits
 * 12 dp above the keyboard while it's up and on the safe area's bottom edge when it's down,
 * following the keyboard as it slides.
 */
val WindowInsets.Companion.keyboardWithGap: WindowInsets
    @Composable get() = ime.add(WindowInsets(bottom = PbSpace.S12))
