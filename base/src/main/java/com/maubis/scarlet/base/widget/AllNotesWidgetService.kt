package com.maubis.scarlet.base.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.getTextForWidget
import com.maubis.scarlet.base.settings.SortingOptionsBottomSheet
import com.maubis.scarlet.base.settings.sWidgetShowArchivedNotes
import com.maubis.scarlet.base.settings.sWidgetShowDeletedNotes
import com.maubis.scarlet.base.settings.sWidgetShowLockedNotes
import com.maubis.scarlet.base.support.utils.ColorUtil

fun getWidgetNotes(): List<Note> {
  val state = listOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name).toMutableList()
  if (sWidgetShowArchivedNotes) {
    state.add(NoteState.ARCHIVED.name)
  }
  if (sWidgetShowDeletedNotes) {
    state.add(NoteState.TRASH.name)
  }

  val sorting = SortingOptionsBottomSheet.getSortingState()
  return sort(ScarletApp.data.notes.getByNoteState(state.toTypedArray())
          .filter { note -> (!note.locked || sWidgetShowLockedNotes) }, sorting)
}

class AllNotesWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
    return AllNotesRemoteViewsFactory(applicationContext)
  }
}

class AllNotesRemoteViewsFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {

  var notes = emptyList<Note>()

  override fun onCreate() {
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getItemId(position: Int): Long {
    return if (position < notes.size) notes[position].uid.toLong() else 0
  }

  override fun onDataSetChanged() {
    notes = getWidgetNotes().take(15)
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getViewAt(position: Int): RemoteViews? {
    if (position == AdapterView.INVALID_POSITION || position >= notes.size) {
      return null
    }

    val note = notes[position]

    val views = RemoteViews(context.getPackageName(), R.layout.item_widget_note)

    views.setTextViewText(R.id.description, note.getTextForWidget())
    views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

    val isLightShaded = ColorUtil.isLightColored(note.color)
    val colorResource = if (isLightShaded) R.color.dark_tertiary_text else R.color.light_secondary_text
    val textColor = ContextCompat.getColor(context, colorResource)
    views.setInt(R.id.description, "setTextColor", textColor)

    val extras = Bundle()
    extras.putInt(INTENT_KEY_NOTE_ID, note.uid)
    val fillInIntent = Intent()
    fillInIntent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
    fillInIntent.putExtras(extras)

    views.setOnClickFillInIntent(R.id.description, fillInIntent)
    views.setOnClickFillInIntent(R.id.container_layout, fillInIntent)

    return views
  }

  override fun getCount(): Int {
    return notes.size
  }

  override fun getViewTypeCount(): Int {
    return 1
  }

  override fun onDestroy() {

  }

}