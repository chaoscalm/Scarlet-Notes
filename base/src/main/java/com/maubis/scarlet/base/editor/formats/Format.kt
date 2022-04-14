package com.maubis.scarlet.base.editor.formats

import com.maubis.scarlet.base.settings.sEditorMoveChecked
import org.json.JSONObject
import java.util.*

class Format(var formatType: FormatType) {

  var uid: Int = 0
  var text: String = ""

  init {
    if (formatType == FormatType.SEPARATOR) {
      // Needed to make sure the Format doesn't get lost, since Formats containing no text
      // are discarded when the note is saved
      text = "---"
    }
  }

  val markdownText: String
    get() {
      return when (formatType) {
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

  constructor(formatType: FormatType, text: String) : this(formatType) {
    this.formatType = formatType
    this.text = text
  }

  fun toJson(): JSONObject? {
    if (text.trim { it <= ' ' }.isEmpty()) {
      return null
    }

    val map = mapOf("format" to formatType.name, "text" to text)
    return JSONObject(map)
  }

  companion object {
    fun fromJson(json: JSONObject): Format {
      return Format(FormatType.valueOf(json.getString("format"))).apply {
        text = json.getString("text")
      }
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

    if (currentItem.formatType == FormatType.CHECKLIST_CHECKED
      && nextItem.formatType == FormatType.CHECKLIST_UNCHECKED) {
      Collections.swap(mutableFormats, index, index + 1)
      continue
    }
    index += 1
  }
  while (index > 0) {
    val currentItem = mutableFormats[index]
    val nextItem = mutableFormats[index - 1]

    if (currentItem.formatType == FormatType.CHECKLIST_UNCHECKED
      && nextItem.formatType == FormatType.CHECKLIST_CHECKED) {
      Collections.swap(mutableFormats, index, index - 1)
      continue
    }
    index -= 1
  }
  return mutableFormats
}