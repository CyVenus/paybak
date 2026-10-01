//  SwiftUI wrapper around rive-ios 6.28.0 (legacy `RiveViewModel` API + data binding), adapted from
//  the verified design/rive-ios/PaybakRive.swift. Rive types stay in this file, so screens only need
//  SwiftUI (MemberImportVisibility requires `import RiveRuntime` wherever a Rive member is used).
//
//  Everything is main-actor isolated (the target's default). Rive calls the auto-bind and trigger
//  callbacks synchronously on the main thread.
//
//  Note: the current .riv files carry Rive's export watermark, so rive-ios plays a ~2 s black "RIVE"
//  pre-roll on every new artboard instance. The fix is clean re-exports, not code.

import Combine
import RiveRuntime
import SwiftUI
import UIKit
import os

// MARK: - Controller

/// Owns one artboard instance, its state machine and the view-model instance bound to it.
///
/// Use one controller per on-screen view, and keep it in `@StateObject`: its autoclosure creates the
/// controller once per view identity. (An `@Observable` class in `@State` would be re-created on every
/// parent update, and rive-ios leaks an artboard per auto-bound view model, issue #427.)
final class PaybakRiveController: ObservableObject {
    static let reduceMotionProperty = "reduceMotion"

    let asset: PaybakRiveAsset

    /// Goes up by one each time the file's own tap listener fires `asset.tapTrigger`.
    /// Drive haptics from it with `.sensoryFeedback(_:trigger:)`.
    @Published private(set) var tapCount = 0

    /// nil when the file, artboard or state machine could not be loaded; the view then draws nothing.
    fileprivate let riveViewModel: RiveViewModel?

    /// The instance bound to the state machine. Auto-bind replaces it on every reconfiguration
    /// (`restart()`), so values and listeners are stored and re-applied in `didBind`.
    private var boundInstance: RiveDataBindingViewModel.Instance?
    private var numberValues: [String: Float] = [:]
    private var boolValues: [String: Bool] = [:]
    private var triggerHandlers: [String: () -> Void] = [:]
    private var listenerTokens: [(property: RiveDataBindingViewModel.Instance.TriggerProperty, id: UUID)] = []
    private var isSuspended = false

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Rive")
    private static var fileCache: [String: RiveFile] = [:]

    init(_ asset: PaybakRiveAsset, bundle: Bundle = .main) {
        self.asset = asset
        riveViewModel = Self.makeRiveViewModel(for: asset, bundle: bundle)
        // Seeded before the first bind so the first advance already uses the right mode.
        // PaybakRiveView keeps it in sync with the Reduce Motion setting.
        boolValues[Self.reduceMotionProperty] = UIAccessibility.isReduceMotionEnabled
        // Runs the callback synchronously now (artboard and state machine are set), then again on
        // every reset: auto-bind binds the default view-model instance (index 0) each time.
        riveViewModel?.riveModel?.enableAutoBind { [weak self] instance in
            self?.didBind(instance)
        }
    }

    // MARK: Data binding

    /// Sets a number property, e.g. `setNumber("step", to: 2)`.
    func setNumber(_ name: String, to value: Float) {
        numberValues[name] = value
        guard let property = boundInstance?.numberProperty(fromPath: name) else {
            return logMissing("number", name)
        }
        guard property.value != value else { return }
        property.value = value
        kick()
    }

    /// Sets a boolean property, e.g. `setBool("reduceMotion", to: true)`.
    func setBool(_ name: String, to value: Bool) {
        boolValues[name] = value
        guard let property = boundInstance?.booleanProperty(fromPath: name) else {
            return logMissing("boolean", name)
        }
        guard property.value != value else { return }
        property.value = value
        kick()
    }

    /// Fires a view-model trigger from code. Not for taps: the files' own listeners already fire
    /// their tap triggers, so firing them again would double the animation.
    func fire(trigger name: String) {
        guard let property = boundInstance?.triggerProperty(fromPath: name) else {
            return logMissing("trigger", name)
        }
        property.trigger()
        kick()
    }

    /// Runs `action` on the main thread whenever the trigger fires (a Rive listener, the state machine
    /// or `fire(trigger:)`). One handler per trigger; calling it again replaces the handler, so it is
    /// safe from `.onAppear`.
    func observe(trigger name: String, perform action: @escaping () -> Void) {
        let needsListener = triggerHandlers[name] == nil && name != asset.tapTrigger
        triggerHandlers[name] = action
        if needsListener, let boundInstance {
            attachListener(trigger: name, on: boundInstance)
        }
    }

    // MARK: Playback

    /// Stops the display link. A touch on the view resumes it.
    func pause() {
        isSuspended = true
        riveViewModel?.pause()
    }

    func resume() {
        isSuspended = false
        riveViewModel?.play()
    }

    /// Rebuilds the artboard and state machine so the animation starts from its first frame.
    /// Auto-bind binds a new instance and `didBind` re-applies values and listeners.
    /// A new artboard instance also replays the watermark pre-roll.
    func restart() {
        riveViewModel?.reset()
        if !isSuspended {
            riveViewModel?.play()
        }
    }

    // MARK: Private

    /// Called by auto-bind: once during init, then twice per reset (artboard, then state machine).
    private func didBind(_ instance: RiveDataBindingViewModel.Instance) {
        for token in listenerTokens {
            token.property.removeListener(token.id)
        }
        listenerTokens.removeAll()
        boundInstance = instance

        for (name, value) in numberValues {
            instance.numberProperty(fromPath: name)?.value = value
        }
        for (name, value) in boolValues {
            instance.booleanProperty(fromPath: name)?.value = value
        }
        var triggers = Set(triggerHandlers.keys)
        if let tapTrigger = asset.tapTrigger {
            triggers.insert(tapTrigger)
        }
        for name in triggers {
            attachListener(trigger: name, on: instance)
        }
    }

    private func attachListener(trigger name: String, on instance: RiveDataBindingViewModel.Instance) {
        guard let property = instance.triggerProperty(fromPath: name) else {
            return logMissing("trigger", name)
        }
        // Called synchronously on the main thread from RiveView's advance or touch handling.
        let id = property.addListener { [weak self] in
            self?.handleTrigger(name)
        }
        listenerTokens.append((property: property, id: id))
    }

    private func handleTrigger(_ name: String) {
        if name == asset.tapTrigger {
            tapCount += 1
            Self.log.debug("\(self.asset.fileName, privacy: .public): \(name, privacy: .public) fired (tap \(self.tapCount))")
        }
        triggerHandlers[name]?()
    }

    /// The legacy RiveView only advances while its display link runs or on a touch, so a property
    /// change on a stopped view needs `play()` (rive-ios issue #383).
    private func kick() {
        guard !isSuspended else { return }
        riveViewModel?.play()
    }

    private func logMissing(_ kind: String, _ name: String) {
        Self.log.error("\(self.asset.fileName, privacy: .public): no \(kind, privacy: .public) property '\(name, privacy: .public)'")
    }

    private static func makeRiveViewModel(for asset: PaybakRiveAsset, bundle: Bundle) -> RiveViewModel? {
        do {
            let model = RiveModel(riveFile: try loadFile(named: asset.fileName, bundle: bundle))
            // Check the names with the throwing API first: RiveViewModel.init uses `try!`, so a wrong
            // name there would crash instead of throwing.
            try model.setArtboard(asset.artboard)
            try model.setStateMachine(asset.stateMachine)
            return RiveViewModel(
                model,
                stateMachineName: asset.stateMachine,
                fit: .contain,
                alignment: .center,
                autoPlay: true,
                artboardName: asset.artboard
            )
        } catch {
            log.error("Rive load failed for \(asset.fileName, privacy: .public): \(String(describing: error), privacy: .public)")
            assertionFailure("Rive load failed for \(asset.fileName): \(error)")
            return nil
        }
    }

    /// Parses each .riv once; every controller still gets its own artboard and state machine.
    private static func loadFile(named name: String, bundle: Bundle) throws -> RiveFile {
        if let cached = fileCache[name] {
            return cached
        }
        guard let url = bundle.url(forResource: name, withExtension: "riv") else {
            throw PaybakRiveError.missingResource("\(name).riv")
        }
        // None of the Paybak files references CDN or out-of-band assets.
        let file = try RiveFile(data: Data(contentsOf: url), loadCdn: false)
        fileCache[name] = file
        return file
    }
}

nonisolated enum PaybakRiveError: Error {
    case missingResource(String)
}

// MARK: - Views

/// Lays out the asset's Figma slot and draws the artboard centred on it at its native size, so the
/// bleed artboards overflow the slot by 12 pt per side, unclipped. When the space is narrower or
/// shorter than the slot, slot and artboard scale down together, keeping their proportions; they
/// never grow. Below `minScale` the illustration would only be a speck, so it is left out. Keeps
/// `reduceMotion` bound to the OS setting and pauses the animation while off screen. Decorative:
/// hidden from VoiceOver.
struct PaybakRiveView: View {
    @ObservedObject var controller: PaybakRiveController

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// The smallest share of its Figma size an illustration is still drawn at.
    static let minScale: CGFloat = 0.25

    var body: some View {
        let asset = controller.asset
        Color.clear
            .aspectRatio(asset.slotSize, contentMode: .fit)
            .frame(maxWidth: asset.slotSize.width)
            .overlay {
                GeometryReader { slot in
                    let scale = slot.size.width / asset.slotSize.width
                    if scale >= Self.minScale {
                        artboard
                            .frame(width: asset.viewSize.width * scale, height: asset.viewSize.height * scale)
                            .position(x: slot.size.width / 2, y: slot.size.height / 2)
                    }
                }
            }
            // Files with tap listeners get touches; Onboarding lets them through to the Welcome swipe.
            .allowsHitTesting(asset.isInteractive)
            .accessibilityHidden(true)
            .onChange(of: reduceMotion, initial: true) { _, isOn in
                controller.setBool(PaybakRiveController.reduceMotionProperty, to: isOn)
            }
            .onAppear { controller.resume() }
            .onDisappear { controller.pause() }
    }

    @ViewBuilder
    private var artboard: some View {
        if let riveViewModel = controller.riveViewModel {
            RiveViewRepresentable(viewModel: riveViewModel)
        }
    }
}

/// A self-contained illustration that owns its controller and plays a light haptic whenever the
/// file's tap trigger fires (Get Started, Setup 4, All set, Home empty states). For Welcome, keep the
/// controller in the screen and drive `step` with `setNumber("step", to:)`.
struct PaybakRiveIllustration: View {
    @StateObject private var controller: PaybakRiveController

    init(_ asset: PaybakRiveAsset) {
        _controller = StateObject(wrappedValue: PaybakRiveController(asset))
    }

    var body: some View {
        PaybakRiveView(controller: controller)
            .sensoryFeedback(.impact(weight: .light), trigger: controller.tapCount)
    }
}

#Preview("PaybakRiveIllustration") {
    VStack(spacing: PBSpace.s24) {
        PaybakRiveIllustration(.getStarted)
        PaybakRiveIllustration(.homeAllSquare)
    }
    .padding(PBLayout.screenMargin)
}
