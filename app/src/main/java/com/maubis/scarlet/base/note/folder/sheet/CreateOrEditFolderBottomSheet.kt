package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.ui.ColorView
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.databinding.BottomSheetCreateEditFolderBinding
import java.util.*

class CreateOrEditFolderBottomSheet : ThemedBottomSheetFragment() {
  private val folder: Folder by lazy {
    val folderUuid = UUID.fromString(requireArguments().getString(KEY_FOLDER_UUID))
    data.folders.getByUUID(folderUuid) ?: Folder(ScarletApp.prefs.noteDefaultColor)
  }
  private var onFolderSaveListener: (Folder) -> Unit = { _ -> }

  private var selectedColor: Int = 0
  private lateinit var views: BottomSheetCreateEditFolderBinding

  override fun setupDialogViews(dialog: Dialog) {
    selectedColor = folder.color
    setAlwaysExpanded(dialog)

    setupTitle()
    setupNameTextField()
    setupSaveButton()
    setupDeleteButton()
    refreshColorsList()
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun inflateLayout(): View {
    views = BottomSheetCreateEditFolderBinding.inflate(layoutInflater)
    return views.root
  }

  private fun setupTitle() {
    views.dialogTitle.setTextColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
    views.dialogTitle.typeface = appTypeface.title()
    views.dialogTitle.setText(if (folder.isPersisted()) R.string.folder_sheet_edit_note else R.string.folder_sheet_add_note)
  }

  private fun setupNameTextField() {
    with(views.folderNameTextField) {
      setTextColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
      setHintTextColor(appTheme.getColor(ThemeColor.HINT_TEXT))
      typeface = appTypeface.text()
      setText(folder.title)
      setOnEditorActionListener(getEditorActionListener(
        runnable = {
          saveFolder(text.toString())
          onFolderSaveListener(folder)
          dismiss()
          return@getEditorActionListener true
        }))
    }
  }

  private fun setupSaveButton() {
    views.saveButton.setOnClickListener {
      saveFolder(views.folderNameTextField.text.toString())
      onFolderSaveListener(folder)
      dismiss()
    }
  }

  private fun setupDeleteButton() {
    views.removeButton.isVisible = folder.isPersisted()
    views.removeButton.setOnClickListener {
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

  private fun refreshColorsList() {
    views.colorSelector.removeAllViews()
    val colors = requireContext().resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(requireContext())
      item.setColor(color, selectedColor == color)
      item.setOnClickListener {
        selectedColor = color
        refreshColorsList()
      }
      views.colorSelector.addView(item)
    }
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card, R.id.core_color_card)

  companion object {
    const val KEY_FOLDER_UUID = "folder_uuid"

    fun openSheet(activity: ThemedActivity, folder: Folder, listener: (Folder) -> Unit) {
      val sheet = CreateOrEditFolderBottomSheet()
      sheet.arguments = Bundle().apply { putString(KEY_FOLDER_UUID, folder.uuid.toString()) }
      sheet.onFolderSaveListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}