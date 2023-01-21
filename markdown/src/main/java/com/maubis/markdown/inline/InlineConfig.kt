package com.maubis.markdown.inline

interface InlineConfig {
  val type: InlineSegmentType
  val identifier: String
    get() = ""

  fun isStart(segment: String, index: Int) = false
  fun startIncrement() = 0
  fun isEnd(segment: String, index: Int) = false
  fun endIncrement() = 0
}

class PlainInline(override val type: InlineSegmentType) : InlineConfig

class StartMarkerInline(override val type: InlineSegmentType, private val startDelimiter: String) : InlineConfig {
  override fun isStart(segment: String, index: Int): Boolean {
    if (index + startDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, startDelimiter, 0, startDelimiter.length, true)
  }

  override fun isEnd(segment: String, index: Int): Boolean = true
  override fun startIncrement(): Int = startDelimiter.length

  override val identifier = "${type.name}($startDelimiter)"
}

class DelimitedInline(
  override val type: InlineSegmentType,
  val startDelimiter: String,
  val endDelimiter: String
) : InlineConfig {
  override fun isStart(segment: String, index: Int): Boolean {
    if (index + startDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, startDelimiter, 0, startDelimiter.length, true)
  }

  override fun isEnd(segment: String, index: Int): Boolean {
    if (index + endDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, endDelimiter, 0, endDelimiter.length, true)
  }

  override fun startIncrement(): Int = startDelimiter.length
  override fun endIncrement(): Int = endDelimiter.length

  override val identifier = "${type.name}($startDelimiter,$endDelimiter)"
}