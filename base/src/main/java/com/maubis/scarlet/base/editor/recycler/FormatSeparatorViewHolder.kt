package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.sheet.FormatActionBottomSheet

class FormatSeparatorViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  private val separator: View = root.findViewById(R.id.separator)
  private val dragHandle: ImageView = root.findViewById(R.id.action_move_icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    separator.setBackgroundColor(config.hintTextColor)

    dragHandle.setColorFilter(config.iconColor)
    dragHandle.isVisible = config.editable
    dragHandle.setOnClickListener {
      openSheet(activity, FormatActionBottomSheet().apply {
        noteUUID = config.noteUUID
        format = data
      })
    }
    dragHandle.setOnLongClickListener {
      activity.startFormatDrag(this)
      true
    }

    itemView.setOnLongClickListener {
      activity.startFormatDrag(this)
      true
    }
  }
}