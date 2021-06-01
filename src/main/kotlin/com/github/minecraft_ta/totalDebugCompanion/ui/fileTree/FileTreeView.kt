package com.github.minecraft_ta.totalDebugCompanion.ui.fileTree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.minecraft_ta.totalDebugCompanion.model.FileTree
import com.github.minecraft_ta.totalDebugCompanion.util.withoutWidthConstraints

@Composable
fun FileTreeViewHeader() = Surface {
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Files",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTreeView(model: FileTree) = Surface(
    modifier = Modifier.fillMaxSize()
) {
    with(LocalDensity.current) {
        Box {
            val scrollState = rememberLazyListState()
            val fontSize = 14.sp
            val lineHeight = fontSize.toDp() * 1.5f

            LazyColumn(
                modifier = Modifier.fillMaxSize().withoutWidthConstraints(),
                state = scrollState,
            ) {
                items(model.items) { FileTreeItemView(fontSize, lineHeight, it) }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = ScrollbarAdapter(scrollState)
            )
        }
    }
}

@Composable
private fun FileTreeItemView(fontSize: TextUnit, height: Dp, model: FileTree.Item) = Row(
    modifier = Modifier
        .height(height)
        .clickable { model.open() }
) {
    Row(modifier = Modifier.padding(start = 24.dp * model.level)) {
        val active = remember { mutableStateOf(false) }

        FileItemIcon(Modifier.align(Alignment.CenterVertically), model)
        Text(
            text = model.name,
            color = if (active.value) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clipToBounds()
                .pointerMoveFilter(
                    onEnter = {
                        active.value = true
                        true
                    },
                    onExit = {
                        active.value = false
                        true
                    }
                ),
            softWrap = true,
            fontSize = fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
private fun FileItemIcon(modifier: Modifier, model: FileTree.Item) {
    Box(modifier.size(24.dp).padding(4.dp)) {
        when (val type = model.type) {
            is FileTree.ItemType.Folder -> when {
                !type.canExpand -> Unit
                type.isExpanded -> Icon(
                    Icons.Default.KeyboardArrowDown,
                    tint = LocalContentColor.current,
                    contentDescription = ""
                )
                else -> Icon(
                    Icons.Default.KeyboardArrowRight,
                    tint = LocalContentColor.current,
                    contentDescription = ""
                )
            }
            is FileTree.ItemType.File -> when (type.ext) {
                "java" -> Icon(Icons.Default.Code, tint = Color(0xFF3E86A0), contentDescription = "")
                "xml" -> Icon(Icons.Default.Code, tint = Color(0xFFC19C5F), contentDescription = "")
                "txt" -> Icon(Icons.Default.Description, tint = Color(0xFF87939A), contentDescription = "")
                "md" -> Icon(Icons.Default.Description, tint = Color(0xFF87939A), contentDescription = "")
                "gitignore" -> Icon(Icons.Default.BrokenImage, tint = Color(0xFF87939A), contentDescription = "")
                "gradle" -> Icon(Icons.Default.Build, tint = Color(0xFF87939A), contentDescription = "")
                "kts" -> Icon(Icons.Default.Build, tint = Color(0xFF3E86A0), contentDescription = "")
                "properties" -> Icon(Icons.Default.Settings, tint = Color(0xFF62B543), contentDescription = "")
                "bat" -> Icon(Icons.Default.Launch, tint = Color(0xFF87939A), contentDescription = "")
                else -> Icon(Icons.Default.Menu, tint = Color(0xFF87939A), contentDescription = "")
            }
        }
    }
}
