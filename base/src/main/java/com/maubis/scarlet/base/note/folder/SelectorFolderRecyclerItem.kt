package com.maubis.scarlet.base.note.folder

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.database.entities.Folder

class SelectorFolderRecyclerItem(context: Context, val folder: Folder) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColor(folder.color)
  val title = folder.title
  val titleColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)

  val folderColor = folder.color
  val iconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_primary_text)
  }
  override val type = Type.FOLDER
}
