package com.maubis.scarlet.base.home

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.settings.ColorView
import com.maubis.scarlet.base.support.ui.ThemeColorType

class TagsAndColorPickerViewHolder(
        val activity: MainActivity,
        val flexbox: FlexboxLayout,
        val onTagClick: (Tag) -> Unit,
        val onColorClick: (Int) -> Unit) {

  val tags = emptySet<Tag>().toMutableSet()
  val colors = emptySet<Int>().toMutableSet()

  fun reset() {
    tags.clear()
    tags.addAll(data.tags.search(""))

    colors.clear()
    colors.addAll(data.notes.getAll().map { it.color })
  }

  fun notifyChanged() {
    setViews()
  }

  @Synchronized
  fun setViews() {
    flexbox.removeAllViews()
    setTags()
    setColors()
  }

  private fun setTags() {
    val length = tags.size
    tags.toList()
      .subList(0, length.coerceAtMost(6))
      .forEach { tag ->
        val tagView = View.inflate(activity, R.layout.layout_flexbox_tag_item, null) as View
        val text = tagView.findViewById<TextView>(R.id.tag_text)
        text.text = tag.title
        text.typeface = appTypeface.title()

        val backgroundDrawable = activity.getDrawable(R.drawable.flexbox_selected_tag_item_bg)!!
        if (activity.state.tags.any { it.uuid == tag.uuid }) {
          val accentColor = ContextCompat.getColor(activity, R.color.colorAccent)
          backgroundDrawable.setTint(accentColor)
          text.setTextColor(accentColor)
        } else {
          val themeColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)
          backgroundDrawable.setTint(themeColor)
          text.setTextColor(themeColor)
        }
        text.background = backgroundDrawable

        tagView.setOnClickListener { onTagClick(tag) }
        flexbox.addView(tagView)
      }
  }

  private fun setColors() {
    val length = colors.size
    colors.toList()
      .subList(0, length.coerceAtMost(6))
      .forEach { color ->
        val colorView = ColorView(activity, R.layout.layout_color_small)
        colorView.setColor(color, activity.state.colors.contains(color))
        colorView.setOnClickListener { onColorClick(color) }
        flexbox.addView(colorView)
      }
  }
}