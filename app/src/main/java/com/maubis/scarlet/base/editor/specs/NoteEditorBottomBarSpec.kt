package com.maubis.scarlet.base.editor.specs

import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.HorizontalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.common.specs.bottomBar
import com.maubis.scarlet.base.common.specs.bottomBarRoundIcon
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.MarkdownFormatting
import com.maubis.scarlet.base.editor.sheet.MarkdownHelpBottomSheet

enum class NoteEditorBottomBarType {
  FORMATTING,
  BLOCKS,
  OPTIONS,
}

@LayoutSpec
object NoteEditorBottomBarSpec {

  @OnCreateInitialState
  fun onCreateInitialState(state: StateValue<NoteEditorBottomBarType>) {
    state.set(NoteEditorBottomBarType.FORMATTING)
  }

  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop colorConfig: ToolbarColorConfig,
      @State state: NoteEditorBottomBarType): Component {
    val row = Row.create(context)
        .widthPercent(100f)
        .paddingDip(YogaEdge.HORIZONTAL, 4f)
        .alignItems(YogaAlign.CENTER)

    val content = when (state) {
      NoteEditorBottomBarType.BLOCKS ->
        HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(
            NoteEditorAllBlocksBottomBar.create(context)
              .colorConfig(colorConfig)
              .blockSelectedEventHandler(NoteEditorBottomBar.onBlockSelected(context))
          )
      NoteEditorBottomBarType.FORMATTING ->
        HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(NoteEditorAllFormattingBottomBar.create(context).colorConfig(colorConfig))
      NoteEditorBottomBarType.OPTIONS ->
        NoteEditorOptionsBottomBar.create(context)
            .colorConfig(colorConfig)
            .flexGrow(1f)
    }
    row.child(content)

    if (state == NoteEditorBottomBarType.FORMATTING) {
      row.child(
        bottomBarRoundIcon(context, colorConfig)
          .marginDip(YogaEdge.START, 8f)
          .iconRes(R.drawable.ic_add)
          .flexShrink(0f)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.BLOCKS))
      )
    } else if (state == NoteEditorBottomBarType.BLOCKS) {
      row.child(
        bottomBarRoundIcon(context, colorConfig)
          .marginDip(YogaEdge.START, 8f)
          .iconRes(R.drawable.ic_close)
          .flexShrink(0f)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.FORMATTING))
      )
    }

    if (state == NoteEditorBottomBarType.FORMATTING || state == NoteEditorBottomBarType.OPTIONS) {
      row.child(
        bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_more_options)
          .flexShrink(0f)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.OPTIONS))
      )
    }
    return bottomBar(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onStateChangeClick(
      context: ComponentContext,
      @State state: NoteEditorBottomBarType,
      @Param nextState: NoteEditorBottomBarType) {
    if (state == NoteEditorBottomBarType.OPTIONS && nextState == NoteEditorBottomBarType.OPTIONS) {
      NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.FORMATTING)
      return
    }
    NoteEditorBottomBar.onStateChange(context, nextState)
  }

  @OnEvent(BlockSelectedEvent::class)
  fun onBlockSelected(context: ComponentContext, @FromEvent formatType: FormatType) {
    (context.androidContext as EditNoteActivity).addEmptyItemAtFocused(formatType)
    NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.FORMATTING)
  }

  @OnUpdateState
  fun onStateChange(state: StateValue<NoteEditorBottomBarType>, @Param nextState: NoteEditorBottomBarType) {
    state.set(nextState)
  }
}

@LayoutSpec
object NoteEditorOptionsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as EditNoteActivity
    return Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_help)
            .onClick { openSheet(activity, MarkdownHelpBottomSheet()) })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_undo)
            .onClick { activity.performUndo() })
        .child(bottomBarRoundIcon(context, colorConfig)
            .bgColor(activity.note().color)
            .bgAlpha(255)
            .iconRes(R.drawable.ic_empty)
            .onClick { activity.onColorChangeClick() }
            .showBorder(true)
            .iconMarginHorizontalDip(8f)
            .iconSizeDip(28f))
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_redo)
            .onClick { activity.performRedo() })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .build()
  }
}

@LayoutSpec
object NoteEditorAllFormattingBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as EditNoteActivity
    return Row.create(context)
      .alignSelf(YogaAlign.CENTER)
      .alignItems(YogaAlign.CENTER)
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_text_bold)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.BOLD) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_text_italic)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.ITALICS) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_text_underlined)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.UNDERLINE) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_bulleted_list)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.UNORDERED) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_code_inline)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.CODE) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_text_strikethrough)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.STRIKE_THROUGH) })
      .build()
  }
}

@Event
class BlockSelectedEvent {
  lateinit var formatType: FormatType
}

@LayoutSpec(events = [BlockSelectedEvent::class])
object NoteEditorAllBlocksBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop colorConfig: ToolbarColorConfig): Component {
    val handler = NoteEditorAllBlocksBottomBar.getBlockSelectedEventHandler(context) ?:
      throw IllegalArgumentException("The BlockSelectedEvent requires an event handler")
    return Row.create(context)
        .alignSelf(YogaAlign.CENTER)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title)
            .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.SUB_HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_text_content)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.TEXT) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_checkbox)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_quote)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.QUOTE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_code_block)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.CODE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_image)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.IMAGE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_separator)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.SEPARATOR) })
        .build()
  }
}
