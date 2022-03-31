package com.maubis.scarlet.base.note.creation.specs

import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EmptyComponent
import com.facebook.litho.widget.HorizontalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownFormatting
import com.maubis.scarlet.base.note.copy
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.creation.sheet.MarkdownHelpBottomSheet
import com.maubis.scarlet.base.note.share
import com.maubis.scarlet.base.settings.sEditorMarkdownDefault
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.bottomBarCard
import com.maubis.scarlet.base.support.specs.bottomBarRoundIcon

enum class NoteEditorBottomBarType {
  COMMON_BLOCKS,
  COMMON_MARKDOWNS,
  ALL_BLOCKS,
  ALL_MARKDOWNS,
  OPTIONS,
}

@LayoutSpec
object NoteEditorBottomBarSpec {

  @OnCreateInitialState
  fun onCreateInitialState(state: StateValue<NoteEditorBottomBarType>) {
    state.set(if (sEditorMarkdownDefault) NoteEditorBottomBarType.COMMON_MARKDOWNS else NoteEditorBottomBarType.COMMON_BLOCKS)
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
      NoteEditorBottomBarType.COMMON_BLOCKS ->
        NoteEditorBlocksBottomBar.create(context)
          .colorConfig(colorConfig)
          .flexGrow(1f)
          .toggleButtonClick(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.ALL_BLOCKS))
      NoteEditorBottomBarType.COMMON_MARKDOWNS -> NoteEditorMarkdownsBottomBar.create(context)
        .colorConfig(colorConfig)
        .flexGrow(1f)
        .toggleButtonClick(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.ALL_MARKDOWNS))
      NoteEditorBottomBarType.ALL_BLOCKS -> HorizontalScroll.create(context)
        .flexGrow(1f)
        .contentProps(NoteEditorAllBlocksBottomBar.create(context).colorConfig(colorConfig))
      NoteEditorBottomBarType.ALL_MARKDOWNS -> HorizontalScroll.create(context)
        .flexGrow(1f)
        .contentProps(NoteEditorAllMarkdownsBottomBar.create(context).colorConfig(colorConfig))
      NoteEditorBottomBarType.OPTIONS ->
        NoteEditorOptionsBottomBar.create(context)
          .colorConfig(colorConfig)
          .flexGrow(1f)
    }
    row.child(content)

    val extraRoundIcon = bottomBarRoundIcon(context, colorConfig)
      .bgColor(Color.TRANSPARENT)
      .onClick { }
      .isClickDisabled(true)
    val icon = when (state) {
      NoteEditorBottomBarType.COMMON_BLOCKS -> extraRoundIcon
        .iconRes(R.drawable.ic_markdown_logo)
        .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_MARKDOWNS))
      NoteEditorBottomBarType.COMMON_MARKDOWNS -> extraRoundIcon
        .iconRes(R.drawable.ic_add_white_24dp)
        .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_BLOCKS))
      NoteEditorBottomBarType.ALL_BLOCKS -> extraRoundIcon
        .marginDip(YogaEdge.HORIZONTAL, 4f)
        .iconRes(R.drawable.ic_close_white_48dp)
        .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_BLOCKS))
      NoteEditorBottomBarType.ALL_MARKDOWNS -> extraRoundIcon
        .marginDip(YogaEdge.HORIZONTAL, 4f)
        .iconRes(R.drawable.ic_close_white_48dp)
        .clickHandler(NoteEditorBottomBar.onStateChangeClick(context, NoteEditorBottomBarType.COMMON_MARKDOWNS))
      NoteEditorBottomBarType.OPTIONS -> EmptyComponent.create(context)
    }
    row.child(icon)

    val moreIcon = when (state) {
      NoteEditorBottomBarType.COMMON_MARKDOWNS, NoteEditorBottomBarType.COMMON_BLOCKS, NoteEditorBottomBarType.OPTIONS ->
        bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_more_options)
          .bgColor(Color.TRANSPARENT)
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
      NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.COMMON_BLOCKS)
      return
    }
    if (state == NoteEditorBottomBarType.ALL_MARKDOWNS && nextState == NoteEditorBottomBarType.COMMON_BLOCKS) {
      NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.ALL_BLOCKS)
      return
    }
    if (state == NoteEditorBottomBarType.ALL_BLOCKS && nextState == NoteEditorBottomBarType.COMMON_MARKDOWNS) {
      NoteEditorBottomBar.onStateChange(context, NoteEditorBottomBarType.ALL_MARKDOWNS)
      return
    }
    NoteEditorBottomBar.onStateChange(context, nextState)
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
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
      .alignItems(YogaAlign.CENTER)
      .child(bottomBarRoundIcon(context, colorConfig)
               .bgColor(Color.TRANSPARENT)
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
object NoteEditorBlocksBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig,
    @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
      .alignItems(YogaAlign.CENTER)
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_title_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_subject_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_check_box_white_24dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_format_quote_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.QUOTE) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_more_horiz_white_48dp)
               .onClick { }
               .isClickDisabled(true)
               .clickHandler(toggleButtonClick))
      .build()
  }
}

@LayoutSpec
object NoteEditorMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig,
    @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
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
object NoteEditorAllBlocksBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
      .alignSelf(YogaAlign.CENTER)
      .alignItems(YogaAlign.CENTER)
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_title_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_title_white_48dp)
               .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
               .onClick { activity.addEmptyItemAtFocused(FormatType.SUB_HEADING) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_subject_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_check_box_white_24dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_format_quote_white_48dp)
               .onClick { activity.addEmptyItemAtFocused(FormatType.QUOTE) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.icon_code_block)
               .onClick { activity.addEmptyItemAtFocused(FormatType.CODE) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_image_gallery)
               .onClick { activity.addEmptyItemAtFocused(FormatType.IMAGE) })
      .child(bottomBarRoundIcon(context, colorConfig)
               .iconRes(R.drawable.ic_format_separator)
               .onClick { activity.addEmptyItemAtFocused(FormatType.SEPARATOR) })
      .build()
  }
}

@LayoutSpec
object NoteEditorAllMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as CreateNoteActivity
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

@LayoutSpec
object NoteViewBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as ViewAdvancedNoteActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .bgColor(Color.TRANSPARENT)
                .iconRes(R.drawable.ic_apps_white_48dp)
                .onClick { activity.openMoreOptions() })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.icon_delete)
                .onClick { activity.moveItemToTrashOrDelete(activity.note()) })
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_content_copy_white_48dp)
                .onClick { activity.note().copy(activity) })
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_share_white_48dp)
                .onClick { activity.note().share(activity) })


    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
    row.child(bottomBarRoundIcon(context, colorConfig)
                .bgColor(Color.TRANSPARENT)
                .iconRes(R.drawable.ic_edit_white_48dp)
                .onClick { activity.openEditor() })
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}
