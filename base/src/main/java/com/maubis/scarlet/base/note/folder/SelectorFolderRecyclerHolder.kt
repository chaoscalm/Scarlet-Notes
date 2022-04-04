package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.recycler.setFullSpan
import com.maubis.scarlet.base.common.ui.CircleDrawable

class SelectorFolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  private val title: TextView = view.findViewById(R.id.folder_title)
  private val icon: ImageView = view.findViewById(R.id.folder_icon)

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    setFullSpan()

    val item = itemData as SelectorFolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)
    title.typeface = appTypeface.title()
    title.alpha = 0.8f

    icon.setColorFilter(item.iconColor)
    icon.background = CircleDrawable(item.folderColor, false)
    icon.alpha = 0.8f
  }
}