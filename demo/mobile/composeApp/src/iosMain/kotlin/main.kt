import androidx.compose.ui.window.ComposeUIViewController
import com.umain.transport.app.App
import platform.UIKit.UIViewController

import com.umain.transport.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}