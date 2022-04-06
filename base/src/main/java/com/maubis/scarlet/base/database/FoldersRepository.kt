package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.daos.FolderDao
import com.maubis.scarlet.base.database.entities.Folder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FoldersRepository(private val database: FolderDao) {

  private val folders: ConcurrentHashMap<UUID, Folder> by lazy { loadFoldersFromDB() }

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

  fun exists(folderUuid: UUID): Boolean = folders.containsKey(folderUuid)

  fun getAll(): List<Folder> {
    return folders.values.toList()
  }

  fun getAllUUIDs(): List<UUID> {
    return folders.keys.toList()
  }

  fun getByUUID(uuid: UUID): Folder? {
    return folders[uuid]
  }

  private fun loadFoldersFromDB(): ConcurrentHashMap<UUID, Folder> {
    val foldersMap = ConcurrentHashMap<UUID, Folder>()
    database.getAll().forEach { foldersMap[it.uuid] = it }
    return foldersMap
  }
}