import SwiftUI

/// The 65 Figma icons (`Icon / …`, HugeIcons stroke-rounded), in Figma order. The raw value is the
/// asset name, which is the Figma file name (`Icon / Chevron Left` → "chevron-left").
///
/// The assets are 24 × 24 template images with 1.5 pt strokes, so they take the foreground style.
/// Brand exceptions: `.google` and `.whatsapp` keep their official colours; `.apple` is tinted black
/// or white only.
enum PBIcon: String, CaseIterable, Identifiable {
    case home = "home"
    case groups = "groups"
    case plus = "plus"
    case activity = "activity"
    case profile = "profile"
    case bell = "bell"
    case chevronRight = "chevron-right"
    case chevronLeft = "chevron-left"
    case settings = "settings"
    case mail = "mail"
    case receipt = "receipt"
    case food = "food"
    case bolt = "bolt"
    case wallet = "wallet"
    case moneyIn = "money-in"
    case moneyOut = "money-out"
    case exchange = "exchange"
    case lend = "lend"
    case calendar = "calendar"
    case check = "check"
    case checkCircle = "check-circle"
    case close = "close"
    case alert = "alert"
    case userAdd = "user-add"
    case people = "people"
    case apple = "apple"
    case google = "google"
    case search = "search"
    case camera = "camera"
    case copy = "copy"
    case car = "car"
    case bed = "bed"
    case ticket = "ticket"
    case shoppingBag = "shopping-bag"
    case tag = "tag"
    case split = "split"
    case note = "note"
    case arrowRight = "arrow-right"
    case arrowUp = "arrow-up"
    case sparkles = "sparkles"
    case plane = "plane"
    case drone = "drone"
    case package = "package"
    case qrCode = "qr-code"
    case scan = "scan"
    case link = "link"
    case share = "share"
    case logout = "logout"
    case `repeat` = "repeat"
    case flag = "flag"
    case delete = "delete"
    case restore = "restore"
    case chart = "chart"
    case mic = "mic"
    case image = "image"
    case flame = "flame"
    case wiFi = "wi-fi"
    case crown = "crown"
    case bank = "bank"
    case download = "download"
    case star = "star"
    case whatsapp = "whatsapp"
    case shuffle = "shuffle"
    case lock = "lock"
    case help = "help"

    var id: String { rawValue }
}

/// Draws an icon at `size` by scaling the whole 24 × 24 artwork, so the stroke scales with it exactly
/// like Figma (24 → 1.5, 20 → 1.25, 16 → 1.0, 14 → 0.875). Tint it with `.foregroundStyle`.
/// Icons are decorative; the control that contains one carries the accessibility label.
struct PBIconView: View {
    let icon: PBIcon
    var size: CGFloat = PBSize.iconLg

    init(_ icon: PBIcon, size: CGFloat = PBSize.iconLg) {
        self.icon = icon
        self.size = size
    }

    var body: some View {
        Image(icon.rawValue)
            .resizable()
            .frame(width: size, height: size)
            .accessibilityHidden(true)
    }
}

/// The Open Peeps heads (`Art / Peep Head / …`), 120 × 120 transparent art. Draw them scaled to the
/// circle and clipped; the circle's fill comes from the container.
enum PBPeepHead: String, CaseIterable, Identifiable {
    case arjun = "avatar-1"
    case priya = "avatar-2"
    case rohan = "avatar-3"
    case esha = "avatar-4"
    case dev = "avatar-5"
    case kabir = "avatar-6"
    case meera = "avatar-7"

    /// The five Setup 1 options. The profile stores the index into this list.
    static let presets: [PBPeepHead] = [.arjun, .priya, .rohan, .esha, .dev]

    var id: String { rawValue }

    var image: Image { Image(rawValue) }

    /// The Figma component name, used as the accessibility label of picker options.
    var name: String {
        switch self {
        case .arjun: "Arjun"
        case .priya: "Priya"
        case .rohan: "Rohan"
        case .esha: "Esha"
        case .dev: "Dev"
        case .kabir: "Kabir"
        case .meera: "Meera"
        }
    }
}

#Preview("Icons") {
    LazyVGrid(columns: Array(repeating: GridItem(.fixed(PBSize.tap)), count: 7)) {
        ForEach(PBIcon.allCases) { icon in
            PBIconView(icon)
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: PBSize.tap, height: PBSize.tap)
        }
    }
}
