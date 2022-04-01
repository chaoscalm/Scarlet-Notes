package com.maubis.scarlet.base.note.tag

import android.app.Dialog
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.database.entities.Tag

class CreateOrEditTagBottomSheet : ThemedBottomSheetFragment() {

  var selectedTag: Tag? = null
  var sheetOnTagListener: (tag: Tag, deleted: Boolean) -> Unit = { _, _ -> }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

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
      val updated = onActionClick(tag, enterTag.text.toString())
      sheetOnTagListener(tag, !updated)
      dismiss()
    }

    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)
    removeBtn.visibility = if (tag.isNotPersisted()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      tag.delete()
      sheetOnTagListener(tag, true)
      dismiss()
    }
    enterTag.setText(tag.title)
    enterTag.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        val updated = onActionClick(tag, enterTag.text.toString())
        sheetOnTagListener(tag, !updated)
        dismiss()
        return@getEditorActionListener true
      }))
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun onActionClick(tag: Tag, title: String): Boolean {
    tag.title = title
    if (tag.title.isBlank()) {
      tag.delete()
      return false
    }
    tag.save()
    return true
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_or_edit_tag

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card)

  companion object {
    fun openSheet(activity: ThemedActivity, tag: Tag, listener: (tag: Tag, deleted: Boolean) -> Unit) {
      val sheet = CreateOrEditTagBottomSheet()

      sheet.selectedTag = tag
      sheet.sheetOnTagListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}