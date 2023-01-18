package com.maubis.scarlet.base.home.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.recycler.setFullSpan
import com.maubis.scarlet.base.home.MainActivity

class NoNotesNoticeRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  private val activity = context as MainActivity
  private val title: TextView = itemView.findViewById(R.id.title)
  private val description: TextView = itemView.findViewById(R.id.description)

  override fun populate(data: RecyclerItem, extra: Bundle?) {
    setFullSpan()
    setHeaderText()
    setDescriptionText()
    setTouchListener()
  }

  private fun setHeaderText() {
    title.text = if (activity.isInTrash && !activity.isInSearchMode) {
      activity.getString(R.string.main_trash_empty)
    } else {
      activity.getString(R.string.main_no_notes)
    }
  }

  private fun setDescriptionText() {
    description.text = when {
      activity.isInSearchMode -> activity.getString(R.string.main_no_notes_found)
      activity.isInTrash -> activity.getString(R.string.main_trash_empty_hint)
      else -> activity.getString(R.string.main_no_notes_hint)
    }
  }

  private fun setTouchListener() {
    if (activity.isInSearchMode || activity.isInTrash) {
      itemView.isClickable = false
    } else {
      itemView.setOnClickListener { activity.launchNewNoteEditor() }
    }
  }
}
