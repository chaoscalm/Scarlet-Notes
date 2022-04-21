package com.maubis.scarlet.base.editor

import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.scarlet.base.common.utils.logNonCriticalError
import com.maubis.scarlet.base.settings.sEditorMoveChecked
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object Formats {
  private const val JSON_KEY_NOTE = "note"

  fun List<Format>.toNoteContent(): String {
    val array = JSONArray()
    for (format in this) {
      val json = format.toJson()
      if (json != null) array.put(json)
    }

    return JSONObject().put(JSON_KEY_NOTE, array).toString()
  }

  fun fromNoteContent(noteContent: String): List<Format> {
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

  fun sortChecklistsPreservingSections(formats: List<Format>): List<Format> {
    if (!sEditorMoveChecked) {
      return formats
    }

    val mutableFormats = formats.toMutableList()
    var index = 0
    while (index < formats.size - 1) {
      val currentItem = mutableFormats[index]
      val nextItem = mutableFormats[index + 1]

      if (currentItem.type == FormatType.CHECKLIST_CHECKED
        && nextItem.type == FormatType.CHECKLIST_UNCHECKED) {
        Collections.swap(mutableFormats, index, index + 1)
        continue
      }
      index += 1
    }
    while (index > 0) {
      val currentItem = mutableFormats[index]
      val nextItem = mutableFormats[index - 1]

      if (currentItem.type == FormatType.CHECKLIST_UNCHECKED
        && nextItem.type == FormatType.CHECKLIST_CHECKED) {
        Collections.swap(mutableFormats, index, index - 1)
        continue
      }
      index -= 1
    }
    return mutableFormats
  }

  fun getEnhancedFormatsForNoteView(formats: List<Format>): List<Format> {
    var maxIndex = formats.size
    val enhancedFormats = mutableListOf<Format>()
    formats.forEach {
      if (it.type == FormatType.TEXT) {
        val moreFormats = convertTextToFormats(it.text)
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
    return enhancedFormats.toNoteContent()
  }

  private fun enhanceTextFormats(formats: List<Format>): List<Format> {
    val newFormats = mutableListOf<Format>()
    for (format in formats) {
      if (format.type != FormatType.TEXT) {
        newFormats.add(format)
        continue
      }

      val moreFormats = convertTextToFormats(
        format.text,
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

  private fun convertTextToFormats(text: String): List<Format> {
    return convertTextToFormats(
      text,
      arrayOf(
        MarkdownSegmentType.HEADING_1,
        MarkdownSegmentType.HEADING_2,
        MarkdownSegmentType.HEADING_3,
        MarkdownSegmentType.BULLET_1,
        MarkdownSegmentType.BULLET_2,
        MarkdownSegmentType.BULLET_3,
        MarkdownSegmentType.CODE,
        MarkdownSegmentType.QUOTE,
        MarkdownSegmentType.CHECKLIST_UNCHECKED,
        MarkdownSegmentType.CHECKLIST_CHECKED,
        MarkdownSegmentType.SEPARATOR,
        MarkdownSegmentType.IMAGE))
  }

  /*
   * Converts a string to the internal format types using the Markdown Segmentation Library.
   * It's possible to pass specific formats which will be preserved in the formats
   */
  private fun convertTextToFormats(text: String, whitelistedSegments: Array<MarkdownSegmentType>): List<Format> {
    val extractedFormats = mutableListOf<Format>()
    val segments = TextSegmenter(text).get()

    var lastFormat: Format? = null
    segments.forEach { segment ->
      val isSegmentWhitelisted = whitelistedSegments.contains(segment.type())
      val newFormat = when {
        !isSegmentWhitelisted -> null
        segment.type() == MarkdownSegmentType.HEADING_1 -> Format(FormatType.HEADING, segment.strip())
        segment.type() == MarkdownSegmentType.HEADING_2 -> Format(FormatType.SUB_HEADING, segment.strip())
        segment.type() == MarkdownSegmentType.HEADING_3 -> Format(FormatType.HEADING_3, segment.strip())
        segment.type() == MarkdownSegmentType.BULLET_1 -> Format(FormatType.BULLET_1, segment.strip())
        segment.type() == MarkdownSegmentType.BULLET_2 -> Format(FormatType.BULLET_2, segment.strip())
        segment.type() == MarkdownSegmentType.BULLET_3 -> Format(FormatType.BULLET_3, segment.strip())
        segment.type() == MarkdownSegmentType.CODE -> Format(FormatType.CODE, segment.strip())
        segment.type() == MarkdownSegmentType.QUOTE -> Format(FormatType.QUOTE, segment.strip())
        segment.type() == MarkdownSegmentType.CHECKLIST_UNCHECKED -> Format(FormatType.CHECKLIST_UNCHECKED, segment.strip())
        segment.type() == MarkdownSegmentType.CHECKLIST_CHECKED -> Format(FormatType.CHECKLIST_CHECKED, segment.strip())
        segment.type() == MarkdownSegmentType.SEPARATOR -> Format(FormatType.SEPARATOR)
        segment.type() == MarkdownSegmentType.IMAGE -> Format(FormatType.IMAGE, segment.strip().trim())
        else -> null
      }

      val tempLastFormat = lastFormat
      when {
        tempLastFormat !== null && newFormat !== null -> {
          extractedFormats.add(tempLastFormat)
          extractedFormats.add(newFormat)
          lastFormat = null
        }
        tempLastFormat === null && newFormat !== null -> {
          extractedFormats.add(newFormat)
        }
        tempLastFormat !== null && newFormat === null -> {
          tempLastFormat.text += "\n"
          tempLastFormat.text += segment.text()
          lastFormat = tempLastFormat
        }
        tempLastFormat == null && newFormat === null -> {
          lastFormat = Format(FormatType.TEXT, segment.text())
        }
      }
    }

    val tempLastFormat = lastFormat
    if (tempLastFormat !== null) {
      extractedFormats.add(tempLastFormat)
    }
    return extractedFormats
  }
}
