package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.ui.Theme
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.settings.STORE_KEY_TEXT_SIZE
import com.maubis.scarlet.base.settings.TEXT_SIZE_DEFAULT
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.settings.sUIUseNoteColorAsBackground

const val KEY_EDITABLE = "KEY_EDITABLE"
const val KEY_NOTE_COLOR = "KEY_NOTE_COLOR"

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
    val noteColor: Int = extra?.getInt(KEY_NOTE_COLOR) ?: sNoteDefaultColor
    val secondaryTextColor: Int
    val tertiaryTextColor: Int
    val iconColor: Int
    val hintTextColor: Int
    val isLightBackground = ColorUtil.isLightColor(noteColor)
    val linkColor: Int
    when {
      !sUIUseNoteColorAsBackground -> {
        secondaryTextColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)
        iconColor = appTheme.get(ThemeColorType.TOOLBAR_ICON)
        hintTextColor = appTheme.get(ThemeColorType.HINT_TEXT)
        linkColor = appTheme.get(ThemeColorType.ACCENT_TEXT)
      }
      isLightBackground -> {
        secondaryTextColor = appTheme.get(context, Theme.LIGHT, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.get(context, Theme.LIGHT, ThemeColorType.TERTIARY_TEXT)
        iconColor = appTheme.get(context, Theme.LIGHT, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = appTheme.get(context, Theme.LIGHT, ThemeColorType.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowLight)
      }
      else -> {
        secondaryTextColor = appTheme.get(context, Theme.DARK, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = appTheme.get(context, Theme.DARK, ThemeColorType.TERTIARY_TEXT)
        iconColor = appTheme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = appTheme.get(context, Theme.DARK, ThemeColorType.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowDark)
      }
    }
    val fontSize = extra?.getInt(STORE_KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT) ?: TEXT_SIZE_DEFAULT
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
          FormatType.CODE, FormatType.IMAGE -> appTheme.get(context, R.color.code_light, R.color.code_dark)
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
          else -> Typeface.NORMAL
        }
    )

    populate(data, config)
  }

  abstract fun populate(data: Format, config: FormatViewHolderConfig)
}
