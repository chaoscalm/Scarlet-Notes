package com.maubis.scarlet.base.editor.formats.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.FormatType

class FormatBulletViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val firstMargin: View = root.findViewById(R.id.first_margin)
  private val secondMargin: View = root.findViewById(R.id.second_margin)
  private val icon: ImageView = root.findViewById(R.id.icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    icon.setColorFilter(config.iconColor)

    when (data.type) {
      FormatType.BULLET_1 -> {
        icon.setImageResource(R.drawable.icon_bullet_1)
        firstMargin.isVisible = false
        secondMargin.isVisible = false
      }
      FormatType.BULLET_2 -> {
        icon.setImageResource(R.drawable.icon_bullet_2)
        firstMargin.isVisible = false
        secondMargin.isVisible = true
      }
      FormatType.BULLET_3 -> {
        icon.setImageResource(R.drawable.icon_bullet_3)
        firstMargin.isVisible = true
        secondMargin.isVisible = true
      }
      else -> {
      } // Ignore other cases
    }
  }
}
