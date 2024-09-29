import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import screens.InputScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(screen = InputScreen)
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Json Fun",
        state = rememberWindowState(
            placement = WindowPlacement.Maximized
        )
    ) {
        App() // Call your main application function here
    }
}
