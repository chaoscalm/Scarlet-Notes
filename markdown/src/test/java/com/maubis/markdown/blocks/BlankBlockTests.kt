package com.maubis.markdown.blocks

import org.junit.Test

class BlankBlockTests : MarkdownBlockParserTestSuite() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.NORMAL, text)), processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testBlankText() {
    val text = "     "
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.NORMAL, text)), processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testJustNewlinesText() {
    val text = "\n\n\n"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(
        listOf(testBlock(MarkdownBlockType.NORMAL, "\n\n\n")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testJustNewlineAndBlanksText() {
    val text = "   \n  \n \n    "
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(
        listOf(testBlock(MarkdownBlockType.NORMAL, text)),
        processed)
    assertBlocksContainText(processed, text)
  }
}
