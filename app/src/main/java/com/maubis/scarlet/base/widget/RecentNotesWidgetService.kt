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
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.getTextForWidget

fun getAvailableNotesForWidgets(): List<Note> {
  val states = mutableListOf(NoteState.DEFAULT, NoteState.FAVOURITE)
  if (ScarletApp.prefs.showArchivedNotesInWidgets) {
    states.add(NoteState.ARCHIVED)
  }

  return ScarletApp.data.notes.getByNoteState(*states.toTypedArray())
    .filter { note -> (!note.locked || ScarletApp.prefs.showLockedNotesInWidgets) }
}

class RecentNotesWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
    return RecentNotesRemoteViewsFactory(applicationContext)
  }
}

class RecentNotesRemoteViewsFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {
  private var notes = emptyList<Note>()

  override fun onCreate() {}

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getItemId(position: Int): Long {
    return if (position < notes.size) notes[position].uid.toLong() else 0
  }

  override fun onDataSetChanged() {
    notes = getAvailableNotesForWidgets()
      .sortedByDescending { it.updateTimestamp }
      .take(15)
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getViewAt(position: Int): RemoteViews? {
    if (position == AdapterView.INVALID_POSITION || position >= notes.size) {
      return null
    }

    val note = notes[position]
    val views = RemoteViews(context.packageName, R.layout.item_widget_note)
    views.setTextViewText(R.id.description, note.getTextForWidget())
    views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

    val isLightShaded = ColorUtil.isLightColor(note.color)
    val colorResource = if (isLightShaded) {
      com.github.bijoysingh.uibasics.R.color.dark_tertiary_text
    } else {
      com.github.bijoysingh.uibasics.R.color.light_secondary_text
    }
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

  override fun getCount(): Int = notes.size

  override fun getViewTypeCount(): Int = 1

  override fun onDestroy() {}
}