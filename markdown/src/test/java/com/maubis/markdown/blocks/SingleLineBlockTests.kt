package com.maubis.markdown.blocks

import org.junit.Test

class SingleLineBlockTests : MarkdownBlockParserTestSuite() {
  @Test
  fun testSingleLineText() {
    val text = "test string"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.NORMAL, text)), processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testSingleLineHeadings() {
    val textH1 = "# heading 1"
    val processedH1 = MarkdownBlockParser(textH1).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.HEADING_1, textH1)), processedH1)
    assertBlocksContainText(processedH1, textH1)

    val textH2 = "## heading 2"
    val processedH2 = MarkdownBlockParser(textH2).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.HEADING_2, textH2)), processedH2)
    assertBlocksContainText(processedH2, textH2)

    val textH3 = "### heading 3"
    val processedH3 = MarkdownBlockParser(textH3).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.HEADING_3, textH3)), processedH3)
    assertBlocksContainText(processedH3, textH3)

    val textH4 = "#### normal"
    val processedH4 = MarkdownBlockParser(textH4).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.NORMAL, textH4)), processedH4)
    assertBlocksContainText(processedH4, textH4)
  }

  @Test
  fun testSingleLineBullets() {
    val textB1 = "- bullet"
    val processedB1 = MarkdownBlockParser(textB1).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.BULLET_1, textB1)), processedB1)
    assertBlocksContainText(processedB1, textB1)

    val textB2 = "  - bullet"
    val processedB2 = MarkdownBlockParser(textB2).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.BULLET_2, textB2)), processedB2)
    assertBlocksContainText(processedB2, textB2)

    val textB3 = "     - bullet"
    val processedB3 = MarkdownBlockParser(textB3).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.BULLET_3, textB3)), processedB3)
    assertBlocksContainText(processedB3, textB3)
  }

  @Test
  fun testSingleLineQuote() {
    val text = "> quote"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.QUOTE, text)), processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testSingleLineCode() {
    val text = "```"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.CODE, text)), processed)
    assertBlocksContainText(processed, text)
  }
}
