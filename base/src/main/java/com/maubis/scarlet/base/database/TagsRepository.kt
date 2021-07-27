package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.daos.TagDao
import com.maubis.scarlet.base.database.entities.Tag
import java.util.concurrent.ConcurrentHashMap

class TagsRepository(private val database: TagDao) {

  private val tags: ConcurrentHashMap<String, Tag> by lazy { loadTagsFromDB() }

  fun save(tag: Tag) {
    val id = database.insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    tags[tag.uuid] = tag
  }

  fun delete(tag: Tag) {
    if (tag.isUnsaved()) {
      return
    }
    database.delete(tag)
    tags.remove(tag.uuid)
    tag.uid = 0
  }

  fun getAll(): List<Tag> {
    return tags.values.toList()
  }

  fun getByUUID(uuid: String): Tag? {
    return tags[uuid]
  }

  fun getByTitle(title: String): Tag? {
    return tags.values.firstOrNull { it.title == title }
  }

  fun search(string: String): List<Tag> {
    return tags.values
            .filter { string.isBlank() || it.title.contains(string, true) }
  }

  private fun loadTagsFromDB(): ConcurrentHashMap<String, Tag> {
    val tagsMap = ConcurrentHashMap<String, Tag>()
    database.getAll().forEach { tagsMap[it.uuid] = it }
    return tagsMap
  }
}