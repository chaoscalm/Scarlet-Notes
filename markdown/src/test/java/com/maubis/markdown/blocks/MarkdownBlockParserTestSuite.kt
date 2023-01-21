package com.maubis.markdown.blocks

import org.junit.Assert

abstract class MarkdownBlockParserTestSuite {
  protected fun assertBlocksAreEqual(expectedBlocks: List<MarkdownBlock>, processedBlocks: List<MarkdownBlock>) {
    Assert.assertEquals(
        "Error: Lists must have same size\n" +
            "${toString(expectedBlocks)} vs ${toString(processedBlocks)}",
        expectedBlocks.size, processedBlocks.size)
    expectedBlocks.forEachIndexed { index, expectedBlock ->
      val processedBlock = processedBlocks[index]
      Assert.assertEquals(
          "Error: Types must be the same\n" +
              "${toString(expectedBlocks)} vs ${toString(processedBlocks)}",
          expectedBlock.type, processedBlock.type)
      Assert.assertEquals(
          "Error: Text must be the same\n" +
              "${toString(expectedBlocks)} vs ${toString(processedBlocks)}",
          expectedBlock.text(), processedBlock.text())
      Assert.assertNotEquals(
          "Error: Type should never be INVALID\n" +
              "${toString(expectedBlocks)} vs ${toString(processedBlocks)}",
          MarkdownBlockType.INVALID, processedBlock.type)
    }
  }

  protected fun assertBlocksContainText(blocks: List<MarkdownBlock>, text: String) {
    val string = StringBuilder()
    blocks.forEachIndexed { index, block ->
      string.append(block.text())
      if (index != blocks.size - 1) {
        string.append("\n")
      }
    }

    Assert.assertEquals(text, string.toString())
  }

  protected fun testBlock(type: MarkdownBlockType, text: String): MarkdownBlock {
    return NormalMarkdownBlock(type, text)
  }

  private fun toString(blocks: List<MarkdownBlock>): String {
    val builder = StringBuilder()
    builder.append("{")
    blocks.forEach {
      builder.append("(")
      builder.append(it.type.name)
      builder.append(",")
      builder.append(it.text())
      builder.append(")")
    }
    builder.append("}")
    return builder.toString()
  }
}
