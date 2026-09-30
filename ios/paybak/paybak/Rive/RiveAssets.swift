import CoreGraphics

/// One config per Paybak .riv file (Resources/Rive, copied flat into the bundle). Names were checked
/// against the files with rive-ios 6.28.0 (rive.md). Every file also contains Rive's own artboards
/// ("Watermark", "NuRiveBrandmark01", "NuRiveWordmark01"), so the main artboard is always loaded by name.
struct PaybakRiveAsset: Identifiable {
    /// Bundle resource name without the .riv extension.
    let fileName: String
    let artboard: String
    /// Every main artboard has one state machine with the artboard's name.
    let stateMachine: String
    /// The artboard size in pt: the size the Rive view is drawn at.
    let viewSize: CGSize
    /// The Figma layout slot. Get Started, Notifications and AllSquare artboards are the slot plus
    /// 12 pt of bleed per side: layout reserves the slot and the view overflows it, centred, unclipped.
    let slotSize: CGSize
    /// The "something was tapped" trigger the file's own listeners fire; nil when the file has no
    /// listeners. Observe only this one, so a tap gives one haptic.
    let tapTrigger: String?

    var id: String { fileName }

    /// Files with tap listeners get touches; Onboarding lets them through to the Welcome swipe.
    var isInteractive: Bool { tapTrigger != nil }
}

extension PaybakRiveAsset {
    /// Welcome 1–3. Drive the `step` number (1…3); the file animates the slide transitions.
    static let onboarding = PaybakRiveAsset(
        fileName: "paybak-onboarding", artboard: "Onboarding", stateMachine: "Onboarding",
        viewSize: CGSize(width: 362, height: 340), slotSize: CGSize(width: 362, height: 340),
        tapTrigger: nil
    )
    /// Get Started card (the artboard draws its own grey card; don't add a native one).
    static let getStarted = PaybakRiveAsset(
        fileName: "paybak-getstarted", artboard: "Get Started", stateMachine: "Get Started",
        viewSize: CGSize(width: 386, height: 284), slotSize: CGSize(width: 362, height: 260),
        tapTrigger: "personTapped"
    )
    /// Setup 4.
    static let notifications = PaybakRiveAsset(
        fileName: "paybak-notifications", artboard: "Notifications", stateMachine: "Notifications",
        viewSize: CGSize(width: 386, height: 324), slotSize: CGSize(width: 362, height: 300),
        tapTrigger: "bellTapped"
    )
    /// All set.
    static let allSet = PaybakRiveAsset(
        fileName: "paybak-allset", artboard: "All Set", stateMachine: "All Set",
        viewSize: CGSize(width: 362, height: 300), slotSize: CGSize(width: 362, height: 300),
        tapTrigger: "personTapped"
    )
    /// Home first-day empty state.
    static let homeFirstDay = PaybakRiveAsset(
        fileName: "paybak-homefirstday", artboard: "First Day", stateMachine: "First Day",
        viewSize: CGSize(width: 240, height: 180), slotSize: CGSize(width: 240, height: 180),
        tapTrigger: "characterTapped"
    )
    /// Home all-settled empty state ("You're all square.").
    static let homeAllSquare = PaybakRiveAsset(
        fileName: "paybak-home-allset", artboard: "AllSquare", stateMachine: "AllSquare",
        viewSize: CGSize(width: 264, height: 204), slotSize: CGSize(width: 240, height: 180),
        tapTrigger: "tapped"
    )
}
