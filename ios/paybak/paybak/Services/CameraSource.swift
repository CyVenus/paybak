import UIKit

// STUB (app-architecture §4): Lane C fills this with the AVFoundation camera (and the simulated feed
// on the simulator), keeping these names.
/// Where the receipt camera gets its pictures: the real camera, or the bundled receipt art on the
/// simulator and when the debug toggle is on.
enum CameraSource {
    /// True on the simulator (no useful camera) unless the debug toggle turns it off.
    static var usesSimulatedFeed: Bool {
        #if targetEnvironment(simulator)
        true
        #else
        false
        #endif
    }

    /// The simulated feed's picture (the Leopold Cafe receipt).
    static var simulatedPhoto: UIImage? { UIImage(named: "art-receipt-full") }
}
