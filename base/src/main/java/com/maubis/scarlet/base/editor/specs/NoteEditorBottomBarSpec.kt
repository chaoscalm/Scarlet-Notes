package com.maubis.scarlet.base.editor.specs

import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EmptyComponent
import com.facebook.litho.widget.HorizontalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.common.specs.bottomBarCard
import com.maubis.scarlet.base.common.specs.bottomBarRoundIcon
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.MarkdownFormatting
import com.maubis.scarlet.base.editor.sheet.MarkdownHelpBottomSheet

enum class NoteEditorBottomBarType {
  COMMON_FORMATTING,
  ALL_FORMATTING,
  ALL_BLOCKS,
  OPTIONS,
}

@LayoutSpec
object NoteEditorBottomBarSpec {

  @OnCreateInitialState
  fun onCreateInitialState(state: StateValue<NoteEditorBottomBarType>) {
    state.set(NoteEditorBottomBarType.COMMON_FORMATTING)
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
      NoteEditorBottomBarType.COMMON_FORMATTING ->
        NoteEditorCommonFormattingBottomBar.create(context)
          .colorConfig(colorConfig)
          .flexGrow(1f)
          .toggleButtonClick(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.ALL_FORMATTING))
      NoteEditorBottomBarType.ALL_BLOCKS ->
        HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(
            NoteEditorAllBlocksBottomBar.create(context)
              .colorConfig(colorConfig)
              .blockSelectedEventHandler(NoteEditorBottomBar.onBlockSelected(context))
          )
      NoteEditorBottomBarType.ALL_FORMATTING ->
        HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(NoteEditorAllFormattingBottomBar.create(context).colorConfig(colorConfig))
      NoteEditorBottomBarType.OPTIONS ->
        NoteEditorOptionsBottomBar.create(context)
            .colorConfig(colorConfig)
            .flexGrow(1f)
    }
    row.child(content)

    val extraRoundIcon = bottomBarRoundIcon(context, colorConfig)
        .onClick { }
        .isClickDisabled(true)
    val icon = when (state) {
      NoteEditorBottomBarType.COMMON_FORMATTING -> extraRoundIcon
          .iconRes(R.drawable.ic_add_white_24dp)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.ALL_BLOCKS))
      NoteEditorBottomBarType.ALL_BLOCKS -> extraRoundIcon
          .marginDip(YogaEdge.HORIZONTAL, 4f)
          .iconRes(R.drawable.ic_close_white_48dp)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_FORMATTING))
      NoteEditorBottomBarType.ALL_FORMATTING -> extraRoundIcon
          .marginDip(YogaEdge.HORIZONTAL, 4f)
          .iconRes(R.drawable.ic_close_white_48dp)
          .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_FORMATTING))
      NoteEditorBottomBarType.OPTIONS -> EmptyComponent.create(context)
    }
    row.child(icon)

    val moreIcon = when (state) {
      NoteEditorBottomBarType.COMMON_FORMATTING, NoteEditorBottomBarType.OPTIONS ->
        bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_more_options)
            .onClick { }
            .isClickDisabled(true)
            .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.OPTIONS))
      else -> EmptyComponent.create(context)
    }
    row.child(moreIcon)
    return bottomBarCard(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onStateChangeClick(
      context: ComponentContext,
      @State state: NoteEditorBottomBarType,
      @Param nextState: NoteEditorBottomBarType) {
    if (state == NoteEditorBottomBarType.OPTIONS && nextState == NoteEditorBottomBarType.OPTIONS) {
      NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.COMMON_FORMATTING)
      return
    }
    NoteEditorBottomBar.onStateChange(context, nextState)
  }

  @OnEvent(BlockSelectedEvent::class)
  fun onBlockSelected(context: ComponentContext, @FromEvent formatType: FormatType) {
    (context.androidContext as EditNoteActivity).addEmptyItemAtFocused(formatType)
    NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.COMMON_FORMATTING)
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
            .iconRes(R.drawable.icon_markdown_help)
            .onClick { openSheet(activity, MarkdownHelpBottomSheet()) })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_undo_history)
            .onClick { activity.onHistoryClick(true) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .bgColor(activity.note().color)
            .bgAlpha(255)
            .iconRes(R.drawable.ic_empty)
            .onClick { activity.onColorChangeClick() }
            .showBorder(true)
            .iconMarginHorizontalRes(R.dimen.toolbar_round_small_icon_margin_horizontal)
            .iconSizeRes(R.dimen.toolbar_round_small_icon_size))
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_redo_history)
            .onClick { activity.onHistoryClick(false) })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .build()
  }
}

@LayoutSpec
object NoteEditorCommonFormattingBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop colorConfig: ToolbarColorConfig,
      @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as EditNoteActivity
    return Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_markdown_bold)
            .onClick { activity.triggerMarkdown(MarkdownFormatting.BOLD) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_markdown_italics)
            .onClick { activity.triggerMarkdown(MarkdownFormatting.ITALICS) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_markdown_underline)
            .onClick { activity.triggerMarkdown(MarkdownFormatting.UNDERLINE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownFormatting.UNORDERED) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_more_horiz_white_48dp)
            .onClick { }
            .isClickDisabled(true)
            .clickHandler(toggleButtonClick))
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
        .iconRes(R.drawable.ic_markdown_bold)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.BOLD) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_markdown_italics)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.ITALICS) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_markdown_underline)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.UNDERLINE) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.UNORDERED) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_code_white_48dp)
        .onClick { activity.triggerMarkdown(MarkdownFormatting.CODE) })
      .child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_markdown_strikethrough)
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
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title_white_48dp)
            .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.SUB_HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_subject_white_48dp)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.TEXT) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_check_box_white_24dp)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_format_quote_white_48dp)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.QUOTE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.icon_code_block)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.CODE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_image_gallery)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.IMAGE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_format_separator)
            .onClick { NoteEditorAllBlocksBottomBar.dispatchBlockSelectedEvent(handler, FormatType.SEPARATOR) })
        .build()
  }
}
