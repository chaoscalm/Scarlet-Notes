package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.settings.sInternalEnableFullScreen
import com.maubis.scarlet.base.support.utils.OsVersionUtils

abstract class ThemedActivity : AppCompatActivity(), ThemeChangeListener {

  abstract fun notifyThemeChange()

  override fun onThemeChange(theme: Theme) {
    notifyThemeChange()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appTheme.register(this)
  }

  fun setSystemTheme(color: Int = getStatusBarColor()) {
    setStatusBarColor(color)
    setStatusBarTextColor()
  }

  override fun onResume() {
    super.onResume()
    fullScreenView()
  }

  override fun onConfigurationChanged(configuration: Configuration) {
    super.onConfigurationChanged(configuration)
    if (!sThemeIsAutomatic) {
      return
    }
    setThemeFromSystem(this)
    appTheme.notifyChange(this)
  }

  fun fullScreenView() {
    if (!sInternalEnableFullScreen) {
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

  private fun setStatusBarTextColor() {
    if (OsVersionUtils.canSetStatusBarTheme()) {
      val view = window.decorView
      var flags = view.systemUiVisibility
      flags = when (appTheme.isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int = appTheme.get(ThemeColorType.BACKGROUND)

  fun getStatusBarColor(): Int = appTheme.get(ThemeColorType.STATUS_BAR)

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
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