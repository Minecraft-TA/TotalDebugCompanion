package com.github.minecraft_ta.totalDebugCompanion.ui.editor

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.Keyboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.minecraft_ta.totalDebugCompanion.model.CodeEditor
import com.github.minecraft_ta.totalDebugCompanion.model.Settings
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.util.Fonts
import com.github.minecraft_ta.totalDebugCompanion.util.getStylesForJavaCode
import com.github.minecraft_ta.totalDebugCompanion.util.loadableScoped
import kotlinx.coroutines.runBlocking
import java.awt.event.KeyEvent
import java.util.stream.Collectors
import java.util.stream.IntStream

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorView(model: CodeEditor, settings: Settings) = key(model) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState(initialFirstVisibleItemIndex = model.initialScrollPosition)

    val lines by loadableScoped(model.lines)

    Box {
        with(LocalDensity.current) {
            SelectionContainer {
                Surface(
                    Modifier.fillMaxSize().horizontalScroll(horizontalScrollState),
                    color = AppTheme.colors.backgroundDark,
                ) {
                    if (lines != null) {
                        Lines(model, lines!!, verticalScrollState, settings)
                        println(model.initialScrollPosition)
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (lines != null) {
                VerticalScrollbar(
                    rememberScrollbarAdapter(verticalScrollState, lines!!.size, settings.lineHeight(this)),
                    Modifier.align(Alignment.CenterEnd).width(settings.scrollbarWidth),
                )
            }

            HorizontalScrollbar(
                rememberScrollbarAdapter(horizontalScrollState),
                modifier = Modifier.fillMaxWidth().height(settings.scrollbarWidth).align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Lines(model: CodeEditor, lines: CodeEditor.Lines, verticalScrollState: LazyListState, settings: Settings) =
    with(LocalDensity.current) {
        val maxNum = remember(lines.lineNumberDigitCount) {
            (1..lines.lineNumberDigitCount).joinToString(separator = "") { "9" }
        }

        var longestLineIndex = 0

        val code = IntStream.range(0, lines.size)
            .mapToObj {
                val line = lines[it].content.value.value
                if (lines[longestLineIndex].content.value.value.length < line.length)
                    longestLineIndex = it
                line
            }.collect(Collectors.joining("\n"))
        val styles = getStylesForJavaCode(code)

        Box(Modifier.fillMaxSize()) {
            val lineHeight = settings.fontSize.toDp() * 1.6f
            Column(
                Modifier.fillMaxSize()
                    .scrollable(verticalScrollState, orientation = Orientation.Vertical, reverseDirection = true)
            ) {
                if (lines.size > 0) {
                    Box(Modifier.height(0.dp)) {
                        Line(
                            Modifier.align(Alignment.CenterStart),
                            maxNum,
                            lines[longestLineIndex],
                            listOf(),
                            settings
                        ) {}
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = verticalScrollState,
                ) {
                    items(lines.size) { index ->
                        Box(Modifier.height(lineHeight)) {
                            Line(
                                Modifier.align(Alignment.CenterStart),
                                maxNum,
                                lines[index],
                                styles[index] ?: listOf(),
                                settings
                            ) {
                                if (!GlobalKeyboardState.isPressed(KeyEvent.VK_CONTROL) || lines[index].content.value.value[it] == ' ')
                                    return@Line

                                val outStream = TotalDebugServer.currentOutputStream ?: return@Line

                                synchronized(outStream) {
                                    outStream.write(2)
                                    outStream.writeUTF(model.fileName)
                                    outStream.writeInt(index) // row
                                    outStream.writeInt(it) // column
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
private fun Line(
    modifier: Modifier,
    maxNum: String,
    line: CodeEditor.Line,
    styles: List<Triple<SpanStyle, Int, Int>>,
    settings: Settings,
    onClick: (Int) -> Unit
) {
    Row(modifier = modifier) {
        DisableSelection {
            Box {
                LineNumber(maxNum, Modifier.alpha(0f), settings)
                LineNumber(line.number.toString(), Modifier.align(Alignment.CenterEnd), settings)
            }
        }

        //using the modifier parameter is a hack to get alignment CenterStart
        LineContent(
            line.content,
            modifier = modifier
                .weight(1f)
                .padding(start = 18.dp, end = 12.dp),
            styles,
            settings = settings,
            onClick = onClick
        )
    }
}

@Composable
private fun LineNumber(number: String, modifier: Modifier, settings: Settings) = Text(
    text = number,
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    color = LocalContentColor.current.copy(alpha = 0.30f),
    modifier = modifier.padding(start = 12.dp)
)

@Composable
private fun LineContent(
    content: CodeEditor.Content,
    modifier: Modifier,
    styles: List<Triple<SpanStyle, Int, Int>>,
    settings: Settings,
    onClick: (Int) -> Unit
) = ClickableText(
    text = if (content.isCode) {
        codeString(content.value.value, styles)
    } else {
        buildAnnotatedString {
            withStyle(AppTheme.code.simple) {
                append(content.value.value)
            }
        }
    },
    style = TextStyle(fontSize = settings.fontSize, fontFamily = Fonts.jetbrainsMono()),
    modifier = modifier,
    softWrap = false,
    onClick = onClick
)

private fun codeString(str: String, styles: List<Triple<SpanStyle, Int, Int>>) = buildAnnotatedString {
    withStyle(AppTheme.code.simple) {
        val strFormatted = str.replace("\t", "    ")
        append(strFormatted)

        styles.forEach {
            addStyle(it.first, it.second, it.third)
        }
    }
}
