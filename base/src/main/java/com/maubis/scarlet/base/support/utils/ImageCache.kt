package com.maubis.scarlet.base.support.utils

import android.content.Context
import android.graphics.Bitmap
import com.maubis.scarlet.base.database.entities.Note
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong

const val IMAGE_CACHE_SIZE = 1024 * 1024 * 10L

@OptIn(DelicateCoroutinesApi::class)
class ImageCache(context: Context) {

  private val thumbnailFolder = File(context.cacheDir, "thumbnails")
  private var thumbnailCacheSize = AtomicLong(0L)

  init {
    thumbnailFolder.mkdirs()

    GlobalScope.launch(Dispatchers.IO) {
      val files = thumbnailFolder.listFiles()
      files?.forEach { thumbnailCacheSize.addAndGet(it.length()) }
    }
  }

  fun thumbnailFile(noteUUID: String, formatFileName: String): File {
    return File(thumbnailFolder, "$noteUUID::$formatFileName")
  }

  fun saveThumbnail(cacheFile: File, bitmap: Bitmap): Bitmap {
    if (cacheFile.exists()) {
      thumbnailCacheSize.addAndGet(-cacheFile.length())
    }

    val compressedBitmap: Bitmap = sampleBitmap(bitmap)

    try {
      val fOut = FileOutputStream(cacheFile)
      compressedBitmap.compress(Bitmap.CompressFormat.PNG, 75, fOut)
      fOut.flush()
      fOut.close()
    } catch (exception: Exception) {
      logNonCriticalError(exception)
      return compressedBitmap
    }

    thumbnailCacheSize.addAndGet(cacheFile.length())
    performEviction()
    return compressedBitmap
  }

  fun deleteThumbnails(note: Note) {
    GlobalScope.launch(Dispatchers.IO) {
      thumbnailFolder
          .listFiles { file -> file.name.startsWith(note.uuid.toString()) }
          ?.forEach { it.delete() }
    }
  }

  private fun sampleBitmap(bitmap: Bitmap): Bitmap {
    val cropDimension = Math.min(bitmap.width, bitmap.height)
    val destinationBitmap = Bitmap.createBitmap(bitmap, 0, 0, cropDimension, cropDimension)
    return Bitmap.createScaledBitmap(destinationBitmap, 256, 256, false)
  }

  @Synchronized
  private fun performEviction() {
    if (thumbnailCacheSize.get() <= IMAGE_CACHE_SIZE) {
      return
    }

    GlobalScope.launch(Dispatchers.IO) {
      var index = 0
      val files = thumbnailFolder.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
      while (thumbnailCacheSize.get() > IMAGE_CACHE_SIZE * 0.9 && index < files.size) {
        thumbnailCacheSize.addAndGet(-files[index].length())
        files[index].delete()
        index++
      }
    }
  }
}