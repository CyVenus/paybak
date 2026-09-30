package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import app.paybak.paybak.data.Currencies
import app.paybak.paybak.ui.components.CodeLength
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarOption
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCodeDigit
import app.paybak.paybak.ui.components.PbCodeDigitState
import app.paybak.paybak.ui.components.PbCodeField
import app.paybak.paybak.ui.components.PbCurrencyRow
import app.paybak.paybak.ui.components.PbOnboardingTopBar
import app.paybak.paybak.ui.components.PbPaymentPreview
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSetupHeader
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.PbToast
import app.paybak.paybak.ui.components.PbToastHost
import app.paybak.paybak.ui.components.rememberPbToastState
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

private const val EMAIL_HELPER = "We’ll send a 6-digit code."

@Composable
internal fun InputsPage() {
    GalleryPage {
        GallerySection("Control / Input Field") { TextFieldStates() }
        GallerySection("Control / Code Digit: Empty · Focused · Filled · Error") {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                PbCodeDigit(null, PbCodeDigitState.Empty)
                PbCodeDigit(null, PbCodeDigitState.Focused)
                PbCodeDigit('4', PbCodeDigitState.Filled)
                PbCodeDigit('4', PbCodeDigitState.Error)
            }
        }
        GallerySection("Control / Code Input: Typing · Error · live") { CodeInputs() }
        GallerySection("Navigation / Onboarding Top Bar") {
            PbOnboardingTopBar(showSkip = true)
            PbOnboardingTopBar(showBack = true)
            PbOnboardingTopBar(showBack = true, showSkip = true)
        }
        GallerySection("Navigation / Setup Header") { SetupHeaders() }
        GallerySection("Control / Avatar Option (tap to select)") { AvatarOptions() }
        GallerySection("Row / Currency (tap to select)") { CurrencyRows() }
        GallerySection("Card / Payment Preview · Overlay / Toast") { PaymentPreviews() }
    }
}

@Composable
private fun TextFieldStates() {
    GalleryLabel("Default")
    PbTextField(
        value = "",
        onValueChange = {},
        label = "Email",
        placeholder = "you@example.com",
        helper = EMAIL_HELPER,
    )
    GalleryLabel("Focused")
    PbTextField(
        value = "you@example.com",
        onValueChange = {},
        label = "Email",
        helper = EMAIL_HELPER,
        interactionSource = rememberFocusedSource(),
    )
    GalleryLabel("Filled")
    PbTextField(
        value = "you@example.com",
        onValueChange = {},
        label = "Email",
        helper = EMAIL_HELPER,
    )
    GalleryLabel("Error")
    PbTextField(
        value = "you@example.com",
        onValueChange = {},
        label = "Email",
        helper = EMAIL_HELPER,
        isError = true,
    )
    GalleryLabel("Disabled")
    PbTextField(
        value = "",
        onValueChange = {},
        label = "Email",
        placeholder = "you@example.com",
        helper = EMAIL_HELPER,
        enabled = false,
    )
    GalleryLabel("Leading icon, no label or helper")
    PbTextField(
        value = "",
        onValueChange = {},
        placeholder = "Search currencies",
        leadingIcon = PbIcon.Search,
    )
    GalleryLabel("Live (type here)")
    var text by remember { mutableStateOf("") }
    PbTextField(
        value = text,
        onValueChange = { text = it },
        label = "Email or phone",
        placeholder = "you@example.com",
        helper = EMAIL_HELPER,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
}

@Composable
private fun CodeInputs() {
    PbCodeField(code = "4829", onCodeChange = {}, interactionSource = rememberFocusedSource())
    PbCodeField(code = "482917", onCodeChange = {}, isError = true)
    var code by remember { mutableStateOf("") }
    PbCodeField(
        code = code,
        onCodeChange = { code = it },
        isError = code.length == CodeLength && code != "000000",
    )
    GalleryLabel("Live: 000000 is correct; any other six digits show the error.")
}

@Composable
private fun SetupHeaders() {
    (1..4).forEach { step -> PbSetupHeader(step = step, onBack = {}, showSkip = step >= 3) }
    var step by remember { mutableIntStateOf(1) }
    GalleryLabel("Live: the next segment fills, the number slides, Skip fades")
    PbSetupHeader(
        step = step,
        onBack = { step = (step - 1).coerceAtLeast(1) },
        showSkip = step >= 3,
    )
    PbButton(
        "Next step",
        onClick = { step = step % 4 + 1 },
        style = PbButtonStyle.Secondary,
        size = PbButtonSize.Small,
    )
}

@Composable
private fun AvatarOptions() {
    var selected by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbPeepHead.Presets.forEachIndexed { index, head ->
            PbAvatarOption(
                content = PbAvatarContent.Art(head),
                selected = selected == index,
                onClick = { selected = index },
                contentDescription = head.name,
            )
        }
        PbAvatarOption(
            content = PbAvatarContent.Symbol(PbIcon.Camera),
            selected = false,
            onClick = {},
            contentDescription = "Choose a photo",
        )
    }
}

@Composable
private fun PaymentPreviews() {
    val toast = rememberPbToastState()
    PbPaymentPreview(
        name = "Arjun Mehta",
        upiId = "arjun@okaxis",
        avatar = PbAvatarContent.Art(PbPeepHead.Arjun),
        onCopy = { toast.show("UPI ID copied") },
    )
    PbPaymentPreview(
        name = "Arjun Mehta",
        upiId = "",
        avatar = PbAvatarContent.Initials("AM"),
        onCopy = {},
        placeholder = "yourname@bank",
    )
    GalleryLabel("Copy shows the toast for 2 s")
    Box(Modifier.fillMaxWidth().height(PbSize.Tap), contentAlignment = Alignment.Center) {
        PbToastHost(toast)
    }
    PbToast("UPI ID copied")
}

@Composable
private fun CurrencyRows() {
    val suggested = remember { Currencies.suggested() }
    val popular = remember { Currencies.popular(excludingCode = suggested.currency.code) }
    val available = remember { Currencies.all().size }
    var selected by remember { mutableStateOf(suggested.currency.code) }
    Column {
        PbSectionHeader("Suggested")
        val region = if (suggested.fromRegion) " · Based on your region" else ""
        PbCurrencyRow(
            symbol = suggested.currency.tileSymbol,
            title = suggested.currency.name,
            subtitle = suggested.currency.code + region,
            selected = selected == suggested.currency.code,
            onClick = { selected = suggested.currency.code },
        )
        PbSectionHeader("Popular")
        popular.forEach { currency ->
            PbCurrencyRow(
                symbol = currency.tileSymbol,
                title = currency.name,
                subtitle = currency.code,
                selected = selected == currency.code,
                onClick = { selected = currency.code },
            )
        }
        GalleryLabel("$available ISO currencies in use today")
    }
}
