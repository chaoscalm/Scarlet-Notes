package com.maubis.scarlet.base.home

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.ui.ColorView
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.database.entities.Tag

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
    tags.toList()
      .subList(0, tags.size.coerceAtMost(6))
      .forEach { tag ->
        val tagView = buildViewForTag(tag)
        flexbox.addView(tagView)
      }
  }

  private fun setColors() {
    colors.toList()
      .subList(0, colors.size.coerceAtMost(6))
      .forEach { color ->
        val colorView = buildViewForColor(color)
        flexbox.addView(colorView)
      }
  }

  private fun buildViewForTag(tag: Tag): View {
    val tagView = View.inflate(activity, R.layout.layout_flexbox_tag_item, null) as View
    val text = tagView.findViewById<TextView>(R.id.tag_text)
    text.text = tag.title
    text.typeface = appTypeface.title()
    setTagTextColor(tag, text)
    tagView.setOnClickListener { onTagClick(tag) }
    return tagView
  }

  private fun setTagTextColor(tag: Tag, text: TextView) {
    val backgroundDrawable = activity.getDrawable(R.drawable.flexbox_selected_tag_item_bg)!!
    if (activity.state.isFilteringByTag(tag)) {
      val accentColor = ContextCompat.getColor(activity, R.color.colorAccent)
      backgroundDrawable.setTint(accentColor)
      text.setTextColor(accentColor)
    } else {
      val themeColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)
      backgroundDrawable.setTint(themeColor)
      text.setTextColor(themeColor)
    }
    text.background = backgroundDrawable
  }

  private fun buildViewForColor(color: Int): View {
    val colorView = ColorView(activity, R.layout.layout_color_small)
    colorView.setColor(color, activity.state.colors.contains(color))
    colorView.setOnClickListener { onColorClick(color) }
    return colorView
  }
}