package com.maubis.scarlet.base.common.ui

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R

class TypefaceController(context: Context) {
  data class TypefaceSet(
    val heading: Typeface = Typeface.DEFAULT,
    val subHeading: Typeface = Typeface.DEFAULT,
    val title: Typeface = Typeface.DEFAULT,
    val text: Typeface = Typeface.DEFAULT,
    val code: Typeface = Typeface.MONOSPACE
  )

  private val appTypefaces: TypefaceSet = TypefaceSet(
      heading = ResourcesCompat.getFont(context, R.font.monserrat_medium)!!,
      subHeading = ResourcesCompat.getFont(context, R.font.monserrat_medium)!!,
      title = ResourcesCompat.getFont(context, R.font.monserrat)!!,
      text = ResourcesCompat.getFont(context, R.font.open_sans)!!,
      code = Typeface.MONOSPACE
  )

  init {
    setMarkdownConfig()
  }

  private fun setMarkdownConfig() {
    MarkdownConfig.spanConfig.headingTypeface = subHeading()
    MarkdownConfig.spanConfig.heading2Typeface = title()
    MarkdownConfig.spanConfig.heading3Typeface = title()
    MarkdownConfig.spanConfig.textTypeface = text()
    MarkdownConfig.spanConfig.codeTypeface = code()
  }

  fun heading(): Typeface = appTypefaces.heading

  fun subHeading(): Typeface = appTypefaces.subHeading

  fun title(): Typeface = appTypefaces.title

  fun text(): Typeface = appTypefaces.text

  fun code(): Typeface = appTypefaces.code
}