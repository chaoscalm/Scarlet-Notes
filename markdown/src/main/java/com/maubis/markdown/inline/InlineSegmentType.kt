package com.maubis.markdown.inline

enum class InlineSegmentType {
  INVALID,
  NORMAL,
  BOLD,
  ITALICS,
  UNDERLINE,
  CODE,
  STRIKE,
  IGNORE_CHAR,
}