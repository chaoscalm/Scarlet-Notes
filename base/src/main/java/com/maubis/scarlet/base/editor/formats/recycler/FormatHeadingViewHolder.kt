package com.maubis.scarlet.base.editor.formats.recycler

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
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
    edit.imeOptions = EditorInfo.IME_ACTION_DONE
    edit.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
  }
}