package com.maubis.scarlet.base.note.tag

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.databinding.BottomSheetCreateEditTagBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateOrEditTagBottomSheet : ThemedBottomSheetFragment() {
  private val tag: Tag by lazy {
    val tagUuid = UUID.fromString(requireArguments().getString(KEY_TAG_UUID))
    data.tags.getByUUID(tagUuid) ?: throw IllegalArgumentException("Invalid tag UUID")
  }
  private var onTagSaveListener: (Tag) -> Unit = { _ -> }

  private lateinit var views: BottomSheetCreateEditTagBinding

  override fun setupDialogViews(dialog: Dialog) {
    setAlwaysExpanded(dialog)

    setupTitle()
    setupNameTextField()
    setupSaveButton()
    setupRemoveButton()
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun inflateLayout(): View {
    views = BottomSheetCreateEditTagBinding.inflate(layoutInflater)
    return views.root
  }

  private fun setupTitle() {
    views.dialogTitle.setTextColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
    views.dialogTitle.setText(if (tag.isPersisted()) R.string.tag_sheet_edit_title else R.string.tag_sheet_create_title)
    views.dialogTitle.typeface = appTypeface.title()
  }

  private fun setupNameTextField() {
    views.tagNameTextField.setTextColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
    views.tagNameTextField.setHintTextColor(appTheme.getColor(ThemeColor.HINT_TEXT))
    views.tagNameTextField.typeface = appTypeface.text()
    views.tagNameTextField.setText(tag.title)
    views.tagNameTextField.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        updateTagName(views.tagNameTextField.text.toString())
        dismiss()
        return@getEditorActionListener true
      }))
  }

  private fun setupSaveButton() {
    views.saveButton.setOnClickListener {
      updateTagName(views.tagNameTextField.text.toString())
      dismiss()
    }
  }

  private fun setupRemoveButton() {
    views.removeButton.isVisible = tag.isPersisted()
    views.removeButton.setOnClickListener {
      tag.delete()
      removeDeletedTagFromAllNotes(tag)
      onTagSaveListener(tag)
      dismiss()
    }
  }

  private fun updateTagName(title: String) {
    if (title.isBlank()) {
      return
    }
    tag.title = title
    tag.save()
    onTagSaveListener(tag)
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

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card)

  companion object {
    const val KEY_TAG_UUID = "tag_uuid"

    fun openSheet(activity: ThemedActivity, tag: Tag, listener: (Tag) -> Unit) {
      val sheet = CreateOrEditTagBottomSheet()
      sheet.arguments = Bundle().apply { putString(KEY_TAG_UUID, tag.uuid.toString()) }
      sheet.onTagSaveListener = listener
      sheet.show(activity.supportFragmentManager, sheet.getTag())
    }
  }
}