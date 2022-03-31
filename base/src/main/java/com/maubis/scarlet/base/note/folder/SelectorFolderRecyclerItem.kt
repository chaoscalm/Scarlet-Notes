package com.maubis.scarlet.base.note.folder

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.ColorUtil

class SelectorFolderRecyclerItem(context: Context, val folder: Folder) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColor(folder.color)
  val title = folder.title
  val titleColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)

  val folderColor = folder.color
  val iconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }
  override val type = Type.FOLDER
}
