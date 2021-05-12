package com.github.minecraft_ta.totalDebugCompanion.model

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name

class SingleSelection {
    var selected: Any? by mutableStateOf(null)
}

class Editors {
    private val selection = SingleSelection()

    var editors = mutableStateListOf<Editor>()
        private set

    val active: Editor? get() = selection.selected as Editor?

    @OptIn(ExperimentalPathApi::class)
    fun open(file: Path) {
        val editor = Editor(file)
        editor.selection = selection
        editor.close = {
            close(editor)
        }
        editors.add(editor)
        editor.activate()
    }

    private fun close(editor: Editor) {
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selection.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }
}

class Editor(
    val fileName: String,
    val lines: (backgroundScope: CoroutineScope) -> Lines,
) {
    var close: (() -> Unit)? = null
    lateinit var selection: SingleSelection

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = this
    }

    class Line(val number: Int, val content: Content)

    interface Lines {
        val lineNumberDigitCount: Int get() = size.toString().length
        val size: Int
        operator fun get(index: Int): Line
    }

    class Content(val value: State<String>, val isCode: Boolean)
}

@ExperimentalPathApi
fun Editor(file: Path) = Editor(
    fileName = file.name
) { backgroundScope ->
    val textLines = try {
        file.readLines(backgroundScope)
    } catch (e: Throwable) {
        e.printStackTrace()
        EmptyTextLines
    }
    val isCode = file.name.endsWith(".java", ignoreCase = true)

    fun content(index: Int): Editor.Content {
        val text = textLines.get(index)
            .trim('\n') // fix for native crash in Skia.
        // Workaround for another Skia problem with empty line layout.
        // TODO: maybe use another symbols, i.e. \u2800 or \u00a0.
        val state = mutableStateOf(if (text.isEmpty()) " " else text)
        return Editor.Content(state, isCode)
    }

    object : Editor.Lines {
        override val size get() = textLines.size

        override fun get(index: Int): Editor.Line {
            return Editor.Line(
                number = index + 1,
                content = content(index)
            )
        }
    }
}

fun Path.readLines(scope: CoroutineScope): TextLines {
    var lines = emptyList<String>()

    var size by mutableStateOf(0)

    val refreshJob = scope.launch {
        delay(100)
        size = lines.size
        while (true) {
            delay(1000)
            size = lines.size
        }
    }

    scope.launch(Dispatchers.IO) {
        lines = try {
            Files.readAllLines(this@readLines)
        } catch (t: Throwable) {
            t.printStackTrace()
            emptyList()
        }
        refreshJob.cancel()
        size = lines.size
    }

    return object : TextLines {
        override val size get() = size

        override fun get(index: Int): String {
            return lines.get(index)
        }
    }
}

interface TextLines {
    val size: Int
    fun get(index: Int): String
}

object EmptyTextLines : TextLines {
    override val size: Int
        get() = 0

    override fun get(index: Int): String = ""
}
