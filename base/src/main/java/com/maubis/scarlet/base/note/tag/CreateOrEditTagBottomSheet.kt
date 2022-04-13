package com.maubis.scarlet.base.note.tag

import android.app.Dialog
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.database.entities.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateOrEditTagBottomSheet : ThemedBottomSheetFragment() {

  var selectedTag: Tag? = null
  var sheetOnTagListener: (Tag) -> Unit = { _ -> }

  override fun setupDialogViews(dialog: Dialog) {
    val tag = selectedTag
    if (tag == null) {
      dismiss()
      return
    }

    val title = dialog.findViewById<TextView>(R.id.options_title)
    title.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    title.setText(if (tag.isNotPersisted()) R.string.tag_sheet_create_title else R.string.tag_sheet_edit_title)
    title.typeface = appTypeface.title()

    val enterTag = dialog.findViewById<EditText>(R.id.enter_tag)
    enterTag.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    enterTag.setHintTextColor(appTheme.get(ThemeColorType.HINT_TEXT))
    enterTag.typeface = appTypeface.text()

    val action = dialog.findViewById<TextView>(R.id.action_button)
    action.setOnClickListener {
      onActionClick(tag, enterTag.text.toString())
      sheetOnTagListener(tag)
      dismiss()
    }

    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)
    removeBtn.visibility = if (tag.isNotPersisted()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      tag.delete()
      removeDeletedTagFromAllNotes(tag)
      sheetOnTagListener(tag)
      dismiss()
    }
    enterTag.setText(tag.title)
    enterTag.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        onActionClick(tag, enterTag.text.toString())
        sheetOnTagListener(tag)
        dismiss()
        return@getEditorActionListener true
      }))
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun onActionClick(tag: Tag, title: String) {
    if (title.isBlank()) {
      return
    }
    tag.title = title
    tag.save()
  }

  private fun removeDeletedTagFromAllNotes(tag: Tag) {
    val appContext = requireContext().applicationContext
    requireActivity().lifecycleScope.launch(Dispatchers.IO) {
      data.notes.getAll().forEach { note ->
        if (note.tags.contains(tag.uuid)) {
          note.tags.remove(tag.uuid)
          note.save(appContext)
        }
      }
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_or_edit_tag

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card)

  companion object {
    fun openSheet(activity: ThemedActivity, tag: Tag, listener: (Tag) -> Unit) {
      val sheet = CreateOrEditTagBottomSheet()
      sheet.selectedTag = tag
      sheet.sheetOnTagListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}