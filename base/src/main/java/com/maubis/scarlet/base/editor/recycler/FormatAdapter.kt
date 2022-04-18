package com.maubis.scarlet.base.editor.recycler

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.maubis.scarlet.base.editor.Format
import java.util.*

class FormatAdapter(private val formatActivity: IFormatRecyclerViewActivity)
  : MultiRecyclerViewAdapter<Format>(formatActivity.context(), formatActivity.controllerItems()) {

  override fun getItemViewType(position: Int): Int {
    return items[position].type.ordinal
  }

  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(items, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(items, i, i - 1)
      }
    }
    notifyItemMoved(fromPosition, toPosition)
    formatActivity.moveFormat(fromPosition, toPosition)
    return true
  }
}