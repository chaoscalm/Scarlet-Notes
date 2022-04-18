package com.maubis.scarlet.base.editor.recycler

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType

fun getFormatControllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<Format>>()
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.TAG.ordinal)
      .layoutFile(R.layout.item_format_tag)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.TEXT.ordinal)
      .layoutFile(R.layout.item_format_text)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.HEADING.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatHeadingViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.SUB_HEADING.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatHeadingViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.HEADING_3.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatHeadingViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.QUOTE.ordinal)
      .layoutFile(R.layout.item_format_quote)
      .holderClass(FormatQuoteViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CODE.ordinal)
      .layoutFile(R.layout.item_format_code)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_1.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_2.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_3.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CHECKLIST_CHECKED.ordinal)
      .layoutFile(R.layout.item_format_list)
      .holderClass(FormatListViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal)
      .layoutFile(R.layout.item_format_list)
      .holderClass(FormatListViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.IMAGE.ordinal)
      .layoutFile(R.layout.item_format_image)
      .holderClass(FormatImageViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.SEPARATOR.ordinal)
      .layoutFile(R.layout.item_format_separator)
      .holderClass(FormatSeparatorViewHolder::class.java)
      .build())
  return list
}
