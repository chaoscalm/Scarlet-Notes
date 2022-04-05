package com.maubis.scarlet.base.note.recycler

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.*
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.settings.sNoteItemLineCount

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  val lineCount = sNoteItemLineCount
  val backgroundColor = note.adjustedColor()
  private val isLightShaded = ColorUtil.isLightColor(backgroundColor)

  val content = getLockedAwarePreviewText(note)
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

  val tagsSource = note.getTagString()
  val tags = Markdown.renderSegment(tagsSource, true)
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

  private fun getLockedAwarePreviewText(note: Note): CharSequence {
    val lockedText = "*********************************************"
    return when {
      note.isLockedButAppUnlocked() || !note.locked -> {
        // Avoid UI lag in notes list when note is huge.
        // 1500 characters are enough to display 15 lines of note preview on a full-width column
        // in landscape orientation.
        val text = note.getFullTextForDirectMarkdownRender().take(1500)
        renderMarkdownForList(text)
      }
      else -> renderMarkdownForList("# ${note.getTitleForSharing()}\n\n```\n$lockedText\n```")
    }
  }

  private fun renderMarkdownForList(text: String): CharSequence {
    return Markdown.renderWithCustomFormatting(text, strip = true) { spannable, spanInfo ->
      val start = spanInfo.start
      val end = spanInfo.end
      when (spanInfo.markdownType) {
        MarkdownType.HEADING_1 -> {
          spannable.relativeSize(1.2f, start, end)
            .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
            .bold(start, end)
          true
        }
        MarkdownType.HEADING_2 -> {
          spannable.relativeSize(1.1f, start, end)
            .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
            .bold(start, end)
          true
        }
        MarkdownType.HEADING_3 -> {
          spannable.relativeSize(1.0f, start, end)
            .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
            .bold(start, end)
          true
        }
        MarkdownType.CHECKLIST_CHECKED -> {
          spannable.strike(start, end)
          true
        }
        else -> false
      }
    }
  }
}
