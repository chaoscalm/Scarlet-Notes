package com.maubis.scarlet.base.editor.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.sheets.GridActionsBottomSheet
import com.maubis.scarlet.base.common.specs.GridActionItem
import com.maubis.scarlet.base.common.specs.GridSection
import com.maubis.scarlet.base.common.utils.copyTextToClipboard
import com.maubis.scarlet.base.common.utils.shareText
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.ViewNoteActivity
import pl.aprilapps.easyphotopicker.EasyImage

class FormatActionBottomSheet : GridActionsBottomSheet() {

  var noteUUID: String = "default"
  var format: Format? = null

  override fun titleRes(): Int = R.string.format_action_title

  override fun getSections(componentContext: ComponentContext, dialog: Dialog): List<GridSection> {
    val activity = componentContext.androidContext as ViewNoteActivity

    if (this.format == null) {
      return emptyList()
    }

    val format: Format = this.format!!
    val items = ArrayList<GridActionItem>()
    items.add(
      GridActionItem(
        label = R.string.action_share,
        icon = R.drawable.ic_share,
        listener = {
          shareText(activity, format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.type)
      ))
    items.add(
      GridActionItem(
        label = R.string.format_action_copy,
        icon = R.drawable.ic_copy,
        listener = {
          copyTextToClipboard(requireContext(), format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.type)
      ))
    items.add(
      GridActionItem(
        label = R.string.format_action_camera,
        icon = R.drawable.ic_camera,
        listener = {
          EasyImage.openCameraForImage(activity, format.uid)
        },
        visible = format.type === FormatType.IMAGE
      ))
    items.add(
      GridActionItem(
        label = R.string.format_action_gallery,
        icon = R.drawable.ic_image,
        listener = {
          EasyImage.openGallery(activity, format.uid)
        },
        visible = format.type === FormatType.IMAGE
      ))
    items.add(GridActionItem(
      label = R.string.delete_sheet_delete_trash_yes,
      icon = R.drawable.ic_delete,
      listener = {
        activity.deleteFormat(format)
        if (format.type === FormatType.IMAGE && format.text.isNotBlank()) {
          imageStorage.deleteImageIfExists(noteUUID, format)
        }
        dismiss()
      }
    ))

    return listOf(GridSection(items))
  }
}