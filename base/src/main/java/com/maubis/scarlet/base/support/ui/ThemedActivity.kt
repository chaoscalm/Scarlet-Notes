package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.settings.sInternalEnableFullScreen
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.support.utils.logAndMaybeDisplayError

abstract class ThemedActivity : AppCompatActivity(), IThemeChangeListener {

  abstract fun notifyThemeChange()

  override fun onChange(theme: Theme) {
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

  fun setStatusBarColor(color: Int) {
    window.statusBarColor = color
  }

  fun setStatusBarTextColor() {
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
    } catch (exception: Exception) {
      logAndMaybeDisplayError(this, exception)
    }
  }

  fun tryOpeningTheKeyboard(focusedView: View) {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.showSoftInput(focusedView, InputMethodManager.SHOW_FORCED)
    } catch (exception: Exception) {
      logAndMaybeDisplayError(this, exception)
    }
  }
}