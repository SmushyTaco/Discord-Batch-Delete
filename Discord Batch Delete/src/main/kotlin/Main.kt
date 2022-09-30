import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Robot
import java.awt.event.KeyEvent
const val MAX_TITLE_LENGTH = 1024
internal object User32DLL {
    init {
        Native.register("user32")
    }
    @Suppress("FunctionName")
    external fun GetForegroundWindow(): HWND
    @Suppress("FunctionName")
    external fun GetWindowTextW(hWnd: HWND, lpString: CharArray, nMaxCount: Int): Int
}
// Credit: http://blog.ghyze.nl/?p=196
fun getActiveWindowTitle(): String {
    val buffer = CharArray(MAX_TITLE_LENGTH * 2)
    val foregroundWindow: HWND = User32DLL.GetForegroundWindow()
    User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH)
    return Native.toString(buffer)
}
val isActiveTitleADiscordTitle: Boolean
    get() {
        val windowTitle = getActiveWindowTitle()
        return windowTitle.contains("Discord", true) && !windowTitle.equals("Discord Batch Delete", true)
    }
val robot = Robot()
var batchDeleting = false
var isCurrentlyDeletingMessages = false
fun pressKey(key: Int) {
    robot.keyPress(key)
    robot.keyRelease(key)
}
fun pressTwoKeys(keyOne: Int, keyTwo: Int) {
    robot.keyPress(keyOne)
    robot.keyPress(keyTwo)
    robot.keyRelease(keyOne)
    robot.keyRelease(keyTwo)
}
fun deleteMessages() {
    while (batchDeleting) {
        if (!isActiveTitleADiscordTitle) continue
        pressKey(KeyEvent.VK_SPACE)
        pressKey(KeyEvent.VK_BACK_SPACE)
        pressKey(KeyEvent.VK_UP)
        pressTwoKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_A)
        pressKey(KeyEvent.VK_BACK_SPACE)
        pressKey(KeyEvent.VK_ENTER)
        pressKey(KeyEvent.VK_ENTER)
        Thread.sleep(2000)
    }
    isCurrentlyDeletingMessages = false
}
@Composable
fun labelledCheckBox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit), label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.clip(MaterialTheme.shapes.small).clickable(indication = rememberRipple(color = MaterialTheme.colors.primary), interactionSource = remember { MutableInteractionSource() }, onClick = { onCheckedChange(!checked) }).requiredHeight(ButtonDefaults.MinHeight).padding(4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.size(6.dp))
        Text(text = label)
    }
}
private val darkColors = darkColors()
private val lightColors = lightColors()
@Composable
@Preview
fun app() {
    val systemDarkModeCache = isSystemInDarkTheme()
    val darkMode = remember { mutableStateOf(systemDarkModeCache) }
    val batchDeletingForUI = remember { mutableStateOf(false) }
    MaterialTheme(if (darkMode.value) darkColors else lightColors) {
        Surface(color = MaterialTheme.colors.surface) {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                labelledCheckBox(darkMode.value, { darkMode.value = it }, "Dark Mode")
                Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
                    batchDeleting = !batchDeleting
                    batchDeletingForUI.value = !batchDeletingForUI.value
                    if (batchDeleting && !isCurrentlyDeletingMessages) {
                        isCurrentlyDeletingMessages = true
                        CoroutineScope(Dispatchers.Default).launch { deleteMessages() }
                    }
                }) { Text("${if (batchDeletingForUI.value) "Stop" else "Start"} Batch Deleting") }
            }
        }
    }
}
fun main() = application { Window(onCloseRequest = ::exitApplication, title = "Discord Batch Delete") { app() } }