package com.maubis.scarlet.base.editor.recycler

interface ItemTouchHelperAdapter {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

  fun onItemDismiss(position: Int)
}