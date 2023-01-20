package com.maubis.markdown.inline

interface InlineConfig {
  fun type(): InlineSegmentType = InlineSegmentType.INVALID
  fun isStart(segment: String, index: Int) = false
  fun startIncrement() = 0
  fun isEnd(segment: String, index: Int) = false
  fun endIncrement() = 0
  fun identifier() = ""
}

class InvalidInline(val type: InlineSegmentType) : InlineConfig {
  override fun type() = type
}

class StartMarkerInline(val type: InlineSegmentType, private val startDelimiter: String) : InlineConfig {
  override fun type() = type

  override fun isStart(segment: String, index: Int): Boolean {
    if (index + startDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, startDelimiter, 0, startDelimiter.length, true)
  }

  override fun isEnd(segment: String, index: Int): Boolean = true

  override fun startIncrement(): Int = startDelimiter.length
  override fun identifier(): String = "${type().name}($startDelimiter)"
}

class DelimitedInline(
  val type: InlineSegmentType,
  val startDelimiter: String,
  val endDelimiter: String) : InlineConfig {
  override fun type() = type

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
  override fun identifier(): String = "${type().name}($startDelimiter,$endDelimiter)"
}