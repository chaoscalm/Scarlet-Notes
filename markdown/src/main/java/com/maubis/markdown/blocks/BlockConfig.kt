package com.maubis.markdown.blocks

interface BlockConfig {
  val type: MarkdownBlockType
  fun lineIsBlock(line: String) = false
  fun lineStartsBlock(line: String) = false
  fun lineEndsBlock(line: String) = false
}

class PlainBlock(override val type: MarkdownBlockType) : BlockConfig

class FullLineBlock(override val type: MarkdownBlockType, private val lineToken: String) : BlockConfig {
  override fun lineIsBlock(line: String): Boolean {
    return line.trim() == lineToken
  }
}

class SingleLineStartBlock(override val type: MarkdownBlockType,
                           val lineStartToken: String) : BlockConfig {
  override fun lineIsBlock(line: String): Boolean {
    return line.startsWith(lineStartToken)
  }
}

class MultilineDelimitedBlock(
  override val type: MarkdownBlockType,
  val multilineStartToken: String,
  val multilineEndToken: String
) : BlockConfig {
  override fun lineStartsBlock(line: String): Boolean {
    return line.trim() == multilineStartToken
  }

  override fun lineEndsBlock(line: String): Boolean {
    return line.trim() == multilineEndToken
  }
}

class MultilineStartBlock(
  override val type: MarkdownBlockType,
  val multilineStartToken: String
) : BlockConfig {
  override fun lineStartsBlock(line: String): Boolean {
    return line.startsWith(multilineStartToken)
  }
}
