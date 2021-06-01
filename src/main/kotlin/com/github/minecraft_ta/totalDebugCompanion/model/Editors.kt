package com.github.minecraft_ta.totalDebugCompanion.model

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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

    var editors = mutableStateListOf<AbstractEditor>()
        private set

    val active: AbstractEditor? get() = selection.selected as AbstractEditor?

    @OptIn(ExperimentalPathApi::class)
    fun openFile(file: Path) {
        val editor = Editor(file)
        openEditor(editor)
    }

    fun openSearchEditor(query: String, results: List<String>, methodSearch: Boolean, classesCount: Int, time: Int) {
        val editor = SearchEditor(query, mutableStateListOf(*results.sorted().toTypedArray()), methodSearch, classesCount, time)
        openEditor(editor)
    }

    private fun openEditor(editor: AbstractEditor) {
        editor.selection = selection
        editor.close = {
            close(editor)
        }
        editors.add(editor)
        editor.activate()
    }

    private fun close(editor: AbstractEditor) {
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selection.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }
}

abstract class AbstractEditor {
    var close: (() -> Unit)? = null
    lateinit var selection: SingleSelection

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = this
    }

    abstract fun getTitle(): String
}

class SearchEditor(
    val query: String,
    val results: SnapshotStateList<String>,
    val methodSearch: Boolean,
    val classesCount: Int,
    val time: Int
) : AbstractEditor() {

    override fun getTitle(): String {
        return query
    }
}

class CodeEditor(
    val fileName: String,
    val lines: (backgroundScope: CoroutineScope) -> Lines,
) : AbstractEditor() {

    override fun getTitle(): String {
        return fileName
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
fun Editor(file: Path) = CodeEditor(
    fileName = file.name
) { backgroundScope ->
    val textLines = try {
        file.readLines(backgroundScope)
    } catch (e: Throwable) {
        e.printStackTrace()
        EmptyTextLines
    }
    val isCode = file.name.endsWith(".java", ignoreCase = true)

    fun content(index: Int): CodeEditor.Content {
        val text = textLines.get(index)
            .trim('\n') // fix for native crash in Skia.
        // Workaround for another Skia problem with empty line layout.
        // TODO: maybe use another symbols, i.e. \u2800 or \u00a0.
        val state = mutableStateOf(if (text.isEmpty()) " " else text)
        return CodeEditor.Content(state, isCode)
    }

    object : CodeEditor.Lines {
        override val size get() = textLines.size

        override fun get(index: Int): CodeEditor.Line {
            return CodeEditor.Line(
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
