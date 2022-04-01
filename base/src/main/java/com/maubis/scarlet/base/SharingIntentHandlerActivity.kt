package com.maubis.scarlet.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.FormatType
import com.maubis.scarlet.base.editor.formats.Formats
import java.io.File

class SharingIntentHandlerActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      val note = handleSendText(intent)
      if (note === null) {
        return
      }
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
    } finally {
      finish()
    }
  }

  private fun handleSendText(intent: Intent): Note? {
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
          ?: intent.getStringExtra(Intent.EXTRA_TITLE) ?: ""
    val sharedImages = when (intent.action) {
      Intent.ACTION_SEND -> handleSendImage(intent)
      Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleImages(intent)
      else -> emptyList()
    }
    if (sharedText.isBlank() && sharedSubject.isBlank() && sharedImages.isEmpty()) {
      return null
    }

    val note = Note.create(sharedSubject, sharedText)
    val images = mutableListOf<File>()
    for (uri in sharedImages) {
      try {
        contentResolver.openInputStream(uri)?.use {
          images.add(imageStorage.saveImage(note, it))
        }
      } catch (e: Exception) {
        Log.w("Scarlet", "Unable to save image $uri", e)
      }
    }
    val formats = note.getFormats().toMutableList()
    for (image in images.reversed()) {
      formats.add(0, Format(FormatType.IMAGE, image.name))
    }
    note.content = Formats.getEnhancedNoteContent(formats)
    note.save(this)
    return note
  }

  private fun handleSendImage(intent: Intent): List<Uri> {
    val images = mutableListOf<Uri>()
    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
      images.add(it)
    }
    return images
  }

  private fun handleSendMultipleImages(intent: Intent): List<Uri> {
    val images = mutableListOf<Uri>()
    intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
      for (parcelable in it) {
        if (parcelable is Uri) {
          images.add(parcelable)
        }
      }
    }
    return images
  }
}
