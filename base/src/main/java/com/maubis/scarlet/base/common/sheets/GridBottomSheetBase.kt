package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment

abstract class GridBottomSheetBase : ThemedBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    setupViewWithDialog(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.grid_card)

  override fun getLayout(): Int = R.layout.bottom_sheet_grid_layout
}