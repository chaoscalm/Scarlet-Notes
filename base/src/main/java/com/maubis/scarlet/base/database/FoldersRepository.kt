package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.daos.FolderDao
import com.maubis.scarlet.base.database.entities.Folder
import java.util.concurrent.ConcurrentHashMap

class FoldersRepository(private val database: FolderDao) {

  private val folders: ConcurrentHashMap<String, Folder> by lazy { loadFoldersFromDB() }

  fun save(folder: Folder) {
    database.insertFolder(folder)
    folders[folder.uuid] = folder
  }

  fun delete(folder: Folder) {
    if (!exists(folder.uuid)) {
      return
    }
    database.delete(folder)
    folders.remove(folder.uuid)
  }

  fun exists(folderUuid: String): Boolean = folders.containsKey(folderUuid)

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