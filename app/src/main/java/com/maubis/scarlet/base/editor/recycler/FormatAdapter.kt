package com.maubis.scarlet.base.editor.recycler

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.ViewNoteActivity
import java.util.*

class FormatAdapter(private val formatActivity: ViewNoteActivity)
  : MultiRecyclerViewAdapter<Format>(formatActivity, formatActivity.controllerItems()) {

  override fun getItemViewType(position: Int): Int {
    return items[position].type.ordinal
  }

  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    Collections.swap(items, fromPosition, toPosition)
    notifyItemMoved(fromPosition, toPosition)
    formatActivity.onFormatMoved(fromPosition, toPosition)
    return true
  }
}