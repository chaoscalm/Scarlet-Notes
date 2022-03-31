package com.maubis.scarlet.base.core.format

enum class MarkdownFormatting(val startToken: String, val endToken: String = "", val requiresNewLine: Boolean = false) {
  BOLD(startToken = "**", endToken = "**"),
  UNDERLINE(startToken = "_", endToken = "_"),
  ITALICS(startToken = "*", endToken = "*"),
  UNORDERED(startToken = "- ", requiresNewLine = true),
  CHECKLIST_UNCHECKED(startToken = "[ ] ", requiresNewLine = true),
  CODE(startToken = "`", endToken = "`"),
  STRIKE_THROUGH(startToken = "~~", endToken = "~~"),
}