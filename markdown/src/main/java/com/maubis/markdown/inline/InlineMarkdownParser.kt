package com.maubis.markdown.inline

internal class InlineMarkdownParser(val text: String) {

  private var textSegment = NormalInlineSegmentBuilder()
  private var currentInline = InlineSegmentBuilder()
  private val processedSegments = ArrayList<InlineSegmentBuilder>()

  fun parseText(): MarkdownInlineSegment {
    processedSegments.clear()
    processedSegments.add(InlineSegmentBuilder())

    val allInlineTypes = ArrayList<InlineConfig>()
    knownMarkdownInlines.forEach {
      if (it.type != InlineSegmentType.INVALID && it.type != InlineSegmentType.NORMAL) {
        allInlineTypes.add(it)
      }
    }
    allInlineTypes.sortByDescending { it.startIncrement() }

    var index = 0
    while (index < text.length) {
      val char = text[index]

      if (currentInline.config.type == InlineSegmentType.CODE
        && !currentInline.config.isEnd(text, index)) {
        textSegment.builder.append(char)
        index += 1
        continue
      }

      if (currentInline.config.type == InlineSegmentType.IGNORE_CHAR) {
        textSegment.builder.append(char)
        addTextComponent()
        currentInline.paired = true
        index += 1
        unshelveSegment()
        continue
      }

      if (currentInline.config.type != InlineSegmentType.INVALID
        && currentInline.config.isEnd(text, index)) {
        addTextComponent()
        currentInline.paired = true
        index += currentInline.config.endIncrement()
        unshelveSegment()
        continue
      }

      val match = allInlineTypes.firstOrNull { it.isStart(text, index) }
      if (match !== null) {
        addTextComponent()
        shelveSegment()
        currentInline.config = match
        index += match.startIncrement()
        continue
      }

      textSegment.builder.append(char)
      index += 1
    }
    addTextComponent()
    shelveSegment()

    // Now we can have multiple unfinished left if the user did something stupid ;)
    allInlineTypes.forEach {
      while (pairPoorlyPairedConfigs(it)) {
      }
    }
    pairInvalids()

    val result = removeInvalids(currentInline.build())
    if (result is DelimitedInlineSegment && result.children.size == 1) {
      return result.children.first()
    }
    return result
  }

  /**
   * It can be something is not paired because of a user fault like "** something `something **"
   * the ` in the middle will fuck it up
   */
  private fun pairPoorlyPairedConfigs(config: InlineConfig): Boolean {
    val count = processedSegments.count { it.config.identifier == config.identifier }
    if (count <= 1) {
      return false
    }

    var state = 0
    val before = ArrayList<InlineSegmentBuilder>()
    val between = ArrayList<InlineSegmentBuilder>()
    val after = ArrayList<InlineSegmentBuilder>()
    for (segment in processedSegments) {
      if (segment.config.identifier == config.identifier && state == 0) {
        state = 1
        segment.config = PlainInline(InlineSegmentType.INVALID)
        between.add(segment)
        continue
      }

      if (segment.config.identifier == config.identifier && state == 1) {
        segment.config = PlainInline(InlineSegmentType.INVALID)
        after.add(segment)
        state = 2
        continue
      }

      when {
        (state == 0) -> before.add(segment)
        (state == 1) -> between.add(segment)
        (state == 2) -> after.add(segment)
      }
    }

    val current = InlineSegmentBuilder()
    current.config = config
    current.paired = true
    between.forEach {
      val inlineConfig = it.config
      if (inlineConfig is DelimitedInline) {
        current.children.add(NormalInlineSegment(inlineConfig.startDelimiter))
      }
      current.children.addAll(it.children)
    }

    processedSegments.clear()
    processedSegments.addAll(before)
    processedSegments.add(current)
    processedSegments.addAll(after)

    return true
  }

  /**
   * After the pairingPoorly, it is possible to be stuck in a situation where you simply didnt pair.
   * Like "something ** something"
   */
  private fun pairInvalids() {
    currentInline = InlineSegmentBuilder()
    processedSegments.forEach {
      val inlineConfig = it.config
      if (inlineConfig is DelimitedInline && !it.paired) {
        it.children.add(0, NormalInlineSegment(inlineConfig.startDelimiter))
      }

      if (!it.paired || it.config.type == InlineSegmentType.INVALID) {
        currentInline.children.addAll(it.children)
      } else {
        currentInline.children.add(it.build())
      }
    }
    processedSegments.clear()
  }

  /**
   * We are still not done... It might be that end up with INVALIDs inside recursively
   */
  private fun removeInvalids(markdown: MarkdownInlineSegment): MarkdownInlineSegment {
    if (markdown !is DelimitedInlineSegment) {
      return markdown
    }

    val builder = InlineSegmentBuilder()
    builder.config = markdown.config
    markdown.children.forEach {
      val child = removeInvalids(it)
      when {
        child.type == InlineSegmentType.INVALID
            && child is DelimitedInlineSegment -> builder.children.addAll(child.children)
        else -> builder.children.add(child)
      }
    }
    return builder.build()
  }

  private fun addTextComponent() {
    val segment = textSegment.build()
    if (segment.text == "") {
      return
    }
    currentInline.children.add(segment)
    textSegment = NormalInlineSegmentBuilder()
  }

  private fun unshelveSegment() {
    processedSegments.last().children.add(currentInline.build())
    currentInline = processedSegments.last()
    processedSegments.removeAt(processedSegments.size - 1)
  }

  private fun shelveSegment() {
    if (currentInline.config.type == InlineSegmentType.INVALID && currentInline.children.isEmpty()) {
      currentInline = InlineSegmentBuilder()
      return
    }

    processedSegments.add(currentInline)
    currentInline = InlineSegmentBuilder()
  }

  companion object {
    private val knownMarkdownInlines = arrayOf(
      PlainInline(InlineSegmentType.INVALID),
      PlainInline(InlineSegmentType.NORMAL),
      DelimitedInline(InlineSegmentType.BOLD, "**", "**"),
      DelimitedInline(InlineSegmentType.BOLD, "<b>", "</b>"),
      DelimitedInline(InlineSegmentType.BOLD, "__", "__"),
      DelimitedInline(InlineSegmentType.BOLD, "<strong>", "</strong>"),
      DelimitedInline(InlineSegmentType.ITALICS, "*", "*"),
      DelimitedInline(InlineSegmentType.ITALICS, "<em>", "</em>"),
      DelimitedInline(InlineSegmentType.ITALICS, "<i>", "</i>"),
      DelimitedInline(InlineSegmentType.UNDERLINE, "<u>", "</u>"),
      DelimitedInline(InlineSegmentType.UNDERLINE, "_", "_"),
      DelimitedInline(InlineSegmentType.CODE, "<var>", "</var>"),
      DelimitedInline(InlineSegmentType.CODE, "`", "`"),
      DelimitedInline(InlineSegmentType.CODE, "<code>", "</code>"),
      DelimitedInline(InlineSegmentType.STRIKE, "~", "~"),
      DelimitedInline(InlineSegmentType.STRIKE, "~~", "~~"),
      DelimitedInline(InlineSegmentType.STRIKE, "~", "~"),
      StartMarkerInline(InlineSegmentType.IGNORE_CHAR, "\\")
    )
  }
}