package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.settings.sEditorMoveHandles
import com.maubis.scarlet.base.support.sheets.openSheet

class FormatSeparatorViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  val separator: View = root.findViewById(R.id.separator)
  val actionMove: ImageView = root.findViewById(R.id.action_move_icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {

    separator.setBackgroundColor(config.hintTextColor)

    actionMove.setColorFilter(config.iconColor)
    actionMove.isVisible = config.editable
    actionMove.setOnClickListener {
      openSheet(activity, FormatActionBottomSheet().apply {
        noteUUID = config.noteUUID
        format = data
      })
    }
    if (config.editable && !sEditorMoveHandles) {
      actionMove.visibility = View.INVISIBLE
    }
  }
}