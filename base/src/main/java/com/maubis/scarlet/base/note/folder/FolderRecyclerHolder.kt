package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.utils.ColorUtil.darkerColor

class FolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  private val view: CardView = view as CardView
  private val label: UITextView = view.findViewById(R.id.ui_information_title)
  private val title: TextView = view.findViewById(R.id.title)

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as FolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)
    title.typeface = appTypeface.title()

    label.setText(item.label)
    label.setImageTint(item.labelColor)
    label.setTextColor(item.labelColor)
    label.label.typeface = appTypeface.text()

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
      return@setOnLongClickListener false
    }

    when (item.selected) {
      true -> {
        view.alpha = 0.5f
        label.visibility = View.GONE
        title.minLines = 1
      }
      false -> {
        view.alpha = 1.0f
        label.visibility = View.VISIBLE
        title.minLines = 1
      }
    }
    view.alpha = if (item.selected) 0.5f else 1.0f
  }
}