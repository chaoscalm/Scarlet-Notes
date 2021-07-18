package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.core.tag.isUnsaved
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.database.room.tag.TagDao
import java.util.concurrent.ConcurrentHashMap

class TagsRepository(private val database: TagDao) {

  private val tags = ConcurrentHashMap<String, Tag>()

  fun save(tag: Tag) {
    val id = database.insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    notifyInsertTag(tag)
  }

  fun delete(tag: Tag) {
    if (tag.isUnsaved()) {
      return
    }
    database.delete(tag)
    notifyDelete(tag)
    tag.uid = 0
  }

  private fun notifyInsertTag(tag: Tag) {
    maybeLoadFromDB()
    tags[tag.uuid] = tag
  }

  private fun notifyDelete(tag: Tag) {
    maybeLoadFromDB()
    tags.remove(tag.uuid)
  }

  fun getAll(): List<Tag> {
    maybeLoadFromDB()
    return tags.values.toList()
  }

  fun getByUUID(uuid: String): Tag? {
    maybeLoadFromDB()
    return tags[uuid]
  }

  fun getByTitle(title: String): Tag? {
    maybeLoadFromDB()
    return tags.values.firstOrNull { it.title == title }
  }

  fun search(string: String): List<Tag> {
    maybeLoadFromDB()
    return tags.values
      .filter { string.isBlank() || it.title.contains(string, true) }
  }

  @Synchronized
  private fun maybeLoadFromDB() {
    if (tags.isNotEmpty()) {
      return
    }
    database.all.forEach {
      tags[it.uuid] = it
    }
  }
}