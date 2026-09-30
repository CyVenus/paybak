package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.paybak.paybak.ui.components.PbDivider
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/** The gallery pages, in order; `--ei galleryPage N` opens page N (1-based). */
private enum class GalleryPageId(val title: String) {
    Colours("Colours"),
    Type("Text styles"),
    Layout("Spacing, radius, size, materials"),
    Assets("Icons, brand, avatars"),
    Buttons("Buttons"),
    Controls("Badges, avatars, controls"),
    Inputs("Inputs and setup"),
    Navigation("Headers, alerts, sheets, settings"),
    Forms("Forms and money"),
    Lists("Lists and detail"),
    Charts("Progress and charts"),
    Assistant("Assistant and scan"),
    Rive("Rive"),
}

/**
 * Debug-only gallery of the design system: every token, text style and core component state, and
 * the six Rive illustrations at their Figma slot sizes. Swipe or use the arrows to page.
 */
@Composable
fun GalleryScreen(initialPage: Int) {
    val pages = GalleryPageId.entries
    val pagerState =
        rememberPagerState(initialPage = (initialPage - 1).coerceIn(pages.indices)) { pages.size }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(PbColors.Bg.Primary).safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = PbSpace.S8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbIconButton(
                icon = PbIcon.ChevronLeft,
                contentDescription = "Previous page",
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
            )
            Spacer(Modifier.width(PbSpace.S4))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Gallery ${pagerState.currentPage + 1}/${pages.size}",
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Tertiary,
                )
                Text(
                    text = pages[pagerState.currentPage].title,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                )
            }
            PbIconButton(
                icon = PbIcon.ChevronRight,
                contentDescription = "Next page",
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
        }
        PbDivider()
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            when (pages[index]) {
                GalleryPageId.Colours -> ColoursPage()
                GalleryPageId.Type -> TypePage()
                GalleryPageId.Layout -> LayoutPage()
                GalleryPageId.Assets -> AssetsPage()
                GalleryPageId.Buttons -> ButtonsPage()
                GalleryPageId.Controls -> ControlsPage()
                GalleryPageId.Inputs -> InputsPage()
                GalleryPageId.Navigation -> NavigationPage()
                GalleryPageId.Forms -> FormsPage()
                GalleryPageId.Lists -> ListsPage()
                GalleryPageId.Charts -> ChartsPage()
                GalleryPageId.Assistant -> AssistantPage()
                GalleryPageId.Rive -> RivePage()
            }
        }
    }
}
