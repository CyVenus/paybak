#if DEBUG
import Foundation

/// Lane C's Insights section of the debug menu (app-architecture §3.10): the receipt camera's
/// simulated feed on a device (the simulator always uses it).
enum InsightsDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        let isOn = UserDefaults.standard.bool(forKey: CameraSource.simulatedFeedKey)
        return [
            DebugAction(title: "Simulated receipt camera: \(isOn ? "On" : "Off")",
                        detail: "Scan receipt shows the Leopold Cafe receipt instead of the camera") { _ in
                UserDefaults.standard.set(!isOn, forKey: CameraSource.simulatedFeedKey)
            },
        ]
    }
}
#endif
