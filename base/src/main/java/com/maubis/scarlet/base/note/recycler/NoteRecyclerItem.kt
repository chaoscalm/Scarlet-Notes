package com.maubis.scarlet.base.note.recycler

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.adjustedColor
import com.maubis.scarlet.base.note.getDisplayTime
import com.maubis.scarlet.base.note.getImageFile
import com.maubis.scarlet.base.note.getTagString

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  val backgroundColor = note.adjustedColor()
  private val isLightShaded = ColorUtil.isLightColor(backgroundColor)

  val contentColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_primary_text)
  }

  val indicatorColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_tertiary_text)
  }

  val hasReminder = note.reminder !== null
  val actionBarIconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_secondary_text)
  }

  val tagsString = note.getTagString()
  val tagsColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_secondary_text)
  }

  val timestamp = note.getDisplayTime(context)
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, com.github.bijoysingh.uibasics.R.color.light_hint_text)
  }

  val imageSource = note.getImageFile()

  override val type = Type.NOTE
}
