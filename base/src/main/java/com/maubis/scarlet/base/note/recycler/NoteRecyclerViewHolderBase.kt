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
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.utils.trim

open class NoteRecyclerViewHolderBase(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView
  protected val tags: TextView
  protected val image: ImageView
  protected val content: TextView
  protected val edit: ImageView
  protected val share: ImageView
  protected val delete: ImageView
  protected val copy: ImageView
  protected val moreOptions: ImageView
  protected val bottomLayout: View

  protected val pinIndicator: ImageView
  protected val unlockIndicator: ImageView
  protected val reminderIndicator: ImageView
  protected val stateIndicator: ImageView
  protected val backupIndicator: ImageView

  init {
    this.view = view as CardView
    tags = view.findViewById(R.id.tags)
    image = view.findViewById(R.id.image)
    content = view.findViewById(R.id.description)
    share = view.findViewById(R.id.share_button)
    delete = view.findViewById(R.id.delete_button)
    copy = view.findViewById(R.id.copy_button)
    moreOptions = view.findViewById(R.id.options_button)
    pinIndicator = view.findViewById(R.id.pin_icon)
    unlockIndicator = view.findViewById(R.id.unlock_icon)
    reminderIndicator = view.findViewById(R.id.reminder_icon)
    edit = view.findViewById(R.id.edit_button)
    bottomLayout = view.findViewById(R.id.bottom_toolbar_layout)
    stateIndicator = view.findViewById(R.id.state_icon)
    backupIndicator = view.findViewById(R.id.backup_icon)
  }

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
      false
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
    val isImageAvailable = !item.imageSource.isBlank()
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
      !TextUtils.isNullOrEmpty(item.tagsSource) -> {
        tags.setTextColor(item.tagsColor)
        val source = item.tags
        tags.text = trim(source)
        tags.visibility = VISIBLE
      }
      !TextUtils.isNullOrEmpty(item.timestamp) -> {
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
