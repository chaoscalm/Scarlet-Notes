package com.maubis.scarlet.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.editor.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.settings.sEditorSkipNoteViewer
import java.util.*

class ScarletIntentHandlerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data: Uri? = intent?.data
    if (data === null) {
      finish()
      return
    }

    handleOpenNote(data)
    handleCreateNote(data)
    finish()
  }

  private fun handleOpenNote(data: Uri): Boolean {
    if (data.host != "open_note") {
      return false
    }

    val noteUUID = data.getQueryParameter("uuid")
    if (noteUUID === null) {
      return false
    }

    val note = ScarletApp.data.notes.getByUUID(UUID.fromString(noteUUID))
    if (note === null) {
      return false
    }

    val intent = when (data.getQueryParameter("is_edit", false)) {
      true -> edit(this, note)
      false -> view(this, note)
    }
    startActivity(intent)
    return true
  }

  private fun handleCreateNote(data: Uri): Boolean {
    if (data.host != "create_note") {
      return false
    }

    startActivity(create(this))
    return true
  }

  companion object {

    private fun Uri.getQueryParameter(key: String, defaultValue: Boolean): Boolean {
      val param = getQueryParameter(key)
      if (param === null) {
        return defaultValue
      }
      return param == "1"
    }

    fun view(context: Context, note: Note): Intent {
      if (sEditorSkipNoteViewer) {
        return edit(context, note)
      }

      return Intent(context, ViewAdvancedNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
    }

    fun edit(context: Context, note: Note): Intent {
      return Intent(context, EditNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
    }

    fun create(context: Context): Intent {
      return Intent(context, EditNoteActivity::class.java)
    }

    fun view(note: Note): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("open_note")
        .appendQueryParameter("uuid", note.uuid.toString())
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }

    fun edit(note: Note): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("open_note")
        .appendQueryParameter("uuid", note.uuid.toString())
        .appendQueryParameter("is_edit", "1")
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }

    fun create(): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("create_note")
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }
  }
}
