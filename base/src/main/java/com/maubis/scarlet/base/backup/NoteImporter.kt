package com.maubis.scarlet.base.backup

import android.content.Context
import android.os.Environment
import com.github.bijoysingh.starter.async.Parallel
import com.google.gson.Gson
import com.maubis.scarlet.base.backup.data.ExportFileFormat
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.support.utils.logNonCriticalError
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class NoteImporter {
  fun importFromBackupContent(context: Context, content: String) {
    try {
        val fileFormat = Gson().fromJson(content, ExportFileFormat::class.java)
        if (fileFormat == null) {
          importNoteFallback(content, context)
          return
        }
        fileFormat.tags.forEach {
          it.saveIfNotPresent()
        }
        fileFormat.notes.forEach {
          it.saveIfNeeded(context)
        }
        fileFormat.folders?.forEach {
          it.saveIfNotPresent()
        }
    } catch (exception: Exception) {
      importNoteFallback(content, context)
      logNonCriticalError(exception)
    }
  }

  private fun importNoteFallback(content: String, context: Context) {
    content
      .split(EXPORT_NOTE_SEPARATOR)
      .map {
        it.trim()
      }
      .filter { it.isNotBlank() }
      .forEach {
        NoteBuilder.gen("", it).save(context)
      }
  }

  fun getImportableFiles(): List<File> {
    return getFiles(Environment.getExternalStorageDirectory())
  }

  private fun getFiles(directory: File): List<File> {
    val files = ArrayList<File>()
    val folders = ArrayList<File>()
    val allFiles = directory.listFiles()
    if (allFiles != null) {
      for (file in allFiles) {
        if (file.isDirectory()) {
          folders.add(file)
        } else if (isValidFile(file.getPath())) {
          files.add(file)
        }
      }
    }

    val parallel = Parallel<File, List<File>>()
    parallel.setListener { input -> getFiles(input) }

    try {
      val childFiles = parallel.For(folders)
      for (childFile in childFiles) {
        files.addAll(childFile)
      }
    } catch (exception: Exception) {
      logNonCriticalError(exception)
    }

    return files
  }

  fun readFileInputStream(inputStreamReader: InputStreamReader): String {
    lateinit var reader: BufferedReader
    try {
      reader = BufferedReader(inputStreamReader)
      val fileContents = StringBuilder()
      var line: String? = reader.readLine()
      while (line != null) {
        fileContents.append(line + "\n")
        line = reader.readLine()
      }
      return fileContents.toString()
    } catch (exception: IOException) {
      reader.close()
      logNonCriticalError(exception)
      return ""
    }
  }

  private fun isValidFile(filePath: String): Boolean {
    val validExtensions = arrayOf("txt", "md")
    return validExtensions.firstOrNull { isValidFile(filePath, it) } !== null
  }

  private fun isValidFile(filePath: String, validExtension: String): Boolean {
    return filePath.endsWith("." + validExtension) ||
            filePath.endsWith("." + validExtension.uppercase())
  }

}