package com.maubis.scarlet.base.core.format

import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.scarlet.base.note.convertToFormats
import com.maubis.scarlet.base.support.utils.logNonCriticalError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class FormatBuilder {
  val KEY_NOTE = "note"

  @Throws(JSONException::class)
  fun fromJson(json: JSONObject): Format {
    val format = Format()
    format.formatType = FormatType.valueOf(json.getString("format"))
    format.text = json.getString("text")
    return format
  }

  fun getContent(formats: List<Format>): String {
    val array = JSONArray()
    for (format in formats) {
      val json = format.toJson()
      if (json != null) array.put(json)
    }

    val cache = mapOf(KEY_NOTE to array)
    return JSONObject(cache).toString()
  }

  fun getSmarterContent(formats: List<Format>): String {
    val extractedFormats = mutableListOf<Format>()
    for (format in formats) {
      if (format.formatType != FormatType.TEXT) {
        extractedFormats.add(format)
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
      extractedFormats.addAll(moreFormats)
    }
    return getContent(extractedFormats)
  }

  fun getFormats(note: String): List<Format> {
    val formats = ArrayList<Format>()
    try {
      val json = JSONObject(note)
      val array = json.getJSONArray(KEY_NOTE)
      for (index in 0 until array.length()) {
        try {
          val format = fromJson(array.getJSONObject(index))
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

  fun getNextFormatType(type: FormatType): FormatType {
    return when (type) {
      FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3 -> FormatType.TEXT
      FormatType.CHECKLIST_CHECKED, FormatType.CHECKLIST_UNCHECKED -> FormatType.CHECKLIST_UNCHECKED
      FormatType.NUMBERED_LIST -> FormatType.NUMBERED_LIST
      else -> FormatType.TEXT
    }
  }
}
