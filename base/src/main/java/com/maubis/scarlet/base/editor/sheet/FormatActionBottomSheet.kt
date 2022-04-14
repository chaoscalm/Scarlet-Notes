package com.maubis.scarlet.base.editor.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.sheets.GridOptionBottomSheet
import com.maubis.scarlet.base.common.specs.GridSectionItem
import com.maubis.scarlet.base.common.specs.GridSectionOptionItem
import com.maubis.scarlet.base.common.utils.copyTextToClipboard
import com.maubis.scarlet.base.common.utils.shareText
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.FormatType
import pl.aprilapps.easyphotopicker.EasyImage

class FormatActionBottomSheet : GridOptionBottomSheet() {

  var noteUUID: String = "default"
  var format: Format? = null

  override fun title(): Int = R.string.format_action_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem> {
    val activity = componentContext.androidContext as ViewNoteActivity

    val sections = ArrayList<GridSectionItem>()
    val options = ArrayList<GridSectionOptionItem>()

    if (this.format === null) {
      return sections
    }

    val format: Format = this.format!!
    options.add(
      GridSectionOptionItem(
        label = R.string.action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = {
          shareText(activity, format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.type)
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.format_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = {
          copyTextToClipboard(requireContext(), format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.type)
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.format_action_camera,
        icon = R.drawable.ic_image_camera,
        listener = {
          EasyImage.openCameraForImage(activity, format.uid)
        },
        visible = format.type === FormatType.IMAGE
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.format_action_gallery,
        icon = R.drawable.ic_image_gallery,
        listener = {
          EasyImage.openGallery(activity, format.uid)
        },
        visible = format.type === FormatType.IMAGE
      ))
    options.add(GridSectionOptionItem(
      label = R.string.delete_sheet_delete_trash_yes,
      icon = R.drawable.ic_delete_white_48dp,
      listener = {
        activity.deleteFormat(format)
        if (format.type === FormatType.IMAGE && format.text.isNotBlank()) {
          imageStorage.deleteImageIfExists(noteUUID, format)
        }
        dismiss()
      }
    ))

    sections.add(GridSectionItem(options = options))
    return sections
  }
}