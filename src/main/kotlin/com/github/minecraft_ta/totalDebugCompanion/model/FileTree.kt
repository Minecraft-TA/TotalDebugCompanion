package com.github.minecraft_ta.totalDebugCompanion.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.streams.toList
import kotlin.system.exitProcess


@OptIn(ExperimentalPathApi::class)
class ExpandableFile(
    val file: Path,
    val level: Int,
) {
    var children: List<ExpandableFile> by mutableStateOf(emptyList())
    val canExpand: Boolean get() = file.hasChildren

    fun toggleExpanded() {
        children = if (children.isEmpty()) {
            file.children
                .map { ExpandableFile(it, level + 1) }
                .sortedWith(compareBy({ it.file.isDirectory }, { it.file.name }))
                .sortedBy { !it.file.isDirectory }
        } else {
            emptyList()
        }
    }
}

@OptIn(ExperimentalPathApi::class)
class FileTree(val root: Path, private val editors: Editors) {
    private val expandableRoot = ExpandableFile(root, 0).apply {
        toggleExpanded()

        startDirectoryWatcher(this, editors)
    }

    val items: List<Item> get() = expandableRoot.toItems()

    inner class Item constructor(
        val file: ExpandableFile
    ) {
        val name: String get() = file.file.name

        val level: Int get() = file.level

        val type: ItemType
            get() = if (file.file.isDirectory) {
                ItemType.Folder(isExpanded = file.children.isNotEmpty(), canExpand = file.canExpand)
            } else {
                ItemType.File(ext = file.file.name.substringAfterLast(".").toLowerCase())
            }

        fun open() = when (type) {
            is ItemType.Folder -> file.toggleExpanded()
            is ItemType.File -> editors.open(file.file)
        }
    }

    sealed class ItemType {
        class Folder(val isExpanded: Boolean, val canExpand: Boolean) : ItemType()
        class File(val ext: String) : ItemType()
    }

    private fun ExpandableFile.toItems(): List<Item> {
        fun ExpandableFile.addTo(list: MutableList<Item>) {
            list.add(Item(this))
            for (child in children) {
                child.addTo(list)
            }
        }

        val list = mutableListOf<Item>()
        addTo(list)
        return list
    }
}

private fun startDirectoryWatcher(expandableFile: ExpandableFile, editors: Editors) {
    //start directory watcher
    val watchService = FileSystems.getDefault().newWatchService()
    expandableFile.file.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE)

    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch(Dispatchers.IO) {
        while (true) {
            val key = watchService.take()

            var any = false
            key.pollEvents().forEach {
                val kind = it.kind()

                if (kind == StandardWatchEventKinds.OVERFLOW || kind == StandardWatchEventKinds.ENTRY_MODIFY)
                    return@forEach

                any = true

                if (kind == StandardWatchEventKinds.ENTRY_DELETE)
                    editors.editors.find { editor ->
                        editor.fileName == (it as WatchEvent<Path>).context().fileName.toString()
                    }?.close?.invoke()
            }

            if (any) {
                //refresh
                expandableFile.toggleExpanded()
                expandableFile.toggleExpanded()
            }

            val valid = key.reset()
            if (!valid) {
                println("Directory no longer accessible")
                exitProcess(1)
            }
        }
    }
}

val Path.isDirectory get() = Files.isDirectory(this)
val Path.hasChildren get() = this.isDirectory && this.children.isNotEmpty()
val Path.children: List<Path>
    get() {
        try {
            return Files.walk(this, 1).toList().drop(1)
        } catch (e: Exception) {
            return emptyList()
        }
    }

