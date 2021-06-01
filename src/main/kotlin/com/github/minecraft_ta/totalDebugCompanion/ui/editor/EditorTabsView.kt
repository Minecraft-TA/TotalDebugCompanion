package com.github.minecraft_ta.totalDebugCompanion.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.minecraft_ta.totalDebugCompanion.model.AbstractEditor
import com.github.minecraft_ta.totalDebugCompanion.model.CodeEditor
import com.github.minecraft_ta.totalDebugCompanion.model.Editors
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.ui.components.ScrollableTabRow

/*@Composable
fun EditorTabsView(model: Editors) = Row(Modifier.horizontalScroll(rememberScrollState())) {
    for (editor in model.editors) {
        EditorTabView(editor)
    }
}*/

@Composable
fun EditorTabsView(model: Editors) {
    ScrollableTabRow(
        selectedTabIndex = model.editors.indexOf(model.active), edgePadding = 0.dp,
        tabs = {
            for (editor in model.editors) {
                EditorTabView(editor)
            }
        },
    )
}

@Composable
fun EditorTabView(model: AbstractEditor) = Surface(
    color = if (model.isActive) {
        AppTheme.colors.backgroundDark
    } else {
        Color.Transparent
    }
) {
    Row(
        Modifier.clickable(remember(::MutableInteractionSource), indication = null) {
            model.activate()
        }.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            model.getTitle(),
            color = LocalContentColor.current,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        val close = model.close

        if (close != null) {
            Icon(
                Icons.Default.Close,
                tint = LocalContentColor.current,
                contentDescription = "Close",
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
                    .clickable {
                        close()
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp, 24.dp)
                    .padding(4.dp)
            )
        }
    }
}
