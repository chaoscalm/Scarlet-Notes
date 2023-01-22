package com.maubis.markdown.spannable

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.*
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spans.*

fun Editable.clearMarkdownSpans() {
  val spans = getSpans(0, length, Any::class.java)
  for (span in spans) {
    if (span is RelativeSizeSpan
      || span is QuoteBlockSpan
      || span is StyleSpan
      || span is TypefaceSpan
      || span is UnderlineSpan
      || span is CustomMarkdownSpan) {
      removeSpan(span)
    }
  }
}

internal fun Spannable.setSpans(spanInfos: List<SpanInfo>) {
  spanInfos.forEach { setSpansFor(it) }
}

internal fun Spannable.setSpansFor(info: SpanInfo) {
  val start = info.start
  val end = info.end
  when (info.markdownType) {
    MarkdownType.HEADING_1 -> relativeSize(1.75f, start, end)
      .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
      .bold(start, end)
    MarkdownType.HEADING_2 -> relativeSize(1.5f, start, end)
      .font(MarkdownConfig.spanConfig.heading2Typeface, start, end)
      .bold(start, end)
    MarkdownType.HEADING_3 -> relativeSize(1.25f, start, end)
      .font(MarkdownConfig.spanConfig.heading3Typeface, start, end)
      .bold(start, end)
    MarkdownType.CODE -> font(MarkdownConfig.spanConfig.codeTypeface, start, end)
      .code(start, end)
    MarkdownType.QUOTE -> quote(start, end).italic(start, end)
    MarkdownType.BOLD -> bold(start, end)
    MarkdownType.ITALICS -> italic(start, end)
    MarkdownType.UNDERLINE -> underline(start, end)
    MarkdownType.INLINE_CODE -> inlineCode(start, end)
    MarkdownType.STRIKE -> strike(start, end)
    MarkdownType.SEPARATOR -> separator(start, end)
    else -> {}
  }
}

fun Spannable.bold(start: Int, end: Int): Spannable {
  this.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.underline(start: Int, end: Int): Spannable {
  this.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.italic(start: Int, end: Int): Spannable {
  this.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.strike(start: Int, end: Int): Spannable {
  this.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.relativeSize(relativeSize: Float, start: Int, end: Int): Spannable {
  this.setSpan(RelativeSizeSpan(relativeSize), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.quote(start: Int, end: Int): Spannable {
  this.setSpan(QuoteBlockSpan(), start, end, 0)
  return this
}

fun Spannable.code(start: Int, end: Int): Spannable {
  this.setSpan(CodeBlockSpan(), start, end, 0)
  return this
}

fun Spannable.inlineCode(start: Int, end: Int): Spannable {
  this.setSpan(CodeSpan(), start, end, 0)
  return this
}

fun Spannable.separator(start: Int, end: Int): Spannable {
  this.setSpan(SeparatorSpan(), start, end, 0)
  return this
}

fun Spannable.font(font: Typeface, start: Int, end: Int): Spannable {
  this.setSpan(TypefaceSpanCompat(font), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}