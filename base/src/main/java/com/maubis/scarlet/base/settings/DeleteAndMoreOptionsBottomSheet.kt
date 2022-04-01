package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.lifecycle.lifecycleScope
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.home.sheets.openDeleteAllXSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteAndMoreOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_delete_notes_and_more

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_delete_all_notes,
      subtitle = R.string.home_option_delete_all_notes_details,
      icon = R.drawable.ic_note_white_48dp,
      listener = {
        openDeleteAllXSheet(activity, R.string.home_option_delete_all_notes_details) {
          activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) { data.notes.getAll().forEach { it.delete(activity) } }
            activity.resetAndLoadData()
            dismiss()
          }
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_delete_all_tags,
      subtitle = R.string.home_option_delete_all_tags_details,
      icon = R.drawable.ic_action_tags,
      listener = {
        openDeleteAllXSheet(activity, R.string.home_option_delete_all_tags_details) {
          activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) { data.tags.getAll().forEach { it.delete() } }
            activity.resetAndLoadData()
            dismiss()
          }
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_delete_all_folders,
      subtitle = R.string.home_option_delete_all_folders_details,
      icon = R.drawable.ic_folder,
      listener = {
        openDeleteAllXSheet(activity, R.string.home_option_delete_all_folders_details) {
          activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) { data.folders.getAll().forEach { it.delete() } }
            activity.resetAndLoadData()
            dismiss()
          }
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_delete_everything,
      subtitle = R.string.home_option_delete_everything_details,
      icon = R.drawable.ic_delete_permanently,
      listener = {
        openDeleteAllXSheet(activity, R.string.home_option_delete_everything_details) {
          activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
              data.notes.getAll().forEach { it.delete(activity) }
              data.tags.getAll().forEach { it.delete() }
              data.folders.getAll().forEach { it.delete() }
            }

            activity.resetAndLoadData()
            dismiss()
          }
        }

      }
    ))
    return options
  }
}