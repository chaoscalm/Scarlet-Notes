package com.maubis.scarlet.base.note.tag

import android.app.Dialog
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
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.note.selection.NotesSelectionActivity
import java.util.*

class SelectedTagChooserBottomSheet : LithoBottomSheet() {

  var onActionListener: (Tag, Boolean) -> Unit = { _, _ -> }

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as NotesSelectionActivity
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
      icon = R.drawable.ic_add_note,
      listener = {
          CreateOrEditTagBottomSheet.openSheet(activity, Tag()) { tag ->
            onActionListener(tag, true)
            refresh(activity, dialog)
          }
      })
    tagsComponent.child(OptionItemLayout.create(componentContext)
                          .option(addTag)
                          .backgroundRes(R.drawable.accent_rounded_bg)
                          .marginDip(YogaEdge.TOP, 16f)
                          .onClick { addTag.listener() })

    component.child(tagsComponent)
    return component.build()
  }

  private fun getTagItems(): List<TagItem> {
    val activity = context as NotesSelectionActivity
    val items = ArrayList<TagItem>()

    val tags = HashSet<UUID>()
    tags.addAll(activity.getAllSelectedNotes().firstOrNull()?.tags ?: emptySet())

    activity.getAllSelectedNotes().forEach {
      val tagsToRemove = HashSet<UUID>()
      for (tag in tags) {
        if (!it.tags.contains(tag)) {
          tagsToRemove.add(tag)
        }
      }
      tags.removeAll(tagsToRemove)
    }
    for (tag in data.tags.getAll()) {
      items.add(
        TagItem(
          tag = tag,
          listener = {
            onActionListener(tag, !tags.contains(tag.uuid))
            activity.refreshSelectedNotes()
            refresh(activity, requireDialog())
          },
          isSelected = tags.contains(tag.uuid)
        ))
    }
    items.sortByDescending { if (it.isSelected) 1 else 0 }
    return items
  }
}