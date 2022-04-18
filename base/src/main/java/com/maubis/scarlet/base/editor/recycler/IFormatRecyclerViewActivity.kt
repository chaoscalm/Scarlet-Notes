package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.editor.Format

interface IFormatRecyclerViewActivity {
  fun context(): Context

  fun deleteFormat(format: Format)

  fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>>

  fun moveFormat(fromPosition: Int, toPosition: Int)
}