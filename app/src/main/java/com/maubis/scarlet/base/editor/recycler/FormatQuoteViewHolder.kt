package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.Format

class FormatQuoteViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val quoteColorView = findViewById<View>(R.id.quoteColorView)
  private val extraMargin: View = root.findViewById(R.id.extra_margin)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    extraMargin.isVisible = !config.editable
    quoteColorView.setBackgroundColor(MarkdownConfig.spanConfig.quoteColor)
  }
}