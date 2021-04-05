package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.core.folder.isUnsaved
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.folder.FolderDao
import java.util.concurrent.ConcurrentHashMap

class FoldersRepository(private val database: FolderDao) {

  private val folders = ConcurrentHashMap<String, Folder>()

  fun save(folder: Folder) {
    val id = database.insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid
    notifyInsertFolder(folder)
  }

  fun delete(folder: Folder) {
    if (folder.isUnsaved()) {
      return
    }
    database.delete(folder)
    notifyDelete(folder)
    folder.uid = 0
  }

  private fun notifyInsertFolder(folder: Folder) {
    maybeLoadFromDB()
    folders[folder.uuid] = folder
  }

  private fun notifyDelete(folder: Folder) {
    maybeLoadFromDB()
    folders.remove(folder.uuid)
  }

  fun getCount(): Int {
    maybeLoadFromDB()
    return folders.size
  }

  fun getUUIDs(): List<String> {
    maybeLoadFromDB()
    return folders.values.map { it.uuid }
  }

  fun getAll(): List<Folder> {
    maybeLoadFromDB()
    return folders.values.toList()
  }

  fun getByID(uid: Int): Folder? {
    maybeLoadFromDB()
    return folders.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Folder? {
    maybeLoadFromDB()
    return folders[uuid]
  }

  fun getByTitle(title: String): Folder? {
    maybeLoadFromDB()
    return folders.values.firstOrNull { it.title == title }
  }

  fun search(string: String): List<Folder> {
    maybeLoadFromDB()
    return folders.values
      .filter { string.isBlank() || it.title.contains(string, true) }
  }

  @Synchronized
  private fun maybeLoadFromDB() {
    if (folders.isNotEmpty()) {
      return
    }
    database.all.forEach {
      folders[it.uuid] = it
    }
  }

  fun clear() {
    folders.clear()
  }
}