package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType

class FormatBulletViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  private val extraMargin: View? = root.findViewById(R.id.extra_margin)
  private val bulletIcon: ImageView = root.findViewById(R.id.icon)
  private val textView: TextView = root.findViewById(R.id.text)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    textView.setAppearanceFromConfig(config)
    textView.setTextIsSelectable(true)
    textView.text = Markdown.renderSegment(data.text, true)
    bulletIcon.setColorFilter(config.iconColor)

    when (data.type) {
      FormatType.BULLET_1 -> {
        bulletIcon.setImageResource(R.drawable.bullet_1)
        extraMargin?.isVisible = false
      }
      FormatType.BULLET_2 -> {
        bulletIcon.setImageResource(R.drawable.bullet_2)
        extraMargin?.isVisible = false
      }
      FormatType.BULLET_3 -> {
        bulletIcon.setImageResource(R.drawable.bullet_3)
        extraMargin?.isVisible = true
      }
      else -> {} // Ignore other cases
    }
  }
}
