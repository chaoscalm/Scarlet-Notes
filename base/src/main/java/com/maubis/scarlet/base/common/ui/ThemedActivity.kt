package com.maubis.scarlet.base.common.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.utils.ColorUtil

abstract class ThemedActivity : AppCompatActivity(), ThemeChangeListener {

  abstract fun applyTheming()

  override fun onThemeChange(theme: Theme) = applyTheming()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    useSystemThemeIfNeeded()
    appTheme.registerChangeListener(this)
  }

  fun updateStatusBarTheme(backgroundColor: Int = appTheme.getColor(ThemeColor.STATUS_BAR)) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = ColorUtil.isLightColor(backgroundColor)
    window.statusBarColor = backgroundColor
  }

  override fun onResume() {
    super.onResume()
    if (ScarletApp.prefs.enableFullscreen) {
      enterFullScreen()
    }
  }

  override fun onConfigurationChanged(configuration: Configuration) {
    super.onConfigurationChanged(configuration)
    useSystemThemeIfNeeded()
  }

  private fun useSystemThemeIfNeeded() {
    if (ScarletApp.prefs.useSystemTheme) {
      setThemeFromSystem(this)
      appTheme.reload(this)
    }
  }

  fun enterFullScreen() {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.hide(WindowInsetsCompat.Type.statusBars())
    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
  }

  fun getThemeColor(): Int = appTheme.getColor(ThemeColor.BACKGROUND)

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    } catch (e: Exception) {
      Log.w("Scarlet", "Unable to close the keyboard", e)
    }
  }

  fun tryOpeningTheKeyboard(focusedView: View) {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.showSoftInput(focusedView, InputMethodManager.SHOW_FORCED)
    } catch (e: Exception) {
      Log.w("Scarlet", "Unable to open the keyboard", e)
    }
  }
}