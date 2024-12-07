import androidx.compose.ui.window.ComposeUIViewController
import io.github.sample.App
import io.github.tbib.klocation.IOSKLocationServices
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    IOSKLocationServices().requestPermission()
    return ComposeUIViewController { App() }
}
