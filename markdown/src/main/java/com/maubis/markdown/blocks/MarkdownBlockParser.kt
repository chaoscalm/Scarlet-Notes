package com.maubis.markdown.blocks

class MarkdownBlockParser(val text: String) {

  private var currentBlock = MarkdownBlockBuilder()
  private val processedBlocks = ArrayList<MarkdownBlock>()

  fun parseText(): List<MarkdownBlock> {
    processedBlocks.clear()
    val allMultilineBlockTypes = knownMarkdownBlocks.filterIsInstance<MultilineDelimitedBlock>()
    val allFullLineBlockTypes = knownMarkdownBlocks.filterIsInstance<FullLineBlock>()
    val allMultilineStartBlockTypes = knownMarkdownBlocks.filterIsInstance<MultilineStartBlock>()
    val allSingleLineBlockTypes = knownMarkdownBlocks.filterIsInstance<SingleLineStartBlock>()
    val lines = text.split("\n")
    for (line in lines) {
      val startCurrentConfig = currentBlock

      // Multiline Code is finishing
      if (startCurrentConfig.config is MultilineDelimitedBlock
        && startCurrentConfig.config.lineEndsBlock(line)) {
        currentBlock.builder.append("\n")
        currentBlock.builder.append(line)
        maybeAddCurrentBlock()
        continue
      }

      // Continuing the multiline code
      if (startCurrentConfig.config is MultilineDelimitedBlock) {
        currentBlock.builder.append("\n")
        currentBlock.builder.append(line)
        continue
      }

      // Check if full line block
      val fullLineBlock = allFullLineBlockTypes.firstOrNull { it.lineIsBlock(line) }
      if (fullLineBlock != null) {
        maybeAddCurrentBlock()
        currentBlock.config = fullLineBlock
        currentBlock.builder.append(line)
        maybeAddCurrentBlock()
        continue
      }

      // Check if start of multiline block
      val multilineBlock = allMultilineBlockTypes.firstOrNull { it.lineStartsBlock(line) }
      if (multilineBlock != null) {
        maybeAddCurrentBlock()
        currentBlock.config = multilineBlock
        currentBlock.builder.append(line)
        continue
      }

      // Check if start of multiline start block
      val multilineStartBlock = allMultilineStartBlockTypes.firstOrNull { it.lineStartsBlock(line) }
      if (multilineStartBlock != null) {
        maybeAddCurrentBlock()
        currentBlock.config = multilineStartBlock
        currentBlock.builder.append(line)
        continue
      }

      // Check if single line block
      val singleLineBlock = allSingleLineBlockTypes.firstOrNull { it.lineIsBlock(line) }
      if (singleLineBlock != null) {
        maybeAddCurrentBlock()
        currentBlock.config = singleLineBlock
        currentBlock.builder.append(line)
        maybeAddCurrentBlock()
        continue
      }

      // Multiline start block in progress, end if double new line
      if (currentBlock.config is MultilineStartBlock && line.isEmpty()) {
        maybeAddCurrentBlock()
        currentBlock.config = PlainBlock(MarkdownBlockType.NORMAL)
        currentBlock.builder.append(line)
        continue
      }

      if (currentBlock.config.type == MarkdownBlockType.INVALID) {
        currentBlock.config = PlainBlock(MarkdownBlockType.NORMAL)
        currentBlock.builder.append(line)
        continue
      }

      // Normal or multiline start block
      currentBlock.builder.append("\n")
      currentBlock.builder.append(line)
    }
    maybeAddCurrentBlock()
    return processedBlocks
  }

  private fun maybeAddCurrentBlock() {
    if (currentBlock.config.type == MarkdownBlockType.INVALID) {
      currentBlock = MarkdownBlockBuilder()
      return
    }
    processedBlocks.add(currentBlock.build())
    currentBlock = MarkdownBlockBuilder()
  }

  companion object {
    private val knownMarkdownBlocks = arrayOf(
      PlainBlock(MarkdownBlockType.INVALID),
      PlainBlock(MarkdownBlockType.NORMAL),

      SingleLineStartBlock(MarkdownBlockType.HEADING_1, "# "),
      SingleLineStartBlock(MarkdownBlockType.HEADING_2, "## "),
      SingleLineStartBlock(MarkdownBlockType.HEADING_3, "### "),

      MultilineDelimitedBlock(MarkdownBlockType.CODE, "```", "```"),

      SingleLineStartBlock(MarkdownBlockType.BULLET_1, "- "),
      SingleLineStartBlock(MarkdownBlockType.BULLET_2, "  - "),
      SingleLineStartBlock(MarkdownBlockType.BULLET_2, " - "),
      SingleLineStartBlock(MarkdownBlockType.BULLET_3, "    - "),
      SingleLineStartBlock(MarkdownBlockType.BULLET_3, "    - "),
      SingleLineStartBlock(MarkdownBlockType.BULLET_3, "     - "),

      MultilineStartBlock(MarkdownBlockType.QUOTE, "> "),

      FullLineBlock(MarkdownBlockType.SEPARATOR, "---"),
      FullLineBlock(MarkdownBlockType.SEPARATOR, "___"),
      FullLineBlock(MarkdownBlockType.SEPARATOR, "----"),
      FullLineBlock(MarkdownBlockType.SEPARATOR, "-----"),
      FullLineBlock(MarkdownBlockType.SEPARATOR, "------"),
      FullLineBlock(MarkdownBlockType.SEPARATOR, "-------"),

      SingleLineStartBlock(MarkdownBlockType.CHECKLIST_UNCHECKED, "[] "),
      SingleLineStartBlock(MarkdownBlockType.CHECKLIST_UNCHECKED, "[ ] "),

      SingleLineStartBlock(MarkdownBlockType.CHECKLIST_CHECKED, "[x] "),
      SingleLineStartBlock(MarkdownBlockType.CHECKLIST_CHECKED, "[X] "),
    )
  }
}