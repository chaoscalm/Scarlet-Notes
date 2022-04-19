package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.utils.getEditorActionListener
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType

class FormatListViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val checkBox: CheckBox = root.findViewById(R.id.icon)
  private val closeButton: ImageView = root.findViewById(R.id.close)
  private val extraMargin: View = root.findViewById(R.id.extra_margin)

  init {
    edit.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        activity.createOrChangeToNextFormat(format)
        true
      },
      preConditions = { !edit.isFocused }
    ))
    edit.imeOptions = EditorInfo.IME_ACTION_DONE
    edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
  }

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    when (data.type) {
      FormatType.CHECKLIST_CHECKED -> {
        checkBox.isChecked = true
        text.paintFlags = text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        itemView.alpha = 0.5f
      }
      FormatType.CHECKLIST_UNCHECKED -> {
        checkBox.isChecked = false
        text.paintFlags = text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        itemView.alpha = 1f
      }
      else -> {
      } // Ignore other cases
    }

    closeButton.isVisible = config.editable
    closeButton.setColorFilter(config.iconColor)
    closeButton.alpha = 0.8f
    closeButton.setOnClickListener {
      activity.deleteFormat(format)
    }

    extraMargin.isVisible = !config.editable
    checkBox.buttonTintList = ColorStateList.valueOf(config.iconColor)
    checkBox.setOnClickListener {
      activity.setFormatChecked(data, data.type != FormatType.CHECKLIST_CHECKED)
    }
  }
}
