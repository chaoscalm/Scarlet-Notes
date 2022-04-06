package com.maubis.scarlet.base.common.utils

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

fun getEditorActionListener(
  runnable: () -> Boolean,
  preConditions: () -> Boolean = { false }): TextView.OnEditorActionListener {
  return TextView.OnEditorActionListener { _: TextView, actionId: Int, event: KeyEvent? ->
    if (preConditions()) {
      return@OnEditorActionListener false
    }

    if (event == null) {
      if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
        return@OnEditorActionListener false
      }
    } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
      if (event.action != KeyEvent.ACTION_DOWN) {
        return@OnEditorActionListener true
      }
    } else {
      return@OnEditorActionListener false
    }
    return@OnEditorActionListener runnable()
  }
}
