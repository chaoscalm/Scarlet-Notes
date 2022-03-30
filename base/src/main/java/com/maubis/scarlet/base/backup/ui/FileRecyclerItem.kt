package com.maubis.scarlet.base.backup.ui

import com.maubis.scarlet.base.backup.AUTO_BACKUP_FILENAME
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import java.io.File

class FileRecyclerItem(
  val name: String,
  val path: String,
  val file: File) : RecyclerItem(), Comparable<FileRecyclerItem> {
  var selected = false

  override val type = Type.FILE

  override fun compareTo(other: FileRecyclerItem): Int {
    if (name.startsWith(NOTES_EXPORT_FILENAME) || name.startsWith(AUTO_BACKUP_FILENAME)) {
      return -1;
    }
    if (other.name.startsWith(NOTES_EXPORT_FILENAME) || other.name.startsWith(AUTO_BACKUP_FILENAME)) {
      return 1;
    }
    return 0;
  }

}