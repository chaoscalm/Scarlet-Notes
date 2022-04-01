package com.maubis.scarlet.base.note

import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.FormatType
import com.maubis.scarlet.base.editor.formats.Formats

object NoteBuilder {
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
    note.content = Formats.getNoteContent(formats)
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
    note.content = Formats.getNoteContent(formats)
    return note
  }

  fun genFromKeep(content: String): List<Format> {
    val randomDelimiter = "-+-" + RandomHelper.getRandom() + "-+-"
    var delimited = content.replace("(^|\n)\\s*\\[\\s\\]\\s*".toRegex(), randomDelimiter + "[ ]")
    delimited = delimited.replace("(^|\n)\\s*\\[x\\]\\s*".toRegex(), randomDelimiter + "[x]")

    val items = delimited.split(randomDelimiter)
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
}