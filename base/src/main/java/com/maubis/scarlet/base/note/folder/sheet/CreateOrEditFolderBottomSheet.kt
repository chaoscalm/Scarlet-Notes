package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.ui.ColorView
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.database.entities.Folder

class CreateOrEditFolderBottomSheet : ThemedBottomSheetFragment() {
  private lateinit var folder: Folder
  private var onFolderSaveListener: (Folder) -> Unit = { _ -> }

  private var selectedColor: Int = 0

  override fun setupDialogViews(dialog: Dialog) {
    setAlwaysExpanded(dialog)

    setupTitle(dialog)
    val folderNameTextField = setupNameTextField(dialog)
    setupSaveButton(dialog, folderNameTextField)
    setupDeleteButton(dialog)

    selectedColor = folder.color
    setColorsList(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setupTitle(dialog: Dialog) {
    val title = dialog.findViewById<TextView>(R.id.options_title)
    title.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    title.typeface = appTypeface.title()
    title.setText(if (folder.isNotPersisted()) R.string.folder_sheet_add_note else R.string.folder_sheet_edit_note)
  }

  private fun setupNameTextField(dialog: Dialog): EditText {
    val folderNameTextField = dialog.findViewById<EditText>(R.id.enter_folder)
    folderNameTextField.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    folderNameTextField.setHintTextColor(appTheme.get(ThemeColorType.HINT_TEXT))
    folderNameTextField.typeface = appTypeface.text()
    folderNameTextField.setText(folder.title)
    folderNameTextField.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        saveFolder(folderNameTextField.text.toString())
        onFolderSaveListener(folder)
        dismiss()
        return@getEditorActionListener true
      }))
    return folderNameTextField
  }

  private fun setupSaveButton(dialog: Dialog, folderNameTextField: EditText) {
    val saveButton = dialog.findViewById<TextView>(R.id.action_button)
    saveButton.setOnClickListener {
      saveFolder(folderNameTextField.text.toString())
      onFolderSaveListener(folder)
      dismiss()
    }
  }

  private fun setupDeleteButton(dialog: Dialog) {
    val deleteButton = dialog.findViewById<TextView>(R.id.action_remove_button)
    deleteButton.visibility = if (folder.isNotPersisted()) GONE else VISIBLE
    deleteButton.setOnClickListener {
      openSheet(context as AppCompatActivity, DeleteFolderBottomSheet().apply {
        selectedFolder = folder
        onDeletionListener = onFolderSaveListener
      })
      dismiss()
    }
  }

  private fun saveFolder(title: String) {
    if (title.isBlank())
      return

    folder.title = title
    folder.color = selectedColor
    folder.updateTimestamp = System.currentTimeMillis()
    folder.save()
  }

  private fun setColorsList(dialog: Dialog) {
    val colorSelectorLayout = dialog.findViewById<FlexboxLayout>(R.id.color_flexbox)
    colorSelectorLayout.removeAllViews()
    val colors = requireContext().resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(requireContext())
      item.setColor(color, selectedColor == color)
      item.setOnClickListener {
        selectedColor = color
        setColorsList(dialog)
      }
      colorSelectorLayout.addView(item)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_folder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card, R.id.core_color_card)

  companion object {
    fun openSheet(activity: ThemedActivity, folder: Folder, listener: (Folder) -> Unit) {
      val sheet = CreateOrEditFolderBottomSheet()
      sheet.folder = folder
      sheet.onFolderSaveListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}