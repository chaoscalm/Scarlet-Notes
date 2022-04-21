package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.text.InputType
import android.view.View
import com.maubis.scarlet.base.common.utils.getEditorActionListener

class FormatHeadingViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {
  init {
    edit.setOnEditorActionListener(getEditorActionListener(
        runnable = {
          activity.createOrChangeToNextFormat(format)
          true
        },
        preConditions = { !edit.isFocused }
    ))
    edit.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
  }
}