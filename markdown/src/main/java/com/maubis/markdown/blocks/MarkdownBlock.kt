package com.maubis.markdown.blocks

internal class MarkdownBlockBuilder {
  var config: BlockConfig = PlainBlock(MarkdownBlockType.INVALID)
  val builder: StringBuilder = StringBuilder()

  fun build(): MarkdownBlock {
    val text = builder.toString()
    return when (val blockConfig = config) {
      is SingleLineStartBlock -> SingleLineStartMarkdownBlock(blockConfig, text)
      is SingleLineDelimitedBlock -> SingleLineDelimitedMarkdownBlock(blockConfig, text)
      is MultilineDelimitedBlock -> MultilineDelimitedMarkdownBlock(blockConfig, text)
      is MultilineStartBlock -> MultilineStartMarkdownBlock(blockConfig, text)
      else -> NormalMarkdownBlock(blockConfig.type, text)
    }
  }
}

abstract class MarkdownBlock {
  abstract val type: MarkdownBlockType

  /**
   * Return the text contained in the block, without the delimiters
   */
  abstract fun strippedText(): String

  /**
   * Return the entire text contained in the block, including the delimiters
   */
  abstract fun text(): String
}

class NormalMarkdownBlock(override val type: MarkdownBlockType, val text: String) : MarkdownBlock() {
  override fun strippedText(): String {
    return text
  }

  override fun text(): String = text
}

class SingleLineStartMarkdownBlock(val config: SingleLineStartBlock, val text: String) : MarkdownBlock() {
  override val type = config.type

  override fun strippedText(): String {
    return text.removePrefix(config.lineStartToken)
  }

  override fun text(): String = text
}

class SingleLineDelimitedMarkdownBlock(val config: SingleLineDelimitedBlock, val text: String) : MarkdownBlock() {
  override val type = config.type

  override fun strippedText(): String {
    return text.removePrefix(config.lineStartToken).trim().removeSuffix(config.lineEndToken)
  }

  override fun text(): String = text
}

class MultilineDelimitedMarkdownBlock(val config: MultilineDelimitedBlock, val text: String) : MarkdownBlock() {
  override val type = config.type

  override fun strippedText(): String {
    return text.trim()
        .removePrefix(config.multilineStartToken).trim()
        .removeSuffix(config.multilineEndToken).trim()
  }

  override fun text(): String = text
}

class MultilineStartMarkdownBlock(val config: MultilineStartBlock, val text: String) : MarkdownBlock() {
  override val type = config.type

  override fun strippedText(): String {
    return text.trim()
        .removePrefix(config.multilineStartToken).trim()
  }

  override fun text(): String = text
}