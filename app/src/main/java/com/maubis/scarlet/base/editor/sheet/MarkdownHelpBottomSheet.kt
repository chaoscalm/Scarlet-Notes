package com.maubis.scarlet.base.editor.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.ui.ThemeColor

class MarkdownHelpBottomSheet : LithoBottomSheet() {
  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(R.string.markdown_help_sheet_title))

    val examples = arrayOf(
      "# Heading", "## Sub Heading", "```\nblock of code\n```", "> quoted text", "**bold**", "*italics*", "<u>underline</u>", "~~strike through~~",
      "`piece of code`")
    examples.forEach {
      column
        .child(
          Text.create(componentContext)
            .typeface(appTypeface.text())
            .text(Markdown.render(it))
            .textSizeRes(R.dimen.font_size_normal)
            .marginDip(YogaEdge.HORIZONTAL, 20f)
            .paddingDip(YogaEdge.VERTICAL, 4f)
            .textColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT)))
    }

    return column.build()
  }
}