package com.maubis.scarlet.base.editor.formats

import com.maubis.scarlet.base.settings.sEditorMoveChecked
import org.json.JSONObject
import java.util.*

class Format(var type: FormatType) {

  var uid: Int = 0
  var text: String = ""

  init {
    if (type == FormatType.SEPARATOR) {
      // Needed to make sure the Format doesn't get lost, since Formats containing no text
      // are discarded when the note is saved
      text = "---"
    }
  }

  val markdownText: String
    get() {
      return when (type) {
        FormatType.NUMBERED_LIST -> "- $text"
        FormatType.HEADING -> "# $text"
        FormatType.CHECKLIST_CHECKED -> "[x] $text"
        FormatType.CHECKLIST_UNCHECKED -> "[ ] $text"
        FormatType.SUB_HEADING -> "## $text"
        FormatType.CODE -> "```\n$text\n```"
        FormatType.QUOTE -> "> $text"
        FormatType.IMAGE -> ""
        FormatType.SEPARATOR -> "\n---\n"
        FormatType.TEXT -> text
        else -> return text
      }
    }

  constructor(type: FormatType, text: String) : this(type) {
    this.type = type
    this.text = text
  }

  fun toJson(): JSONObject? {
    if (text.trim { it <= ' ' }.isEmpty()) {
      return null
    }

    val map = mapOf("format" to type.name, "text" to text)
    return JSONObject(map)
  }

  companion object {
    fun fromJson(json: JSONObject): Format {
      return Format(
        type = FormatType.valueOf(json.getString("format")),
        text = json.getString("text"))
    }
  }
}

fun sectionPreservingSort(formats: List<Format>): List<Format> {
  if (!sEditorMoveChecked) {
    return formats
  }

  val mutableFormats = formats.toMutableList()
  var index = 0
  while (index < formats.size - 1) {
    val currentItem = mutableFormats[index]
    val nextItem = mutableFormats[index + 1]

    if (currentItem.type == FormatType.CHECKLIST_CHECKED
      && nextItem.type == FormatType.CHECKLIST_UNCHECKED) {
      Collections.swap(mutableFormats, index, index + 1)
      continue
    }
    index += 1
  }
  while (index > 0) {
    val currentItem = mutableFormats[index]
    val nextItem = mutableFormats[index - 1]

    if (currentItem.type == FormatType.CHECKLIST_UNCHECKED
      && nextItem.type == FormatType.CHECKLIST_CHECKED) {
      Collections.swap(mutableFormats, index, index - 1)
      continue
    }
    index -= 1
  }
  return mutableFormats
}