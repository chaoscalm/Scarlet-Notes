package com.maubis.markdown.inline

import com.maubis.markdown.spannable.MarkdownType
import com.maubis.markdown.spannable.SpanInfo

internal class NormalInlineSegmentBuilder {
  val builder: StringBuilder = StringBuilder()
  fun build(): NormalInlineSegment {
    return NormalInlineSegment(builder.toString())
  }
}

internal class InlineSegmentBuilder {
  val children = ArrayList<MarkdownInlineSegment>()
  var config: InlineConfig = InvalidInline(InlineSegmentType.INVALID)
  var paired: Boolean = false

  fun build(): MarkdownInlineSegment {
    return DelimitedInlineSegment(config, children)
  }
}

internal abstract class MarkdownInlineSegment {
  abstract fun type(): InlineSegmentType

  abstract fun config(): InlineConfig

  /**
   * The original text inside an inline segment,
   * e.g. for the BOLD in this example "a **b _c_ d** e" is "**b _c_ d**"
   */
  abstract fun textContent(stripDelimiters: Boolean = false): String

  /**
   * The list of spans that make up this segment.
   * This can be overlapping information, which is needed for rendering
   */
  abstract fun textSpans(stripDelimiters: Boolean = false, startPosition: Int): List<SpanInfo>

  fun debug(): String {
    if (this !is DelimitedInlineSegment) {
      return textContent()
    }

    val string = StringBuilder()
    string.append("{${type().name}: ")
    children.forEach {
      string.append(it.debug())
    }
    string.append("}")
    return string.toString()
  }
}

internal class NormalInlineSegment(val text: String) : MarkdownInlineSegment() {
  override fun type() = InlineSegmentType.NORMAL

  override fun config(): InlineConfig = InvalidInline(InlineSegmentType.NORMAL)

  override fun textContent(stripDelimiters: Boolean): String {
    return text
  }

  override fun textSpans(stripDelimiters: Boolean, startPosition: Int): List<SpanInfo> {
    return listOf(SpanInfo(MarkdownType.NORMAL, startPosition, startPosition + textContent(stripDelimiters).length))
  }
}

internal class DelimitedInlineSegment(val config: InlineConfig, val children: List<MarkdownInlineSegment>) : MarkdownInlineSegment() {

  override fun type() = config.type()

  override fun config(): InlineConfig = config

  override fun textContent(stripDelimiters: Boolean): String {
    val builder = StringBuilder()
    if (!stripDelimiters && config is DelimitedInline) {
      builder.append(config.startDelimiter)
    }
    children.forEach { builder.append(it.textContent(stripDelimiters)) }
    if (!stripDelimiters && config is DelimitedInline) {
      builder.append(config.endDelimiter)
    }
    return builder.toString()
  }

  override fun textSpans(stripDelimiters: Boolean, startPosition: Int): List<SpanInfo> {
    val childrenSpans = ArrayList<SpanInfo>()
    var currentIndex = startPosition
    children.forEach {
      childrenSpans.addAll(it.textSpans(stripDelimiters, currentIndex))
      currentIndex += it.textContent(stripDelimiters).length
    }
    childrenSpans.add(SpanInfo(type(), startPosition, startPosition + textContent(stripDelimiters).length))
    return childrenSpans
  }
}
