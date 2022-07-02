package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionsItem
import com.maubis.scarlet.base.common.utils.SortingTechnique
import com.maubis.scarlet.base.home.MainActivity

class SortingOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  override fun title(): Int = R.string.sort_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> {
    val options = ArrayList<LithoChooseOptionsItem>()
    SortingTechnique.values().forEach { technique ->
      options.add(
        LithoChooseOptionsItem(
          title = technique.label,
          listener = {
            ScarletApp.prefs.notesSortingTechnique = technique
            (activity as? MainActivity)?.refreshList()
            refresh(componentContext.androidContext, dialog)
          },
          selected = ScarletApp.prefs.notesSortingTechnique == technique
        ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = SortingOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}