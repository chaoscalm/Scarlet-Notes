package com.maubis.scarlet.base.common.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.common.utils.OsVersionUtils
import com.maubis.scarlet.base.common.utils.logNonCriticalError
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

var sThemeLabel: String
  get() = appPreferences.getString("KEY_APP_THEME", Theme.DARK.name)!!
  set(value) = appPreferences.edit { putString("KEY_APP_THEME", value) }

var sThemeIsAutomatic: Boolean
  get() = appPreferences.getBoolean("automatic_theme", false)
  set(value) = appPreferences.edit { putBoolean("automatic_theme", value) }

var sThemeDarkenCustomColors: Boolean
  get() = appPreferences.getBoolean("darken_note_color", false)
  set(value) = appPreferences.edit { putBoolean("darken_note_color", value) }

fun setThemeFromSystem(context: Context) {
  val configuration = context.resources.configuration
  val systemBasedTheme = when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
    Configuration.UI_MODE_NIGHT_NO -> Theme.LIGHT.name
    Configuration.UI_MODE_NIGHT_YES -> Theme.VERY_DARK.name
    else -> Theme.VERY_DARK.name
  }
  if (systemBasedTheme === sThemeLabel) {
    return
  }
  sThemeLabel = systemBasedTheme
}

class ThemeManager {
  private lateinit var theme: Theme
  private var listeners = HashSet<WeakReference<ThemeChangeListener>>()
  private var colors = HashMap<ThemeColorType, Int>()

  fun setup(context: Context) {
    theme = getThemeFromStore()
    notifyChange(context)
  }

  fun get(): Theme {
    return theme
  }

  fun register(listener: ThemeChangeListener) {
    listeners.add(WeakReference(listener))
  }

  fun isNightTheme() = theme.isNightTheme

  fun shouldDarkenCustomColors() = isNightTheme() && sThemeDarkenCustomColors

  fun get(type: ThemeColorType): Int = colors[type] ?: Color.WHITE

  fun get(context: Context, lightColor: Int, darkColor: Int): Int {
    return ContextCompat.getColor(context, if (isNightTheme()) darkColor else lightColor)
  }

  fun get(context: Context, theme: Theme, type: ThemeColorType): Int {
    return load(context, theme, type)
  }

  fun notifyChange(context: Context) {
    theme = getThemeFromStore()
    for (colorType in ThemeColorType.values()) {
      colors[colorType] = load(context, theme, colorType)
    }

    if (colors[ThemeColorType.TOOLBAR_BACKGROUND] == colors[ThemeColorType.BACKGROUND]) {
      colors[ThemeColorType.TOOLBAR_BACKGROUND] = ColorUtil.darkerColor(
        colors[ThemeColorType.TOOLBAR_BACKGROUND] ?: 0
      )
    }

    setMarkdownConfig(context)
    for (reference in listeners) {
      val listener = reference.get()
      if (listener !== null) {
        listener.onThemeChange(theme)
      }
    }
  }

  private fun setMarkdownConfig(context: Context) {
    MarkdownConfig.spanConfig.codeTextColor = get(ThemeColorType.SECONDARY_TEXT)
    MarkdownConfig.spanConfig.codeBackgroundColor = get(context, R.color.code_light, R.color.code_dark)
    MarkdownConfig.spanConfig.codeBlockLeadingMargin = 8.dpToPixels(context)
    MarkdownConfig.spanConfig.quoteColor = MarkdownConfig.spanConfig.codeBackgroundColor
    MarkdownConfig.spanConfig.separatorColor = MarkdownConfig.spanConfig.codeBackgroundColor
    MarkdownConfig.spanConfig.quoteWidth = 4.dpToPixels(context)
    MarkdownConfig.spanConfig.separatorWidth = 2.dpToPixels(context)
    MarkdownConfig.spanConfig.quoteBlockLeadingMargin = 8.dpToPixels(context)
  }

  private fun Int.dpToPixels(context: Context): Int {
    return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).roundToInt()
  }

  private fun load(context: Context, theme: Theme, type: ThemeColorType): Int {
    val colorResource = when (type) {
      ThemeColorType.BACKGROUND -> theme.background
      ThemeColorType.STATUS_BAR -> {
        if (OsVersionUtils.canSetStatusBarTheme()) theme.background
        else theme.statusBarColorFallback ?: theme.background
      }
      ThemeColorType.PRIMARY_TEXT -> theme.primaryText
      ThemeColorType.SECONDARY_TEXT -> theme.secondaryText
      ThemeColorType.TERTIARY_TEXT -> theme.tertiaryText
      ThemeColorType.HINT_TEXT -> theme.hintText
      ThemeColorType.DISABLED_TEXT -> theme.disabledText
      ThemeColorType.ACCENT_TEXT -> theme.accentText
      ThemeColorType.SECTION_HEADER -> theme.sectionHeader
      ThemeColorType.TOOLBAR_BACKGROUND -> theme.toolbarBackground
      ThemeColorType.TOOLBAR_ICON -> theme.toolbarIcon
    }
    return ContextCompat.getColor(context, colorResource)
  }

  companion object {
    fun getThemeFromStore(): Theme {
      return try {
        Theme.valueOf(sThemeLabel)
      } catch (exception: Exception) {
        logNonCriticalError(exception)
        Theme.DARK
      }
    }
  }
}

// NOTE: Theme names should not be changed since are used to store theme preference
enum class Theme(
  val isNightTheme: Boolean,
  val background: Int,
  val primaryText: Int,
  val secondaryText: Int,
  val tertiaryText: Int,
  val hintText: Int,
  val disabledText: Int,
  val accentText: Int,
  val sectionHeader: Int,
  val toolbarBackground: Int,
  val toolbarIcon: Int,
  val statusBarColorFallback: Int? = null) {
  LIGHT(
    isNightTheme = false,
    background = android.R.color.white,
    primaryText = com.github.bijoysingh.uibasics.R.color.dark_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.dark_tertiary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.dark_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_600,
    toolbarBackground = com.github.bijoysingh.uibasics.R.color.material_grey_50,
    toolbarIcon = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    statusBarColorFallback = com.github.bijoysingh.uibasics.R.color.material_grey_500),
  BEIGE(
    isNightTheme = false,
    background = R.color.app_theme_beige,
    primaryText = com.github.bijoysingh.uibasics.R.color.dark_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.dark_tertiary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.dark_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_700,
    toolbarBackground = R.color.app_theme_beige_dark,
    toolbarIcon = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    statusBarColorFallback = R.color.app_theme_beige_dark),
  ROSE(
    isNightTheme = false,
    background = R.color.app_theme_rose,
    primaryText = com.github.bijoysingh.uibasics.R.color.dark_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.dark_tertiary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.dark_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_700,
    toolbarBackground = R.color.app_theme_rose_dark,
    toolbarIcon = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    statusBarColorFallback = R.color.app_theme_rose_dark),
  SKY(
    isNightTheme = false,
    background = com.github.bijoysingh.uibasics.R.color.material_blue_100,
    primaryText = com.github.bijoysingh.uibasics.R.color.dark_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.dark_tertiary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.dark_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_200,
    accentText = com.github.bijoysingh.uibasics.R.color.material_pink_accent_100,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_600,
    toolbarBackground = com.github.bijoysingh.uibasics.R.color.material_blue_200,
    toolbarIcon = com.github.bijoysingh.uibasics.R.color.dark_secondary_text),
  PURPLE(
    isNightTheme = false,
    background = com.github.bijoysingh.uibasics.R.color.material_purple_100,
    primaryText = com.github.bijoysingh.uibasics.R.color.dark_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.dark_secondary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.dark_tertiary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.dark_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_600,
    toolbarBackground = com.github.bijoysingh.uibasics.R.color.material_purple_200,
    toolbarIcon = com.github.bijoysingh.uibasics.R.color.dark_secondary_text),
  DARK(
    isNightTheme = true,
    background = com.github.bijoysingh.uibasics.R.color.material_grey_850,
    primaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.light_secondary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.light_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_200,
    accentText = com.github.bijoysingh.uibasics.R.color.material_pink_accent_100,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_200,
    toolbarBackground = com.github.bijoysingh.uibasics.R.color.material_grey_900,
    toolbarIcon = android.R.color.white),
  VERY_DARK(
    isNightTheme = true,
    background = com.github.bijoysingh.uibasics.R.color.material_grey_900,
    primaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.light_secondary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.light_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_200,
    accentText = com.github.bijoysingh.uibasics.R.color.material_pink_accent_100,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_200,
    toolbarBackground = com.github.bijoysingh.uibasics.R.color.material_grey_900,
    toolbarIcon = android.R.color.white),
  BLACK(
    isNightTheme = true,
    background = android.R.color.black,
    primaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    secondaryText = com.github.bijoysingh.uibasics.R.color.light_primary_text,
    tertiaryText = com.github.bijoysingh.uibasics.R.color.light_secondary_text,
    hintText = com.github.bijoysingh.uibasics.R.color.light_hint_text,
    disabledText = com.github.bijoysingh.uibasics.R.color.material_grey_200,
    accentText = com.github.bijoysingh.uibasics.R.color.material_pink_accent_100,
    sectionHeader = com.github.bijoysingh.uibasics.R.color.material_blue_grey_200,
    toolbarBackground = android.R.color.black,
    toolbarIcon = android.R.color.white),
}