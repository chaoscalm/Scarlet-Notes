package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.ui.Theme
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.settings.AppPreferences.Companion.DEFAULT_TEXT_SIZE

const val KEY_EDITABLE = "KEY_EDITABLE"
const val KEY_NOTE_COLOR = "KEY_NOTE_COLOR"
const val KEY_TEXT_SIZE = "KEY_TEXT_SIZE"

data class FormatViewHolderConfig(
  val editable: Boolean,
  val isMarkdownEnabled: Boolean,
  val fontSize: Float,
  val backgroundColor: Int,
  val secondaryTextColor: Int,
  val tertiaryTextColor: Int,
  val iconColor: Int,
  val hintTextColor: Int,
  val accentColor: Int,
  val noteUUID: String,
  val typeface: Typeface,
  val typefaceStyle: Int)

abstract class FormatViewHolderBase(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  protected val activity: ViewNoteActivity = context as ViewNoteActivity

  override fun populate(data: Format, extra: Bundle?) {
    val noteColor: Int = extra?.getInt(KEY_NOTE_COLOR) ?: ScarletApp.prefs.noteDefaultColor
    val secondaryTextColor: Int
    val tertiaryTextColor: Int
    val iconColor: Int
    val hintTextColor: Int
    val isLightBackground = ColorUtil.isLightColor(noteColor)
    val linkColor: Int
    when {
      !ScarletApp.prefs.useNoteColorAsBackground -> {
        secondaryTextColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)
        iconColor = appTheme.getColor(ThemeColor.ICON)
        hintTextColor = appTheme.getColor(ThemeColor.HINT_TEXT)
        linkColor = appTheme.getColor(ThemeColor.ACCENT_TEXT)
      }
      isLightBackground -> {
        secondaryTextColor = appTheme.getColor(context, Theme.LIGHT, ThemeColor.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.getColor(context, Theme.LIGHT, ThemeColor.TERTIARY_TEXT)
        iconColor = appTheme.getColor(context, Theme.LIGHT, ThemeColor.ICON)
        hintTextColor = appTheme.getColor(context, Theme.LIGHT, ThemeColor.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowLight)
      }
      else -> {
        secondaryTextColor = appTheme.getColor(context, Theme.DARK, ThemeColor.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.getColor(context, Theme.DARK, ThemeColor.TERTIARY_TEXT)
        iconColor = appTheme.getColor(context, Theme.DARK, ThemeColor.ICON)
        hintTextColor = appTheme.getColor(context, Theme.DARK, ThemeColor.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowDark)
      }
    }
    val fontSize = extra?.getInt(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE) ?: DEFAULT_TEXT_SIZE
    val config = FormatViewHolderConfig(
        editable = !(extra != null
          && extra.containsKey(KEY_EDITABLE)
          && !extra.getBoolean(KEY_EDITABLE)),
        isMarkdownEnabled = data.type != FormatType.CODE,
        fontSize = when (data.type) {
          FormatType.HEADING -> fontSize.toFloat() + 4
          FormatType.SUB_HEADING -> fontSize.toFloat() + 2
          else -> fontSize.toFloat()
        },
        backgroundColor = when (data.type) {
          FormatType.CODE, FormatType.IMAGE -> appTheme.getLightOrDarkColor(context, R.color.code_light, R.color.code_dark)
          else -> ContextCompat.getColor(context, android.R.color.transparent)
        },
        secondaryTextColor = secondaryTextColor,
        tertiaryTextColor = tertiaryTextColor,
        iconColor = iconColor,
        hintTextColor = hintTextColor,
        accentColor = linkColor,
        noteUUID = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default",
        typeface = when (data.type) {
          FormatType.HEADING -> appTypeface.subHeading()
          FormatType.SUB_HEADING -> appTypeface.title()
          FormatType.HEADING_3 -> appTypeface.title()
          FormatType.CODE -> appTypeface.code()
          else -> appTypeface.text()
        },
        typefaceStyle = when (data.type) {
          FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3 -> Typeface.BOLD
          FormatType.QUOTE -> Typeface.ITALIC
          else -> Typeface.NORMAL
        }
    )

    populate(data, config)
  }

  protected fun TextView.setAppearanceFromConfig(config: FormatViewHolderConfig) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    setTextColor(config.secondaryTextColor)
    setHintTextColor(config.hintTextColor)
    setBackgroundColor(config.backgroundColor)
    setLinkTextColor(config.accentColor)
    setTypeface(config.typeface, config.typefaceStyle)
  }

  abstract fun populate(data: Format, config: FormatViewHolderConfig)
}
