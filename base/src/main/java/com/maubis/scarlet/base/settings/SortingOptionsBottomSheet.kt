package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionsItem
import com.maubis.scarlet.base.common.utils.SortingTechnique
import com.maubis.scarlet.base.home.MainActivity

var notesSortingTechniquePref: SortingTechnique
  get() = SortingTechnique.valueOf(appPreferences.getString("notes_sorting_technique", SortingTechnique.LAST_MODIFIED.name)!!)
  set(value) = appPreferences.edit { putString("notes_sorting_technique", value.name) }

class SortingOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  override fun title(): Int = R.string.sort_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> {
    val options = ArrayList<LithoChooseOptionsItem>()
    SortingTechnique.values().forEach { technique ->
      options.add(
        LithoChooseOptionsItem(
          title = technique.label,
          listener = {
            notesSortingTechniquePref = technique
            (activity as? MainActivity)?.refreshList()
            refresh(componentContext.androidContext, dialog)
          },
          selected = notesSortingTechniquePref == technique
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