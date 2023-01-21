package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.blocks.MarkdownBlockParser
import com.maubis.markdown.blocks.MarkdownBlockType
import com.maubis.markdown.inline.InlineMarkdownParser
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
    val blocks = MarkdownBlockParser(text).parseText()
    var currentIndex = 0
    val textBuilder = StringBuilder()
    val formats = ArrayList<SpanInfo>()
    blocks.forEach { block ->
      val finalIndex: Int
      val strippedText: String
      when (block.type) {
        MarkdownBlockType.CODE -> {
          strippedText = if (stripDelimiter) block.strippedText() else block.text()
          finalIndex = currentIndex + strippedText.length
          formats.add(SpanInfo(map(block.type), currentIndex, finalIndex))
        }
        else -> {
          val inlineMarkdown = InlineMarkdownParser(if (stripDelimiter) block.strippedText() else block.text()).parseText()
          strippedText = inlineMarkdown.textContent(stripDelimiter)
          finalIndex = currentIndex + strippedText.length

          formats.add(SpanInfo(map(block.type), currentIndex, finalIndex))
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