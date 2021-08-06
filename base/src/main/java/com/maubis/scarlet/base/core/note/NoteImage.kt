package com.maubis.scarlet.base.core.note

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.support.utils.logNonCriticalError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

interface ImageLoadCallback {
  fun onSuccess()

  fun onError()
}

class NoteImage(context: Context) {

  val rootFolder = File(context.filesDir, "images")

  fun renameOrCopy(note: Note, imageFile: File): File {
    val targetFile = getFile(note.uuid, RandomHelper.getRandom() + ".jpg")
    targetFile.mkdirs()
    deleteIfExist(targetFile)

    val renamed = imageFile.renameTo(targetFile)
    if (!renamed) {
      imageFile.copyTo(targetFile, true)
    }
    return targetFile
  }

  fun getFile(noteUUID: String, imageFormat: Format): File {
    if (imageFormat.formatType != FormatType.IMAGE) {
        logNonCriticalError(IllegalStateException("Format should be an Image"))
    }
    return getFile(noteUUID, imageFormat.text)
  }

  fun getFile(noteUUID: String, formatFileName: String): File {
    return File(rootFolder, noteUUID + File.separator + formatFileName)
  }

  fun deleteAllFiles(note: Note) {
    for (format in FormatBuilder().getFormats(note.description)) {
      if (format.formatType === FormatType.IMAGE) {
        val file = getFile(note.uuid, format)
        deleteIfExist(file)
      }
    }
  }

  fun loadPersistentFileToImageView(image: ImageView, file: File, callback: ImageLoadCallback? = null) {
    GlobalScope.launch {
      if (!file.exists()) {
        withContext(Dispatchers.Main) {
          image.visibility = View.GONE
          callback?.onError()
        }
        return@launch
      }

      val bitmap = ScarletApp.imageCache.loadFromCache(file)
      if (bitmap === null) {
        deleteIfExist(file)
        withContext(Dispatchers.Main) {
          image.visibility = View.GONE
          callback?.onError()
        }
        return@launch
      }

      withContext(Dispatchers.Main) {
        image.visibility = View.VISIBLE
        image.setImageBitmap(bitmap)
      }
    }
  }

  fun loadThumbnailFileToImageView(noteUUID: String, imageUuid: String, image: ImageView) {
    GlobalScope.launch {
      val thumbnailFile = ScarletApp.imageCache.thumbnailFile(noteUUID, imageUuid)
      val persistentFile = ScarletApp.imageCache.persistentFile(noteUUID, imageUuid)

      if (!persistentFile.exists()) {
        withContext(Dispatchers.Main) { image.visibility = View.GONE }
        return@launch
      }

      if (thumbnailFile.exists()) {
        val bitmap = ScarletApp.imageCache.loadFromCache(thumbnailFile)
        if (bitmap === null) {
          deleteIfExist(thumbnailFile)
          withContext(Dispatchers.Main) { image.visibility = View.GONE }
          return@launch
        }

        withContext(Dispatchers.Main) {
          image.visibility = View.VISIBLE
          image.setImageBitmap(bitmap)
        }
        return@launch
      }

      val persistentBitmap = ScarletApp.imageCache.loadFromCache(persistentFile)
      if (persistentBitmap === null) {
        deleteIfExist(persistentFile)
        withContext(Dispatchers.Main) { image.visibility = View.GONE }
        return@launch
      }

      val compressedBitmap = ScarletApp.imageCache.saveThumbnail(thumbnailFile, persistentBitmap)
      withContext(Dispatchers.Main) {
        image.visibility = View.VISIBLE
        image.setImageBitmap(compressedBitmap)
      }
    }
  }

  companion object {
    fun deleteIfExist(file: File): Boolean {
      return when {
        file.exists() -> file.delete()
        else -> false
      }
    }
  }
}