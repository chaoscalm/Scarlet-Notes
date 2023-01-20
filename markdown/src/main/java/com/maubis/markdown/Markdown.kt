package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.inline.InlineMarkdownParser
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.markdown.spannable.*

object Markdown {
  fun render(text: String, strip: Boolean = false): Spannable {
    val spans = getSpanInfo(text, strip)
    val spannable = SpannableStringBuilder(spans.text)
    spannable.setFormats(spans.spans)
    return spannable
  }

  fun renderWithCustomFormatting(text: String, strip: Boolean = false, customSpanInfoAction: (Spannable, SpanInfo) -> Boolean): Spannable {
    val spans = getSpanInfo(text, strip)
    val spannable = SpannableStringBuilder(spans.text)
    spans.spans.forEach {
      if (!customSpanInfoAction(spannable, it)) {
        spannable.setDefaultFormats(it)
      }
    }
    return spannable
  }

  fun renderSegment(text: String, strip: Boolean = false): Spannable {
    val inlineMarkdown = InlineMarkdownParser(text).parseText()
    val strippedText = inlineMarkdown.textContent(strip)
    val formats = inlineMarkdown.textSpans(strip, 0)

    val spannable = SpannableStringBuilder(strippedText)
    spannable.setFormats(formats)
    return spannable
  }

  fun getSpanInfo(text: String, stripDelimiter: Boolean = false): SpanResult {
    val segments = TextSegmenter(text).get()
    var currentIndex = 0
    val textBuilder = StringBuilder()
    val formats = ArrayList<SpanInfo>()
    segments.forEach {
      val finalIndex: Int
      val strippedText: String
      when {
        it.type() == MarkdownSegmentType.CODE -> {
          strippedText = if (stripDelimiter) it.strip() else it.text()
          finalIndex = currentIndex + strippedText.length
          formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
        }
        else -> {
          val inlineMarkdown = InlineMarkdownParser(if (stripDelimiter) it.strip() else it.text()).parseText()
          strippedText = inlineMarkdown.textContent(stripDelimiter)
          finalIndex = currentIndex + strippedText.length

          formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
          formats.addAll(inlineMarkdown.textSpans(stripDelimiter, currentIndex))
        }
      }

      currentIndex = finalIndex + 1
      textBuilder.append(strippedText)
      textBuilder.append("\n")
    }
    return SpanResult(textBuilder.toString(), formats)
  }
}