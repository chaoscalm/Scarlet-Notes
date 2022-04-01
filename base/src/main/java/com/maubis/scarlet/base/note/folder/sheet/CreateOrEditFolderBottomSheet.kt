package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.content.Context
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

  var selectedFolder: Folder? = null
  var sheetOnFolderListener: (folder: Folder, deleted: Boolean) -> Unit = { _, _ -> }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val folder = selectedFolder
    if (folder == null) {
      dismiss()
      return
    }

    val title = dialog.findViewById<TextView>(R.id.options_title)
    title.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    title.typeface = appTypeface.title()
    title.setText(if (folder.isNotPersisted()) R.string.folder_sheet_add_note else R.string.folder_sheet_edit_note)

    val enterFolder = dialog.findViewById<EditText>(R.id.enter_folder)
    enterFolder.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    enterFolder.setHintTextColor(appTheme.get(ThemeColorType.HINT_TEXT))
    enterFolder.typeface = appTypeface.text()

    val action = dialog.findViewById<TextView>(R.id.action_button)
    action.setOnClickListener {
      val updated = onActionClick(folder, enterFolder.text.toString())
      sheetOnFolderListener(folder, !updated)
      dismiss()
    }

    val folderDeleteListener = sheetOnFolderListener
    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)
    removeBtn.visibility = if (folder.isNotPersisted()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      openSheet(context as AppCompatActivity, DeleteFolderBottomSheet().apply {
        selectedFolder = folder
        sheetOnFolderListener = folderDeleteListener
      })
      dismiss()
    }
    enterFolder.setText(folder.title)
    enterFolder.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        val updated = onActionClick(folder, enterFolder.text.toString())
        sheetOnFolderListener(folder, !updated)
        dismiss()
        return@getEditorActionListener true
      }))

    val colorFlexbox = dialog.findViewById<FlexboxLayout>(R.id.color_flexbox)
    setColorsList(dialog.context, folder, colorFlexbox)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun onActionClick(folder: Folder, title: String): Boolean {
    folder.title = title
    if (folder.title.isBlank()) {
      folder.delete()
      return false
    }
    folder.updateTimestamp = System.currentTimeMillis()
    folder.save()
    return true
  }

  private fun setColorsList(context: Context, folder: Folder, colorSelectorLayout: FlexboxLayout) {
    colorSelectorLayout.removeAllViews()
    val colors = context.resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(context)
      item.setColor(color, folder.color == color)
      item.setOnClickListener {
        folder.color = color
        setColorsList(context, folder, colorSelectorLayout)
      }
      colorSelectorLayout.addView(item)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_folder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card, R.id.core_color_card)

  companion object {
    fun openSheet(activity: ThemedActivity, folder: Folder, listener: (folder: Folder, deleted: Boolean) -> Unit) {
      val sheet = CreateOrEditFolderBottomSheet()

      sheet.selectedFolder = folder
      sheet.sheetOnFolderListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}