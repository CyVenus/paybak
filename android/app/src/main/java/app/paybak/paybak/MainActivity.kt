package app.paybak.paybak

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.paybak.paybak.debug.DebugLaunch
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.rive.RiveHost
import app.paybak.paybak.ui.theme.PaybakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light theme only: dark system-bar icons over the white screens, whatever the system
        // theme.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)

        val profileStore = (application as PaybakApplication).profileStore
        val start =
            DebugLaunch.startTarget(intent, profileStore, firstLaunch = savedInstanceState == null)
                ?: StartTarget.Flow(Destination.Splash)
        setContent {
            RiveHost {
                PaybakTheme {
                    PaybakApp(profileStore, start)
                }
            }
        }
    }
}
