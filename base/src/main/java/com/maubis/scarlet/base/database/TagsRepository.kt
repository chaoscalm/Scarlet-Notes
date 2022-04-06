package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.daos.TagDao
import com.maubis.scarlet.base.database.entities.Tag
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TagsRepository(private val database: TagDao) {

  private val tags: ConcurrentHashMap<UUID, Tag> by lazy { loadTagsFromDB() }

  fun save(tag: Tag) {
    database.insertTag(tag)
    tags[tag.uuid] = tag
  }

  fun delete(tag: Tag) {
    if (!exists(tag.uuid)) {
      return
    }
    database.delete(tag)
    tags.remove(tag.uuid)
  }

  fun exists(tagUuid: UUID): Boolean = tags.containsKey(tagUuid)

  fun getAll(): List<Tag> {
    return tags.values.toList()
  }

  fun getByUUID(uuid: UUID): Tag? {
    return tags[uuid]
  }

  fun search(string: String): List<Tag> {
    return tags.values
            .filter { string.isBlank() || it.title.contains(string, true) }
  }

  private fun loadTagsFromDB(): ConcurrentHashMap<UUID, Tag> {
    val tagsMap = ConcurrentHashMap<UUID, Tag>()
    database.getAll().forEach { tagsMap[it.uuid] = it }
    return tagsMap
  }
}