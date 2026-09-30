import SwiftUI

// PLACEHOLDER (app-architecture §2.2): Lane C replaces this file with the real screen and keeps
// this initializer.
/// Camera, Check receipt, Assign items; answers with `.receipt(result)`.
struct ScanReceiptScreen: View {
    let request: ScanRequest


    var body: some View {
        RoutePlaceholder(route: .scanReceipt(request), title: "Scan receipt", owner: .c, spec: "screens-insights-ai §4")
    }
}
