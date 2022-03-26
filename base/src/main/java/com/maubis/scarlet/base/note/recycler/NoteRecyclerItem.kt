package com.maubis.scarlet.base.note.recycler

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.settings.sNoteItemLineCount
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.utils.ColorUtil

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  val lineCount = sNoteItemLineCount
  val backgroundColor = note.adjustedColor()
  val isLightShaded = ColorUtil.isLightColored(backgroundColor)

  val content = note.getLockedAwareTextForHomeList()
  val contentColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val indicatorColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_tertiary_text)
  }

  val hasReminder = note.reminder !== null
  val actionBarIconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val tagsSource = note.getTagString()
  val tags = Markdown.renderSegment(tagsSource, true)
  val tagsColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val timestamp = note.getDisplayTime()
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, R.color.light_hint_text)
  }

  val imageSource = note.getImageFile()

  override val type = Type.NOTE
}
