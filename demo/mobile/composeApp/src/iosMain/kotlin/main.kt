import androidx.compose.ui.window.ComposeUIViewController
import com.umain.transport.app.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
