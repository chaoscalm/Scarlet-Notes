package com.maubis.scarlet.base.editor.formats

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
