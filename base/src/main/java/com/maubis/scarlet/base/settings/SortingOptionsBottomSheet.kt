package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.core.note.SortingTechnique
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionsItem

class SortingOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  var listener: () -> Unit = {}

  override fun title(): Int = R.string.sort_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> {
    val sorting = getSortingState()
    val options = ArrayList<LithoChooseOptionsItem>()

    SortingTechnique.values().forEach { technique ->
      options.add(
        LithoChooseOptionsItem(
          title = getSortingTechniqueLabel(technique),
          listener = {
            setSortingState(technique)
            listener()
            reset(componentContext.androidContext, dialog)
          },
          selected = sorting == technique
        ))
    }
    return options
  }

  companion object {

    const val KEY_SORTING_TECHNIQUE = "KEY_SORTING_TECHNIQUE"

    fun getSortingState(): SortingTechnique {
      return SortingTechnique.values()[appPreferences.getInt(KEY_SORTING_TECHNIQUE, SortingTechnique.LAST_MODIFIED.ordinal)]
    }

    fun getSortingTechniqueLabel(technique: SortingTechnique): Int {
      return when (technique) {
        SortingTechnique.LAST_MODIFIED -> R.string.sort_sheet_last_modified
        SortingTechnique.NEWEST_FIRST -> R.string.sort_sheet_newest_first
        SortingTechnique.OLDEST_FIRST -> R.string.sort_sheet_oldest_first
        SortingTechnique.ALPHABETICAL -> R.string.sort_sheet_alphabetical
        SortingTechnique.NOTE_COLOR -> R.string.sort_sheet_note_color
        SortingTechnique.NOTE_TAGS -> R.string.sort_sheet_note_tags
      }
    }

    fun setSortingState(sortingTechnique: SortingTechnique) {
      appPreferences.edit { putInt(KEY_SORTING_TECHNIQUE, sortingTechnique.ordinal) }
    }

    fun openSheet(activity: MainActivity, listener: () -> Unit) {
      val sheet = SortingOptionsBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}