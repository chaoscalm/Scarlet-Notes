package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.ColorUtil.darkerColor

class FolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  private val view: CardView = view as CardView
  private val notesCountText: TextView = view.findViewById(R.id.notes_count)
  private val icon: ImageView = view.findViewById(R.id.icon)
  private val title: TextView = view.findViewById(R.id.title)

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as FolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)
    title.typeface = appTypeface.title()

    icon.imageTintList = ColorStateList.valueOf(item.labelColor)
    notesCountText.text = item.label
    notesCountText.setTextColor(item.labelColor)
    notesCountText.typeface = appTypeface.text()

    val folderColor = if (appTheme.shouldDarkenCustomColors()) {
      darkerColor(item.folder.color)
    } else {
      item.folder.color
    }
    view.setCardBackgroundColor(folderColor)
    view.setOnClickListener {
      item.click()
    }
    view.setOnLongClickListener {
      item.longClick()
      true
    }
  }
}