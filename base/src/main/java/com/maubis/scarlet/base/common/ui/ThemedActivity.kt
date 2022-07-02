package com.maubis.scarlet.base.common.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.common.utils.OsVersionUtils

abstract class ThemedActivity : AppCompatActivity(), ThemeChangeListener {

  abstract fun applyTheming()

  override fun onThemeChange(theme: Theme) = applyTheming()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    useSystemThemeIfNeeded()
    appTheme.registerChangeListener(this)
  }

  fun updateStatusBarTheme(backgroundColor: Int = getStatusBarColor()) {
    setStatusBarColor(backgroundColor)
    setStatusBarTextColor(backgroundColor)
  }

  override fun onResume() {
    super.onResume()
    fullScreenView()
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

  private fun fullScreenView() {
    if (!ScarletApp.prefs.enableFullscreen) {
      return
    }

    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_FULLSCREEN
  }

  private fun setStatusBarColor(color: Int) {
    window.statusBarColor = color
  }

  private fun setStatusBarTextColor(backgroundColor: Int) {
    if (OsVersionUtils.canSetStatusBarTheme()) {
      val view = window.decorView
      val visibilityFlags = if (ColorUtil.isLightColor(backgroundColor))
        view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      else
        view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
      view.systemUiVisibility = visibilityFlags
    }
  }

  fun getThemeColor(): Int = appTheme.getColor(ThemeColor.BACKGROUND)

  fun getStatusBarColor(): Int = appTheme.getColor(ThemeColor.STATUS_BAR)

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