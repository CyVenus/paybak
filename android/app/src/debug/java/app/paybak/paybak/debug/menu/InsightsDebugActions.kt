package app.paybak.paybak.debug.menu

import app.paybak.paybak.service.camera.CameraSource

/** The debug menu's Insights & AI section (lane C, app-architecture §3.10). */
internal val InsightsDebugActions: List<DebugAction>
    get() =
        listOf(
            DebugAction(
                "Simulated camera: ${if (CameraSource.isSimulated) "on" else "off"}",
                "Scan receipt shows the Leopold Cafe receipt instead of the camera",
            ) {
                CameraSource.isSimulated = !CameraSource.isSimulated
            }
        )
