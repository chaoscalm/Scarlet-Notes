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
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.utils.trim
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked

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

    view.alpha = if (item.note.isNoteLockedButAppUnlocked()) 0.7f else 1.0f
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
    content.text = item.content
    content.maxLines = item.lineCount
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
        stateIndicator.setImageResource(R.drawable.ic_favorite_white_48dp)
      }
      NoteState.ARCHIVED -> {
        stateIndicator.visibility = VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_archive_white_48dp)
      }
      NoteState.TRASH -> {
        stateIndicator.visibility = VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_delete_white_48dp)
      }
      NoteState.DEFAULT -> stateIndicator.visibility = GONE
    }
    unlockIndicator.isVisible = item.note.locked

    pinIndicator.setColorFilter(item.indicatorColor)
    stateIndicator.setColorFilter(item.indicatorColor)
    reminderIndicator.setColorFilter(item.indicatorColor)
    backupIndicator.setColorFilter(item.indicatorColor)
    unlockIndicator.setColorFilter(item.indicatorColor)
  }

  private fun setMetaText(item: NoteRecyclerItem) {
    tags.typeface = appTypeface.text()
    when {
      item.tagsSource.isNotEmpty() -> {
        tags.setTextColor(item.tagsColor)
        val source = item.tags
        tags.text = trim(source)
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

    delete.setColorFilter(item.actionBarIconColor)
    share.setColorFilter(item.actionBarIconColor)
    edit.setColorFilter(item.actionBarIconColor)
    copy.setColorFilter(item.actionBarIconColor)
    moreOptions.setColorFilter(item.actionBarIconColor)
  }

  protected open fun viewClick(note: Note, extra: Bundle?) {}
  protected open fun viewLongClick(note: Note, extra: Bundle?) {}

  protected open fun deleteIconClick(note: Note, extra: Bundle?) {}
  protected open fun shareIconClick(note: Note, extra: Bundle?) {}
  protected open fun editIconClick(note: Note, extra: Bundle?) {}
  protected open fun copyIconClick(note: Note, extra: Bundle?) {}
  protected open fun moreOptionsIconClick(note: Note, extra: Bundle?) {}
}
