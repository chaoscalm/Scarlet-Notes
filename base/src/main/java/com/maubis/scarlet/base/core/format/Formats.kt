package com.maubis.scarlet.base.core.format

import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.scarlet.base.note.convertToFormats
import com.maubis.scarlet.base.support.utils.logNonCriticalError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object Formats {
  private const val JSON_KEY_NOTE = "note"

  fun getNoteContent(formats: List<Format>): String {
    val array = JSONArray()
    for (format in formats) {
      val json = format.toJson()
      if (json != null) array.put(json)
    }

    return JSONObject().put(JSON_KEY_NOTE, array).toString()
  }

  fun getFormatsFromNoteContent(noteContent: String): List<Format> {
    val formats = ArrayList<Format>()
    try {
      val array = JSONObject(noteContent).getJSONArray(JSON_KEY_NOTE)
      for (index in 0 until array.length()) {
        try {
          val format = Format.fromJson(array.getJSONObject(index))
          format.uid = formats.size
          formats.add(format)
        } catch (innerException: JSONException) {
          logNonCriticalError(innerException)
        }
      }
    } catch (exception: Exception) {
      logNonCriticalError(exception)
    }
    return formats
  }

  fun getEnhancedFormatsForNoteView(formats: List<Format>): List<Format> {
    var maxIndex = formats.size
    val enhancedFormats = mutableListOf<Format>()
    formats.forEach {
      if (it.formatType == FormatType.TEXT) {
        val moreFormats = it.text.convertToFormats()
        moreFormats.forEach { format ->
          format.uid = maxIndex
          enhancedFormats.add(format)
          maxIndex += 1
        }
      } else {
        enhancedFormats.add(it)
      }
    }
    return enhancedFormats
  }


  fun getEnhancedNoteContent(formats: List<Format>): String {
    val enhancedFormats = enhanceTextFormats(formats)
    return getNoteContent(enhancedFormats)
  }

  private fun enhanceTextFormats(formats: List<Format>): List<Format> {
    val newFormats = mutableListOf<Format>()
    for (format in formats) {
      if (format.formatType != FormatType.TEXT) {
        newFormats.add(format)
        continue
      }

      val moreFormats = format.text.convertToFormats(
        arrayOf(
          MarkdownSegmentType.HEADING_1,
          MarkdownSegmentType.HEADING_2,
          MarkdownSegmentType.HEADING_3,
          MarkdownSegmentType.CODE,
          MarkdownSegmentType.SEPARATOR,
          MarkdownSegmentType.CHECKLIST_CHECKED,
          MarkdownSegmentType.CHECKLIST_UNCHECKED))
      newFormats.addAll(moreFormats)
    }
    return newFormats
  }
}
