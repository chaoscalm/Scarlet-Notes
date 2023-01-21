package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.blocks.MarkdownBlockParser
import com.maubis.markdown.blocks.MarkdownBlockType
import com.maubis.markdown.inline.InlineMarkdownParser
import com.maubis.markdown.spannable.ParseResult
import com.maubis.markdown.spannable.SpanInfo
import com.maubis.markdown.spannable.setSpans
import com.maubis.markdown.spannable.setSpansFor

object Markdown {
  fun render(text: String, strip: Boolean = false): Spannable {
    val result = parseMarkdown(text, strip)
    return SpannableStringBuilder(result.text).apply {
      setSpans(result.spans)
    }
  }

  fun renderWithCustomFormatting(text: String, strip: Boolean = false, customSpanInfoAction: (Spannable, SpanInfo) -> Boolean): Spannable {
    val result = parseMarkdown(text, strip)
    val spannable = SpannableStringBuilder(result.text)
    result.spans.forEach { spanInfo ->
      if (!customSpanInfoAction(spannable, spanInfo)) {
        spannable.setSpansFor(spanInfo)
      }
    }
    return spannable
  }

  fun renderSegment(text: String, strip: Boolean = false): Spannable {
    val inlineMarkdown = InlineMarkdownParser(text).parseText()
    val textToDisplay = inlineMarkdown.textContent(strip)
    return SpannableStringBuilder(textToDisplay).apply {
      setSpans(inlineMarkdown.textSpans(strip, 0))
    }
  }

  fun Spannable.applyMarkdownSpans(text: String) {
    val spans = parseMarkdown(text).spans
    this.setSpans(spans)
  }

  private fun parseMarkdown(text: String, stripDelimiter: Boolean = false): ParseResult {
    val blocks = MarkdownBlockParser(text).parseText()
    var currentIndex = 0
    val textBuilder = StringBuilder()
    val spans = ArrayList<SpanInfo>()
    blocks.forEach { block ->
      val finalIndex: Int
      val strippedText: String
      when (block.type) {
        MarkdownBlockType.CODE -> {
          strippedText = if (stripDelimiter) block.strippedText() else block.text()
          finalIndex = currentIndex + strippedText.length
          spans.add(SpanInfo(block.type, currentIndex, finalIndex))
        }
        else -> {
          val inlineMarkdown = InlineMarkdownParser(if (stripDelimiter) block.strippedText() else block.text()).parseText()
          strippedText = inlineMarkdown.textContent(stripDelimiter)
          finalIndex = currentIndex + strippedText.length

          spans.add(SpanInfo(block.type, currentIndex, finalIndex))
          spans.addAll(inlineMarkdown.textSpans(stripDelimiter, currentIndex))
        }
      }

      currentIndex = finalIndex + 1
      textBuilder.append(strippedText)
      textBuilder.append("\n")
    }
    return ParseResult(textBuilder.toString(), spans)
  }
}