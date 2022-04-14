package com.maubis.scarlet.base.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.FormatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

interface ImageLoadCallback {
  fun onSuccess()

  fun onError()
}

class ImageStore(context: Context, private val thumbnailsCache: ImageCache) {

  private val rootFolder = File(context.filesDir, "images")

  fun saveImage(note: Note, imageStream: InputStream): File {
    val destFile = newDestinationFile(note)
    val bitmap = BitmapFactory.decodeStream(imageStream)
    bitmap.writeToFile(destFile)
    return destFile
  }

  fun storeExistingImage(note: Note, imageFile: File): File {
    val destFile = newDestinationFile(note)
    val renamed = imageFile.renameTo(destFile)
    if (!renamed) {
      imageFile.copyTo(destFile, true)
    }
    return destFile
  }

  private fun newDestinationFile(note: Note): File {
    val targetFile = getImageFile(note.uuid.toString(), RandomHelper.getRandom() + ".jpg")
    targetFile.mkdirs()
    targetFile.deleteIfExists()
    return targetFile
  }

  fun deleteImageIfExists(noteUUID: String, imageFormat: Format) {
    getImage(noteUUID, imageFormat).deleteIfExists()
  }

  fun getImage(noteUUID: String, imageFormat: Format): File {
    if (imageFormat.type != FormatType.IMAGE) {
      Log.w("Scarlet", "Attempted to retrieve image for a non-image Format")
    }
    return getImageFile(noteUUID, imageFormat.text)
  }

  private fun getImageFile(noteUUID: String, imageName: String): File {
    return File(rootFolder, noteUUID + File.separator + imageName)
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
        file.deleteIfExists()
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
      val imageFile = getImageFile(noteUUID, imageUuid)

      if (!imageFile.exists()) {
        withContext(Dispatchers.Main) { image.visibility = View.GONE }
        return@launch
      }

      if (thumbnailFile.exists()) {
        val bitmap = loadBitmap(thumbnailFile)
        if (bitmap === null) {
          thumbnailFile.deleteIfExists()
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
        imageFile.deleteIfExists()
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

  private fun Bitmap.writeToFile(destinationFile: File) {
    FileOutputStream(destinationFile).use {
      this.compress(Bitmap.CompressFormat.JPEG, 90, it)
    }
  }

  private fun File.deleteIfExists() {
    if (exists())
       delete()
  }
}