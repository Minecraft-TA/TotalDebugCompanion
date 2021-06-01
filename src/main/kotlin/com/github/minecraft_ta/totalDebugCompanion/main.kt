import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.AppManager
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.github.minecraft_ta.totalDebugCompanion.model.*
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.ui.components.PanelState
import com.github.minecraft_ta.totalDebugCompanion.ui.components.ResizablePanel
import com.github.minecraft_ta.totalDebugCompanion.ui.components.StatusBar
import com.github.minecraft_ta.totalDebugCompanion.ui.components.VerticalSplittable
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorEmptyView
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorTabsView
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorView
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.SearchEditorView
import com.github.minecraft_ta.totalDebugCompanion.ui.fileTree.FileTreeView
import com.github.minecraft_ta.totalDebugCompanion.ui.fileTree.FileTreeViewHeader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.IllegalStateException
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.IntStream
import javax.swing.JFrame
import kotlin.streams.toList
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty())
        exitProcess(1)

    val editors = Editors()
    val fileTree = FileTree(Paths.get(args[0]), editors)

    val settings = Settings()

    TotalDebugServer.run(fileTree.root, editors)

    Window(title = "TotalDebugCompanion", size = IntSize(1280, 720)) {
        DisableSelection {
            DesktopMaterialTheme(colors = AppTheme.colors.material) {
                val panelState = remember { PanelState() }

                val animatedSize = if (panelState.splitterState.isResizing) {
                    if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
                } else {
                    animateDpAsState(
                        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
                        SpringSpec(stiffness = StiffnessLow)
                    ).value
                }

                Surface {
                    VerticalSplittable(
                        modifier = Modifier.fillMaxSize(),
                        splitterState = panelState.splitterState,
                        onResize = {
                            panelState.expandedSize = (panelState.expandedSize + it)
                                .coerceIn(panelState.expandedSizeMin, panelState.expandedSizeMax)
                        }
                    ) {
                        ResizablePanel(Modifier.width(animatedSize).fillMaxHeight(), panelState) {
                            Column {
                                FileTreeViewHeader()
                                FileTreeView(fileTree)
                            }
                        }

                        Box {
                            if (editors.active != null) {
                                Column(Modifier.fillMaxSize()) {
                                    EditorTabsView(editors)
                                    Box(Modifier.weight(1f)) {
                                        val activeEditor = editors.active!!
                                        if (activeEditor is CodeEditor)
                                            EditorView(activeEditor, settings)
                                        else if (activeEditor is SearchEditor)
                                            SearchEditorView(activeEditor, settings)
                                        else
                                            throw IllegalStateException()
                                    }
                                    StatusBar(settings)
                                }
                            } else {
                                EditorEmptyView()
                            }
                        }
                    }
                }
            }
        }
    }
}

object TotalDebugServer {
    var currentOutputStream: DataOutputStream? = null

    fun run(root: Path, editors: Editors) {
        Thread {
            val socket = ServerSocket(25570)

            while (true) {
                try {
                    val client = socket.accept()

                    val inputStream = DataInputStream(client.getInputStream())
                    currentOutputStream = DataOutputStream(client.getOutputStream())
                    while (true) {
                        when (inputStream.readUnsignedByte()) {
                            1 -> { //open a file
                                val path = Paths.get(inputStream.readUTF())

                                if (!Files.exists(path) || !path.isSubPathOf(root))
                                    continue

                                val existingEditor = editors.editors
                                    .filterIsInstance<CodeEditor>()
                                    .find { it.fileName == path.fileName.toString() }

                                if (existingEditor != null) {
                                    existingEditor.activate()
                                } else {
                                    editors.openFile(path)
                                }

                                focusWindow()
                            }
                            2 -> { //open reference search results
                                val query = inputStream.readUTF()
                                val resultCount = inputStream.readInt()
                                val results =
                                    IntStream.range(0, resultCount).mapToObj { inputStream.readUTF() }.toList()
                                val methodSearch = inputStream.readBoolean()
                                val classesCount = inputStream.readInt()
                                val time = inputStream.readInt()

                                editors.openSearchEditor(query, results, methodSearch, classesCount, time)

                                focusWindow()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    currentOutputStream = null
                }
            }
        }.start()
    }
}

fun focusWindow() {
    val frame = AppManager.windows[0].window

    val state = frame.extendedState
    frame.extendedState = JFrame.ICONIFIED
    frame.extendedState = state
}

fun Path.isSubPathOf(other: Path): Boolean {
    val it1 = this.normalize().iterator();
    val it2 = other.normalize().iterator();

    while (it2.hasNext()) {
        val part = it2.next();

        if (!it1.hasNext() || !part.equals(it1.next()))
            return false
    }

    return it1.hasNext()
}
