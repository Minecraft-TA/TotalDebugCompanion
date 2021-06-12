package com.github.minecraft_ta.totalDebugCompanion.ui.editor

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.minecraft_ta.totalDebugCompanion.model.SearchEditor
import com.github.minecraft_ta.totalDebugCompanion.model.Settings
import com.github.minecraft_ta.totalDebugCompanion.model.TextLines
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.util.Fonts
import org.jetbrains.skija.paragraph.TextBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchEditorView(model: SearchEditor, settings: Settings) = key(model) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()

    Box {
        with(LocalDensity.current) {
            SelectionContainer {
                Surface(
                    Modifier.fillMaxSize().horizontalScroll(horizontalScrollState),
                    color = AppTheme.colors.backgroundDark,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column {
                            DisableSelection {
                                BoxWithConstraints {
                                    print(this.constraints)
                                    Surface(elevation = 4.dp) {
                                        Row(
                                            Modifier.width(3_000.dp).padding(all = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            @Composable
                                            fun UnderlinedText(text: String) {
                                                Text(text, textDecoration = TextDecoration.Underline)
                                            }

                                            UnderlinedText("Query")
                                            Text(": ${model.query}  ")
                                            UnderlinedText("Results found")
                                            Text(": ${model.results.size}  ")
                                            UnderlinedText("Search type")
                                            Text(": ${if (model.methodSearch) "Method" else "Field"}  ")
                                            UnderlinedText("Classes scanned")
                                            Text(": ${model.classesCount}  ")
                                            UnderlinedText("Took")
                                            Text(": ${model.time}ms")
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = verticalScrollState,
                            ) {
                                items(model.results.size) { index ->
                                    SearchResultLine(model, index, settings)
                                }
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                rememberScrollbarAdapter(verticalScrollState, model.results.size, settings.lineHeight(this)),
                Modifier.align(Alignment.CenterEnd).width(settings.scrollbarWidth),
            )

            HorizontalScrollbar(
                rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.fillMaxWidth().height(settings.scrollbarWidth).align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun SearchResultLine(model: SearchEditor, index: Int, settings: Settings) {
    Box(
        Modifier.background(
            color = if (index.rem(2) == 0) AppTheme.colors.backgroundMedium else Color.Transparent
        ).width(3_000.dp).clickable {
            val outStream = TotalDebugServer.currentOutputStream ?: return@clickable

            synchronized(outStream) {
                outStream.write(1)
                outStream.writeUTF(model.results[index].substringBefore('#').replace('/', '.'))
            }
        }
    ) {
        Row(
            Modifier.padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Close,
                modifier = Modifier.clickable {
                    model.results.removeAt(index)
                },
                tint = Color.Red,
                contentDescription = ""
            )
            Text(
                model.results[index],
                fontSize = settings.fontSize,
                fontFamily = Fonts.jetbrainsMono(),
            )
        }
    }
}
