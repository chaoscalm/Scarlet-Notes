package com.maubis.scarlet.base.common.recycler

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

fun RecyclerViewHolder<RecyclerItem>.setFullSpan() {
  val layoutParams = itemView.layoutParams
  if (layoutParams is StaggeredGridLayoutManager.LayoutParams)
    layoutParams.isFullSpan = true
}