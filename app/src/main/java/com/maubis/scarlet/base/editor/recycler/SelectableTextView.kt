package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class SelectableTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
  : AppCompatTextView(context, attrs, defStyleAttr) {

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.textViewStyle)
  constructor(context: Context) : this(context, null)

  // Work around https://issuetracker.google.com/issues/37095917, which often causes
  // the inability to select the text when the TextView is used in a RecyclerView
  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (isEnabled) {
      val method = movementMethod
      movementMethod = null
      movementMethod = method
    }
  }
}