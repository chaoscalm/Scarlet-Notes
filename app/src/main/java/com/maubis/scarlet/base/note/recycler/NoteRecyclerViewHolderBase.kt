package com.maubis.scarlet.base.note.recycler

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.*
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.setIconTint
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.note.getFullTextForDirectMarkdownRender
import com.maubis.scarlet.base.note.getTitleForSharing
import com.maubis.scarlet.base.note.isLockedButAppUnlocked

open class NoteRecyclerViewHolderBase(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView = view as CardView
  protected val tags: TextView = view.findViewById(R.id.tags)
  protected val image: ImageView = view.findViewById(R.id.image)
  protected val content: TextView = view.findViewById(R.id.description)
  protected val edit: ImageView = view.findViewById(R.id.edit_button)
  protected val share: ImageView = view.findViewById(R.id.share_button)
  protected val delete: ImageView = view.findViewById(R.id.delete_button)
  protected val copy: ImageView = view.findViewById(R.id.copy_button)
  protected val moreOptions: ImageView = view.findViewById(R.id.options_button)

  private val pinIndicator: ImageView = view.findViewById(R.id.pin_icon)
  private val unlockIndicator: ImageView = view.findViewById(R.id.unlock_icon)
  private val reminderIndicator: ImageView = view.findViewById(R.id.reminder_icon)
  private val stateIndicator: ImageView = view.findViewById(R.id.state_icon)
  private val backupIndicator: ImageView = view.findViewById(R.id.backup_icon)

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as NoteRecyclerItem
    setContent(item)
    setImage(item)
    setIndicators(item)
    setMetaText(item)

    view.alpha = if (item.note.isLockedButAppUnlocked()) 0.7f else 1.0f
    view.setOnClickListener { viewClick(item.note, extra) }
    view.setOnLongClickListener {
      viewLongClick(item.note, extra)
      true
    }
    view.setCardBackgroundColor(item.backgroundColor)
    setActionBar(item, extra)
  }

  private fun setContent(item: NoteRecyclerItem) {
    content.setTypeface(appTypeface.text(), Typeface.NORMAL)
    content.text = getLockedAwarePreviewText(item.note)
    content.maxLines = ScarletApp.prefs.notePreviewLines
    content.setTextColor(item.contentColor)
  }

  private fun setImage(item: NoteRecyclerItem) {
    val isImageAvailable = item.imageSource.isNotBlank()
    image.isVisible = isImageAvailable
    if (isImageAvailable) {
      imageStorage.loadThumbnailToImageView(item.note.uuid.toString(), item.imageSource, image)
    }
  }

  private fun setIndicators(item: NoteRecyclerItem) {
    pinIndicator.isVisible = item.note.pinned
    reminderIndicator.isVisible = item.hasReminder
    backupIndicator.isVisible = item.note.excludeFromBackup
    when (item.note.state) {
      NoteState.FAVOURITE -> {
        stateIndicator.visibility = VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_favorite)
      }
      NoteState.ARCHIVED -> {
        stateIndicator.visibility = VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_archive)
      }
      NoteState.TRASH -> {
        stateIndicator.visibility = VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_delete)
      }
      NoteState.DEFAULT -> stateIndicator.visibility = GONE
    }
    unlockIndicator.isVisible = item.note.locked

    pinIndicator.setIconTint(item.indicatorColor)
    stateIndicator.setIconTint(item.indicatorColor)
    reminderIndicator.setIconTint(item.indicatorColor)
    backupIndicator.setIconTint(item.indicatorColor)
    unlockIndicator.setIconTint(item.indicatorColor)
  }

  private fun setMetaText(item: NoteRecyclerItem) {
    tags.typeface = appTypeface.text()
    when {
      item.tagsString.isNotEmpty() -> {
        tags.setTextColor(item.tagsColor)
        tags.text = Markdown.renderSegment(item.tagsString, true)
        tags.visibility = VISIBLE
      }
      item.timestamp.isNotEmpty() -> {
        tags.setTextColor(item.timestampColor)
        tags.text = item.timestamp
        tags.visibility = VISIBLE
      }
      else -> {
        tags.visibility = GONE
      }
    }
  }

  private fun setActionBar(item: NoteRecyclerItem, extra: Bundle?) {
    delete.setOnClickListener { deleteIconClick(item.note, extra) }
    share.setOnClickListener { shareIconClick(item.note, extra) }
    edit.setOnClickListener { editIconClick(item.note, extra) }
    copy.setOnClickListener { copyIconClick(item.note, extra) }
    moreOptions.setOnClickListener { moreOptionsIconClick(item.note, extra) }

    delete.setIconTint(item.actionBarIconColor)
    share.setIconTint(item.actionBarIconColor)
    edit.setIconTint(item.actionBarIconColor)
    copy.setIconTint(item.actionBarIconColor)
    moreOptions.setIconTint(item.actionBarIconColor)
  }

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

  protected open fun viewClick(note: Note, extra: Bundle?) {}
  protected open fun viewLongClick(note: Note, extra: Bundle?) {}

  protected open fun deleteIconClick(note: Note, extra: Bundle?) {}
  protected open fun shareIconClick(note: Note, extra: Bundle?) {}
  protected open fun editIconClick(note: Note, extra: Bundle?) {}
  protected open fun copyIconClick(note: Note, extra: Bundle?) {}
  protected open fun moreOptionsIconClick(note: Note, extra: Bundle?) {}
}
