package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.common.specs.CounterChooser
import com.maubis.scarlet.base.home.MainActivity

class LineCountBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.note_option_number_lines)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(CounterChooser.create(componentContext)
               .value(ScarletApp.prefs.notePreviewLines)
               .minValue(2)
               .maxValue(15)
               .onValueChange { value ->
                 ScarletApp.prefs.notePreviewLines = value
                 refresh(activity, dialog)
                 activity.notifyAdapterExtraChanged()
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