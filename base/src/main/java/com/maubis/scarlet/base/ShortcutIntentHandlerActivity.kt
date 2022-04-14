package com.maubis.scarlet.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.ViewNoteActivity
import java.util.*

class ShortcutIntentHandlerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data: Uri? = intent?.data
    if (data === null) {
      finish()
      return
    }

    handleOpenNote(data)
    finish()
  }

  private fun handleOpenNote(data: Uri) {
    if (data.host != "open_note") {
      return
    }

    val noteUUID = data.getQueryParameter("uuid") ?: return
    val note = ScarletApp.data.notes.getByUUID(UUID.fromString(noteUUID)) ?: return
    startActivity(ViewNoteActivity.makePreferenceAwareIntent(this, note))
  }

  companion object {
    fun viewShortcutIntent(note: Note): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("open_note")
        .appendQueryParameter("uuid", note.uuid.toString())
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }
  }
}
