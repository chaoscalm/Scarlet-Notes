package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.daos.FolderDao
import com.maubis.scarlet.base.database.entities.Folder
import java.util.concurrent.ConcurrentHashMap

class FoldersRepository(private val database: FolderDao) {

  private val folders: ConcurrentHashMap<String, Folder> by lazy { loadFoldersFromDB() }

  fun save(folder: Folder) {
    val id = database.insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid
    folders[folder.uuid] = folder
  }

  fun delete(folder: Folder) {
    if (folder.isUnsaved()) {
      return
    }
    database.delete(folder)
    folders.remove(folder.uuid)
    folder.uid = 0
  }

  fun getAll(): List<Folder> {
    return folders.values.toList()
  }

  fun getByUUID(uuid: String): Folder? {
    return folders[uuid]
  }

  fun getByTitle(title: String): Folder? {
    return folders.values.firstOrNull { it.title == title }
  }

  private fun loadFoldersFromDB(): ConcurrentHashMap<String, Folder> {
    val foldersMap = ConcurrentHashMap<String, Folder>()
    database.getAll().forEach { foldersMap[it.uuid] = it }
    return foldersMap
  }
}