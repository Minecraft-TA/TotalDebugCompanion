package com.github.minecraft_ta.totalDebugCompanion.ui.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.minecraft_ta.totalDebugCompanion.model.Editor
import com.github.minecraft_ta.totalDebugCompanion.model.Settings
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import com.github.minecraft_ta.totalDebugCompanion.util.Fonts
import com.github.minecraft_ta.totalDebugCompanion.util.getStylesForJavaCode
import com.github.minecraft_ta.totalDebugCompanion.util.loadableScoped
import com.github.minecraft_ta.totalDebugCompanion.util.withoutWidthConstraints
import java.util.stream.Collectors
import java.util.stream.IntStream

@Composable
fun EditorView(model: Editor, settings: Settings) = key(model) {
    with(LocalDensity.current) {
        SelectionContainer {
            Surface(
                Modifier.fillMaxSize(),
                color = AppTheme.colors.backgroundDark,
            ) {
                val lines by loadableScoped(model.lines)

                if (lines != null) {
                    Lines(lines!!, settings)
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Lines(lines: Editor.Lines, settings: Settings) = with(LocalDensity.current) {
    val maxNum = remember(lines.lineNumberDigitCount) {
        (1..lines.lineNumberDigitCount).joinToString(separator = "") { "9" }
    }

    val code = IntStream.range(0, lines.size)
        .mapToObj { lines[it].content.value.value }.collect(Collectors.joining("\n"))
    val styles = getStylesForJavaCode(code)

    val horizontalScrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        val verticalScrollState = rememberLazyListState()
        val lineHeight = settings.fontSize.toDp() * 1.6f

        LazyRow {
            items(1) {
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
                            )
                        }
                    }
                }
            }
        }

        HorizontalScrollbar(
            rememberScrollbarAdapter(horizontalScrollState),
            Modifier.align(Alignment.BottomCenter)
        )

        VerticalScrollbar(
            rememberScrollbarAdapter(verticalScrollState, lines.size, lineHeight),
            Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun Line(
    modifier: Modifier,
    maxNum: String,
    line: Editor.Line,
    styles: List<Triple<SpanStyle, Int, Int>>,
    settings: Settings
) {
    Row(modifier = modifier) {
        DisableSelection {
            Box {
                LineNumber(maxNum, Modifier.alpha(0f), settings)
                LineNumber(line.number.toString(), Modifier.align(Alignment.CenterEnd), settings)
            }
        }
        LineContent(
            line.content,
            modifier = Modifier
                .weight(1f)
                .withoutWidthConstraints()
                .padding(start = 28.dp, end = 12.dp),
            styles,
            settings = settings
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
    content: Editor.Content,
    modifier: Modifier,
    styles: List<Triple<SpanStyle, Int, Int>>,
    settings: Settings
) = Text(
    text = if (content.isCode) {
        codeString(content.value.value, styles)
    } else {
        buildAnnotatedString {
            withStyle(AppTheme.code.simple) {
                append(content.value.value)
            }
        }
    },
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    modifier = modifier,
    softWrap = false
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
