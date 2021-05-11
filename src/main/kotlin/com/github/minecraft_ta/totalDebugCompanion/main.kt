import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.github.minecraft_ta.totalDebugCompanion.model.Editors
import com.github.minecraft_ta.totalDebugCompanion.model.FileTree
import com.github.minecraft_ta.totalDebugCompanion.model.Settings
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.ui.components.PanelState
import com.github.minecraft_ta.totalDebugCompanion.ui.components.ResizablePanel
import com.github.minecraft_ta.totalDebugCompanion.ui.components.StatusBar
import com.github.minecraft_ta.totalDebugCompanion.ui.components.VerticalSplittable
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorEmptyView
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorTabsView
import com.github.minecraft_ta.totalDebugCompanion.ui.editor.EditorView
import com.github.minecraft_ta.totalDebugCompanion.ui.fileTree.FileTreeView
import com.github.minecraft_ta.totalDebugCompanion.ui.fileTree.FileTreeViewHeader
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty())
        exitProcess(1)

    val editors = Editors()
    val fileTree = FileTree(Paths.get(args[0]), editors)

    val settings = Settings()

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
                                        EditorView(editors.active!!, settings)
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
