package com.maubis.scarlet.base.home.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.recycler.setFullSpan
import com.maubis.scarlet.base.home.MainActivity

class NoNotesNoticeRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    itemView.setOnClickListener { (context as MainActivity).launchNewNoteEditor() }
  }
}
