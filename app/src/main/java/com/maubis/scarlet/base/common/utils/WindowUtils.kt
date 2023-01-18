package com.maubis.scarlet.base.common.utils

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.maubis.scarlet.base.ScarletApp

fun Window.enterFullScreenIfEnabled() {
  if (ScarletApp.prefs.enableFullscreen) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.hide(WindowInsetsCompat.Type.statusBars())
    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
  }
}