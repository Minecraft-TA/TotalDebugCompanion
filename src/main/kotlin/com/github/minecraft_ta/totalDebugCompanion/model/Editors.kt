package com.github.minecraft_ta.totalDebugCompanion.model

import androidx.compose.runtime.*
import com.github.minecraft_ta.totalDebugCompanion.util.IntList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.readLines

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

        override fun get(index: Int) = Editor.Line(
            number = index + 1,
            content = content(index)
        )
    }
}

fun Path.readLines(scope: CoroutineScope): TextLines {
    var byteBufferSize: Int
    val byteBuffer = RandomAccessFile(this.toFile(), "r").use { file ->
        byteBufferSize = file.length().toInt()
        file.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
    }

    val lineStartPositions = IntList()

    var size by mutableStateOf(0)

    val refreshJob = scope.launch {
        delay(100)
        size = lineStartPositions.size
        while (true) {
            delay(1000)
            size = lineStartPositions.size
        }
    }

    scope.launch(Dispatchers.IO) {
        readLinePositions(lineStartPositions)
        refreshJob.cancel()
        size = lineStartPositions.size
    }

    return object : TextLines {
        override val size get() = size

        override fun get(index: Int): String {
            val startPosition = lineStartPositions[index]
            val length = if (index + 1 < size) lineStartPositions[index + 1] - startPosition else
                byteBufferSize - startPosition
            // Only JDK since 13 has slice() method we need, so do ugly for now.
            byteBuffer.position(startPosition)
            val slice = byteBuffer.slice()
            slice.limit(length)
            return StandardCharsets.UTF_8.decode(slice).toString()
        }
    }
}

private fun Path.readLinePositions(
    starts: IntList
) {
    require(Files.size(this) <= Int.MAX_VALUE) {
        "Files with size over ${Int.MAX_VALUE} aren't supported"
    }

    val averageLineLength = 200
    starts.clear(Files.size(this).toInt() / averageLineLength)

    try {
        FileInputStream(this.toFile()).use {
            val channel = it.channel
            val ib = channel.map(
                FileChannel.MapMode.READ_ONLY, 0, channel.size()
            )
            var isBeginOfLine = true
            var position = 0L
            while (ib.hasRemaining()) {
                val byte = ib.get()
                if (isBeginOfLine) {
                    starts.add(position.toInt())
                }
                isBeginOfLine = byte.toChar() == '\n'
                position++
            }
            channel.close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        starts.clear(1)
        starts.add(0)
    }

    starts.compact()
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
