package com.maubis.scarlet.base.core.note

import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.database.entities.Note

class NoteBuilder {

  /**
   * Generate blank note with color
   */
  fun emptyNote(color: Int): Note {
    val note = Note()
    note.color = color
    return note
  }

  /**
   * Generate blank note from basic title and description
   */
  fun gen(title: String, description: String): Note {
    val note = Note()
    val formats = ArrayList<Format>()
    if (title.isNotEmpty()) {
      formats.add(Format(FormatType.HEADING, title))
    }
    formats.add(Format(FormatType.TEXT, description))
    note.content = FormatBuilder().getContent(formats)
    return note
  }

  /**
   * Generate blank note from basic title and description
   */
  fun gen(title: String, formatSource: List<Format>): Note {
    val note = Note()
    val formats = ArrayList<Format>()
    if (title.isNotEmpty()) {
      formats.add(Format(FormatType.HEADING, title))
    }
    formats.addAll(formatSource)
    note.content = FormatBuilder().getContent(formats)
    return note
  }

  fun genFromKeep(content: String): List<Format> {
    val randomDelimiter = "-+-" + RandomHelper.getRandom() + "-+-"
    var delimitered = content.replace("(^|\n)\\s*\\[\\s\\]\\s*".toRegex(), randomDelimiter + "[ ]")
    delimitered = delimitered.replace("(^|\n)\\s*\\[x\\]\\s*".toRegex(), randomDelimiter + "[x]")

    val items = delimitered.split(randomDelimiter)
    val formats = ArrayList<Format>()
    for (item in items) {
      when {
        item.startsWith("[ ]") -> formats.add(Format(FormatType.CHECKLIST_UNCHECKED, item.removePrefix("[ ]")))
        item.startsWith("[x]") -> formats.add(Format(FormatType.CHECKLIST_CHECKED, item.removePrefix("[x]")))
        !item.isBlank() -> formats.add(Format(FormatType.TEXT, item))
      }
    }
    return formats
  }

  fun copy(reference: Note): Note {
    val note = Note()
    note.uid = reference.uid
    note.uuid = reference.uuid
    note.state = reference.state
    note.content = reference.content
    note.timestamp = reference.timestamp
    note.updateTimestamp = reference.updateTimestamp
    note.color = reference.color
    note.tags = reference.tags
    note.pinned = reference.pinned
    note.locked = reference.locked
    note.reminder = reference.reminder
    note.folder = reference.folder
    return note
  }
}