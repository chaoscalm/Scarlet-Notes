package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.common.specs.CounterChooser
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.home.MainActivity

class FontSizeBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.note_option_font_size)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeDip(ScarletApp.prefs.editorTextSize.toFloat())
          .marginDip(YogaEdge.BOTTOM, 16f)
          .typeface(appTypeface.text())
          .textRes(R.string.note_option_font_size_example)
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(CounterChooser.create(componentContext)
               .value(ScarletApp.prefs.editorTextSize)
               .minValue(12)
               .maxValue(24)
               .onValueChange { value ->
                 ScarletApp.prefs.editorTextSize = value
                 refresh(activity, dialog)
               }
               .paddingDip(YogaEdge.VERTICAL, 16f))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.action_done)
               .onPrimaryClick {
                 dismiss()
               }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}