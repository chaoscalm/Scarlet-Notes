package com.maubis.scarlet.base.editor

import org.json.JSONObject

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
        FormatType.SUB_HEADING -> "## $text"
        FormatType.HEADING_3 -> "### $text"
        FormatType.CHECKLIST_CHECKED -> "[x] $text"
        FormatType.CHECKLIST_UNCHECKED -> "[ ] $text"
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

enum class FormatType {
  TAG,
  TEXT,
  NUMBERED_LIST,
  BULLET_1,
  BULLET_2,
  BULLET_3,
  IMAGE,
  HEADING,// HEADING_1
  SUB_HEADING, // HEADING_2
  HEADING_3,
  CHECKLIST_UNCHECKED,
  CHECKLIST_CHECKED,
  CODE,
  QUOTE,
  SEPARATOR;

  fun getNextFormatType(): FormatType {
    return when (this) {
      HEADING, SUB_HEADING, HEADING_3 -> TEXT
      CHECKLIST_CHECKED, CHECKLIST_UNCHECKED -> CHECKLIST_UNCHECKED
      NUMBERED_LIST -> NUMBERED_LIST
      else -> TEXT
    }
  }
}