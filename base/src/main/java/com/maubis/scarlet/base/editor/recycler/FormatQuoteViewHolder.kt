package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.view.View
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.Format

class FormatQuoteViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val quoteColorView = findViewById<View>(R.id.quoteColorView)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    quoteColorView.setBackgroundColor(MarkdownConfig.spanConfig.quoteColor)
  }
}