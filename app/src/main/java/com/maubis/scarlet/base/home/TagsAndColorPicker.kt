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
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.database.entities.Tag

class TagsAndColorPicker(
  private val activity: MainActivity,
  private val flexbox: FlexboxLayout,
  private val onTagClick: (Tag) -> Unit,
  private val onColorClick: (Int) -> Unit) {

  private val tags = mutableSetOf<Tag>()
  private val colors = mutableSetOf<Int>()

  fun loadData() {
    tags.clear()
    tags.addAll(data.tags.getAll())

    colors.clear()
    colors.addAll(data.notes.getAll().map { it.color }.sorted())
  }

  fun refreshUI() {
    flexbox.removeAllViews()
    populateTags()
    populateColors()
  }

  private fun populateTags() {
    tags.forEach { tag ->
      val tagView = buildViewForTag(tag)
      flexbox.addView(tagView)
    }
  }

  private fun populateColors() {
    colors.take(12).forEach { color ->
      val colorView = buildViewForColor(color)
      flexbox.addView(colorView)
    }
  }

  private fun buildViewForTag(tag: Tag): View {
    val tagView = View.inflate(activity, R.layout.layout_flexbox_tag_item, null)
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
      val themeColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)
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