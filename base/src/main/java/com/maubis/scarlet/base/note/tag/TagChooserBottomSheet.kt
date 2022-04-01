package com.maubis.scarlet.base.note.tag

import android.app.Dialog
import android.content.DialogInterface
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.sheets.OptionItemLayout
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.note.toggleTag

class TagChooserBottomSheet(private val note: Note, private val dismissListener: () -> Unit) : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as ThemedActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
    val tagsComponent = Column.create(componentContext)
      .paddingDip(YogaEdge.TOP, 8f)
      .paddingDip(YogaEdge.BOTTOM, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.tag_sheet_choose_tag)
          .marginDip(YogaEdge.BOTTOM, 12f))
    getTagItems().forEach {
      tagsComponent.child(TagItemLayout.create(componentContext).tagItem(it))
    }

    val addTag = LithoOptionsItem(
      title = R.string.tag_sheet_new_tag_button,
      subtitle = 0,
      icon = R.drawable.icon_add_note,
      listener = { CreateOrEditTagBottomSheet.openSheet(activity, Tag()) { _, _ -> refresh(activity, dialog) } })
    tagsComponent.child(OptionItemLayout.create(componentContext)
                          .option(addTag)
                          .backgroundRes(R.drawable.accent_rounded_bg)
                          .marginDip(YogaEdge.TOP, 16f)
                          .onClick { addTag.listener() })

    component.child(tagsComponent)
    return component.build()
  }

  private fun getTagItems(): List<TagItem> {
    val activity = context as ThemedActivity
    val items = ArrayList<TagItem>()
    val noteTags = note.getTagUUIDs()
    for (tag in data.tags.getAll()) {
      items.add(
        TagItem(
          tag = tag,
          listener = {
            note.toggleTag(tag)
            note.save(activity)
            refresh(activity, requireDialog())
          },
          isSelected = noteTags.contains(tag.uuid),
          isEditable = true,
          editListener = {
            CreateOrEditTagBottomSheet.openSheet(activity, tag) { _, _ ->
              refresh(activity, requireDialog())
            }
          }
        ))
    }
    return items
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    dismissListener()
  }
}