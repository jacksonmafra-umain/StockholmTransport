import androidx.compose.ui.window.ComposeUIViewController
import com.umain.transport.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
