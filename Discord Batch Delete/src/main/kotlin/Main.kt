import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.smushytaco.discord_batch_delete.generated.resources.Res
import com.smushytaco.discord_batch_delete.generated.resources.icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.awt.Robot
import java.awt.event.KeyEvent
import java.lang.foreign.*
import java.nio.charset.StandardCharsets

const val MAX_TITLE_LENGTH = 1024

private object User32FFM {
    private val linker = Linker.nativeLinker()

    private val lookup: SymbolLookup = SymbolLookup.libraryLookup("user32", Arena.global())

    private val HWND = ValueLayout.ADDRESS

    private val INT = ValueLayout.JAVA_INT

    private val getForegroundWindow = linker.downcallHandle(
        lookup.find("GetForegroundWindow").orElseThrow(),
        FunctionDescriptor.of(HWND)
    )

    private val getWindowTextW = linker.downcallHandle(
        lookup.find("GetWindowTextW").orElseThrow(),
        FunctionDescriptor.of(INT, HWND, ValueLayout.ADDRESS, INT)
    )

    fun getForegroundWindow(): MemorySegment {
        return getForegroundWindow.invokeExact() as MemorySegment
    }

    fun getWindowTextW(hwnd: MemorySegment, buffer: MemorySegment, maxCount: Int): Int {
        return getWindowTextW.invokeExact(hwnd, buffer, maxCount) as Int
    }
}

fun getActiveWindowTitle(): String {
    Arena.ofConfined().use { arena ->
        val hwnd = User32FFM.getForegroundWindow()

        val bufBytes = (MAX_TITLE_LENGTH + 1) * 2
        val buf = arena.allocate(bufBytes.toLong(), 2)

        User32FFM.getWindowTextW(hwnd, buf, MAX_TITLE_LENGTH)

        val bytes = buf.asByteBuffer()
        var end = 0
        while (end + 1 < bytes.limit()) {
            if (bytes[end] == 0.toByte() && bytes[end + 1] == 0.toByte()) break
            end += 2
        }
        val arr = ByteArray(end)
        bytes.position(0)
        bytes[arr]
        return String(arr, StandardCharsets.UTF_16LE)
    }
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
fun labelledCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onCheckedChange(!checked) }
            )
            .requiredHeight(ButtonDefaults.MinHeight)
            .padding(4.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.size(6.dp))
        Text(text = label)
    }
}

private val darkColors = darkColorScheme()
private val lightColors = lightColorScheme()

@Composable
@Preview
fun app() {
    val systemDarkModeCache = isSystemInDarkTheme()
    val darkMode = remember { mutableStateOf(systemDarkModeCache) }
    val batchDeletingForUI = remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkMode.value) darkColors else lightColors
    ) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                labelledCheckBox(darkMode.value, { darkMode.value = it }, "Dark Mode")
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        batchDeleting = !batchDeleting
                        batchDeletingForUI.value = !batchDeletingForUI.value
                        if (batchDeleting && !isCurrentlyDeletingMessages) {
                            isCurrentlyDeletingMessages = true
                            CoroutineScope(Dispatchers.Default).launch { deleteMessages() }
                        }
                    }
                ) {
                    Text("${if (batchDeletingForUI.value) "Stop" else "Start"} Batch Deleting")
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Discord Batch Delete",
        icon = painterResource(Res.drawable.icon)
    ) {
        app()
    }
}
