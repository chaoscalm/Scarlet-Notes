package com.maubis.scarlet.base.editor

enum class MarkdownFormatting(val startToken: String, val endToken: String = "", val requiresNewLine: Boolean = false) {
  BOLD(startToken = "**", endToken = "**"),
  UNDERLINE(startToken = "<u>", endToken = "</u>"),
  ITALICS(startToken = "*", endToken = "*"),
  UNORDERED(startToken = "- ", requiresNewLine = true),
  CODE(startToken = "`", endToken = "`"),
  STRIKE_THROUGH(startToken = "~~", endToken = "~~"),
}