package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import com.facebook.litho.ComponentContext

class GenericOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  var title: Int = 0
  var options: List<LithoChooseOptionsItem> = emptyList()

  override fun title(): Int = title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> = options
}