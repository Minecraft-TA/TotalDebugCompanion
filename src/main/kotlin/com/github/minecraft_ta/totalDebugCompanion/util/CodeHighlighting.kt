package com.github.minecraft_ta.totalDebugCompanion.util

import androidx.compose.ui.text.SpanStyle
import com.github.javaparser.JavaParser
import com.github.javaparser.JavaToken
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.minecraft_ta.totalDebugCompanion.ui.AppTheme
import java.util.stream.StreamSupport

fun getStylesForJavaCode(code: String): Map<Int, List<Triple<SpanStyle, Int, Int>>> {
    val styles = mutableMapOf<Int, MutableList<Triple<SpanStyle, Int, Int>>>()

    val conf = ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8)

    val tokenRange = try {
        JavaParser(conf).parse(code).result.get().tokenRange
    } catch (t: Throwable) {
        t.printStackTrace()
        return mapOf()
    }

    StreamSupport.stream(tokenRange.get().spliterator(), false).forEach {
        val range = it.range.get()

        val style = when (it.category) {
            JavaToken.Category.KEYWORD -> AppTheme.code.keyword
            JavaToken.Category.LITERAL -> AppTheme.code.value
            JavaToken.Category.SEPARATOR, JavaToken.Category.OPERATOR -> AppTheme.code.punctuation
            JavaToken.Category.COMMENT -> AppTheme.code.comment
            else -> AppTheme.code.simple
        }

        if (style != AppTheme.code.simple)
            styles.computeIfAbsent(range.begin.line - 1) { mutableListOf() } += Triple(
                style,
                range.begin.column - 1,
                range.end.column
            )
    }

    return styles
}
