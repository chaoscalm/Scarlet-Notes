package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.specs.GridSectionItem
import com.maubis.scarlet.base.common.specs.GridSectionView

abstract class GridActionsBottomSheet : LithoBottomSheet() {

  abstract fun titleRes(): Int?
  abstract fun getItems(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem>

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)

    val title = titleRes()
    if (title != null) {
      column.child(getLithoBottomSheetTitle(componentContext)
        .marginDip(YogaEdge.BOTTOM, 12f)
        .textRes(title))
    } else {
      column.child(EmptySpec.create(componentContext)
        .widthPercent(100f)
        .heightDip(8f))
    }

    val items = getItems(componentContext, dialog)
    var index = 0
    items.forEach {
      index++
      column.child(
        GridSectionView.create(componentContext)
          .marginDip(YogaEdge.HORIZONTAL, 12f)
          .marginDip(YogaEdge.VERTICAL, 8f)
          .iconSizeRes(R.dimen.primary_round_icon_size)
          .showSeparator(index != items.size)
          .section(it))
    }

    return column.build()
  }
}
