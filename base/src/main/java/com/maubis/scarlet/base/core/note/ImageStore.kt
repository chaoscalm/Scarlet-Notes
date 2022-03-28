package com.maubis.scarlet.base.core.note

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.support.utils.ImageCache
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

class ImageStore(context: Context, private val thumbnailsCache: ImageCache) {

  private val rootFolder = File(context.filesDir, "images")

  fun renameOrCopy(note: Note, imageFile: File): File {
    val targetFile = getFile(note.uuid.toString(), RandomHelper.getRandom() + ".jpg")
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
    thumbnailsCache.deleteThumbnails(note)
    deleteImages(note)
  }

  private fun deleteImages(note: Note) {
    GlobalScope.launch(Dispatchers.IO) {
      val folder = File(rootFolder, note.uuid.toString())
      folder.deleteRecursively()
    }
  }

  fun loadImageToImageView(image: ImageView, file: File, callback: ImageLoadCallback? = null) {
    GlobalScope.launch {
      if (!file.exists()) {
        withContext(Dispatchers.Main) {
          image.visibility = View.GONE
          callback?.onError()
        }
        return@launch
      }

      val bitmap = loadBitmap(file)
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

  fun loadThumbnailToImageView(noteUUID: String, imageUuid: String, image: ImageView) {
    GlobalScope.launch {
      val thumbnailFile = thumbnailsCache.thumbnailFile(noteUUID, imageUuid)
      val imageFile = getFile(noteUUID, imageUuid)

      if (!imageFile.exists()) {
        withContext(Dispatchers.Main) { image.visibility = View.GONE }
        return@launch
      }

      if (thumbnailFile.exists()) {
        val bitmap = loadBitmap(thumbnailFile)
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

      val bitmap = loadBitmap(imageFile)
      if (bitmap == null) {
        deleteIfExist(imageFile)
        withContext(Dispatchers.Main) { image.visibility = View.GONE }
        return@launch
      }

      val compressedBitmap = thumbnailsCache.saveThumbnail(thumbnailFile, bitmap)
      withContext(Dispatchers.Main) {
        image.visibility = View.VISIBLE
        image.setImageBitmap(compressedBitmap)
      }
    }
  }

  private fun loadBitmap(imageFile: File): Bitmap? {
    if (imageFile.exists()) {
      val options = BitmapFactory.Options()
      options.inPreferredConfig = Bitmap.Config.ARGB_8888
      return BitmapFactory.decodeFile(imageFile.absolutePath, options)
    }
    return null
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