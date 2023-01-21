package com.maubis.markdown.blocks

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleMultiLineBlockTests : MarkdownBlockParserTestSuite() {
  @Test
  fun testMultiLineText() {
    val text = "text\n" +
        " text\n" +
        "\n" +
        "text"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.NORMAL, "text\n text\n\ntext")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testMultipleHeadlines() {
    val text = "# text\n" +
        "## text\n" +
        "### text\n" +
        "text\n"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.HEADING_1, "# text"),
        testBlock(MarkdownBlockType.HEADING_2, "## text"),
        testBlock(MarkdownBlockType.HEADING_3, "### text"),
        testBlock(MarkdownBlockType.NORMAL, "text\n")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testMultipleBullets() {
    val text = "- text\n" +
        "- text\n" +
        "  - text\n" +
        "    - text\n" +
        "text\n"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.BULLET_1, "- text"),
        testBlock(MarkdownBlockType.BULLET_1, "- text"),
        testBlock(MarkdownBlockType.BULLET_2, "  - text"),
        testBlock(MarkdownBlockType.BULLET_3, "    - text"),
        testBlock(MarkdownBlockType.NORMAL, "text\n")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testMultipleQuotes() {
    val text = "> text\n" +
        "text\n" +
        "> text\n\n" +
        "> text"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.QUOTE, "> text\ntext"),
        testBlock(MarkdownBlockType.QUOTE, "> text"),
        testBlock(MarkdownBlockType.NORMAL, ""),
        testBlock(MarkdownBlockType.QUOTE, "> text")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testCodeAndQuotes() {
    val text = "## text\n\n" +
        "```\ncode\n```\n\n" +
        "> text\n\n" +
        "- bullet"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.HEADING_2, "## text"),
        testBlock(MarkdownBlockType.NORMAL, ""),
        testBlock(MarkdownBlockType.CODE, "```\ncode\n```"),
        testBlock(MarkdownBlockType.NORMAL, ""),
        testBlock(MarkdownBlockType.QUOTE, "> text"),
        testBlock(MarkdownBlockType.NORMAL, ""),
        testBlock(MarkdownBlockType.BULLET_1, "- bullet")),
        processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testMultilineCode() {
    val text = "```\n" +
        "text\n" +
        "# text\n" +
        "## text\n" +
        "### text\n" +
        "> text\n" +
        "\n" +
        "```"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(testBlock(MarkdownBlockType.CODE, text)), processed)
    assertBlocksContainText(processed, text)
  }

  @Test
  fun testSeparatorCode() {
    val text = "```\n" +
        "code\n" +
        "```\n" +
        "---\n" +
        "## heading"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.CODE, "```\ncode\n```"),
        testBlock(MarkdownBlockType.SEPARATOR, "---"),
        testBlock(MarkdownBlockType.HEADING_2, "## heading")), processed)
    assertBlocksContainText(processed, text)
  }



  @Test
  fun testMarkdownInsideCode() {
    val text = "```\n" +
        "**co** _d_ \\e\n" +
        "```\n" +
        "---\n" +
        "## heading"
    val processed = MarkdownBlockParser(text).parseText()
    assertBlocksAreEqual(listOf(
        testBlock(MarkdownBlockType.CODE, "```\n**co** _d_ \\e\n```"),
        testBlock(MarkdownBlockType.SEPARATOR, "---"),
        testBlock(MarkdownBlockType.HEADING_2, "## heading")), processed)
    assertBlocksContainText(processed, text)
  }

}
