package com.maubis.scarlet.base.home.sheets

import android.app.Dialog
import android.content.Intent
import android.util.Log
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType

class ExceptionBottomSheet : LithoBottomSheet() {
  var exception: Exception = RuntimeException()

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.exception_sheet_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.code())
          .textSizeRes(R.dimen.font_size_small)
          .text(Markdown.render("```\n${Log.getStackTraceString(exception)}\n```", true))
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.exception_sheet_share)
               .onPrimaryClick {
                 val intent = Intent(Intent.ACTION_SEND)
                 intent.setType("text/plain");
                 intent.putExtra(Intent.EXTRA_TEXT, Log.getStackTraceString(exception))
                 startActivity(Intent.createChooser(intent, "Thrown exception"))
               }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}