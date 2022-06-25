package com.maubis.scarlet.base.note.folder

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.database.entities.Folder

class FolderRecyclerItem(
  context: Context,
  val folder: Folder,
  notesCount: Int,
  val click: () -> Unit = {},
  val longClick: () -> Unit = {}) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColor(folder.color)
  val title = folder.title
  val titleColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_primary_text)
  }

  val label = when (notesCount) {
    0 -> context.getString(R.string.folder_card_title)
    1 -> context.getString(R.string.folder_card_title_single_note)
    else -> context.getString(R.string.folder_card_title_notes, notesCount)
  }
  val labelColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_secondary_text)
  }

  override val type = Type.FOLDER
}
