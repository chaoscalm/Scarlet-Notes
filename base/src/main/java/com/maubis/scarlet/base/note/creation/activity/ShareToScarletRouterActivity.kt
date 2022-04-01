package com.maubis.scarlet.base.note.creation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.Formats
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import java.io.File

const val KEEP_PACKAGE = "com.google.android.keep"

class ShareToScarletRouterActivity : AppCompatActivity() {

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
    val sharedImages = when {
      intent.action == Intent.ACTION_SEND -> handleSendImage(intent)
      intent.action == Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleImages(intent)
      else -> emptyList()
    }
    if (sharedText.isBlank() && sharedSubject.isBlank() && sharedImages.isEmpty()) {
      return null
    }

    val note = when (isCallerKeep()) {
      true -> NoteBuilder.gen(sharedSubject, NoteBuilder.genFromKeep(sharedText))
      false -> NoteBuilder.gen(sharedSubject, sharedText)
    }
    note.save(this)

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

  private fun isCallerKeep(): Boolean {
    return try {
      when {
        OsVersionUtils.canExtractReferrer() && (referrer?.toString()
          ?: "").contains(KEEP_PACKAGE) -> true
        callingPackage?.contains(KEEP_PACKAGE) ?: false -> true
        (intent?.`package` ?: "").contains(KEEP_PACKAGE) -> true
        else -> false
      }
    } catch (exception: Exception) {
      false
    }
  }
}
