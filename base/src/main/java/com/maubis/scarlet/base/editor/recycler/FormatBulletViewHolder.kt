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

  private val textView: TextView = root.findViewById(R.id.text)
  private val firstMargin: View = root.findViewById(R.id.first_margin)
  private val secondMargin: View = root.findViewById(R.id.second_margin)
  private val bulletIcon: ImageView = root.findViewById(R.id.icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    textView.setAppearanceFromConfig(config)
    textView.setTextIsSelectable(true)
    textView.text = Markdown.renderSegment(data.text, true)
    bulletIcon.setColorFilter(config.iconColor)

    when (data.type) {
      FormatType.BULLET_1 -> {
        bulletIcon.setImageResource(R.drawable.icon_bullet_1)
        firstMargin.isVisible = false
        secondMargin.isVisible = false
      }
      FormatType.BULLET_2 -> {
        bulletIcon.setImageResource(R.drawable.icon_bullet_2)
        firstMargin.isVisible = false
        secondMargin.isVisible = true
      }
      FormatType.BULLET_3 -> {
        bulletIcon.setImageResource(R.drawable.icon_bullet_3)
        firstMargin.isVisible = true
        secondMargin.isVisible = true
      }
      else -> {} // Ignore other cases
    }
  }
}
