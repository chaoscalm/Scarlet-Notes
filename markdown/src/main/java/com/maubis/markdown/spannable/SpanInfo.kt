package com.maubis.markdown.spannable

import com.maubis.markdown.blocks.MarkdownBlockType
import com.maubis.markdown.inline.InlineSegmentType

enum class MarkdownType {
  INVALID,
  HEADING_1,
  HEADING_2,
  HEADING_3,
  CODE,
  BULLET_1,
  BULLET_2,
  BULLET_3,
  QUOTE,
  NORMAL,
  BOLD,
  ITALICS,
  UNDERLINE,
  INLINE_CODE,
  STRIKE,
  SEPARATOR,
  CHECKLIST_UNCHECKED,
  CHECKLIST_CHECKED,
  IMAGE,
}

data class SpanResult(val text: String, val spans: List<SpanInfo>)

data class SpanInfo(val markdownType: MarkdownType, val start: Int, val end: Int)

fun map(type: MarkdownBlockType): MarkdownType {
  return when (type) {
    MarkdownBlockType.INVALID -> MarkdownType.INVALID
    MarkdownBlockType.HEADING_1 -> MarkdownType.HEADING_1
    MarkdownBlockType.HEADING_2 -> MarkdownType.HEADING_2
    MarkdownBlockType.HEADING_3 -> MarkdownType.HEADING_3
    MarkdownBlockType.NORMAL -> MarkdownType.NORMAL
    MarkdownBlockType.CODE -> MarkdownType.CODE
    MarkdownBlockType.BULLET_1 -> MarkdownType.BULLET_1
    MarkdownBlockType.BULLET_2 -> MarkdownType.BULLET_2
    MarkdownBlockType.BULLET_3 -> MarkdownType.BULLET_3
    MarkdownBlockType.QUOTE -> MarkdownType.QUOTE
    MarkdownBlockType.SEPARATOR -> MarkdownType.SEPARATOR
    MarkdownBlockType.CHECKLIST_UNCHECKED -> MarkdownType.CHECKLIST_UNCHECKED
    MarkdownBlockType.CHECKLIST_CHECKED -> MarkdownType.CHECKLIST_CHECKED
    MarkdownBlockType.IMAGE -> MarkdownType.IMAGE
  }
}

fun map(type: InlineSegmentType): MarkdownType {
  return when (type) {
    InlineSegmentType.INVALID -> MarkdownType.INVALID
    InlineSegmentType.NORMAL -> MarkdownType.NORMAL
    InlineSegmentType.BOLD -> MarkdownType.BOLD
    InlineSegmentType.ITALICS -> MarkdownType.ITALICS
    InlineSegmentType.UNDERLINE -> MarkdownType.UNDERLINE
    InlineSegmentType.CODE -> MarkdownType.INLINE_CODE
    InlineSegmentType.STRIKE -> MarkdownType.STRIKE
    InlineSegmentType.IGNORE_CHAR -> MarkdownType.NORMAL
  }
}