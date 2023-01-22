package com.maubis.scarlet.base.editor

import com.maubis.markdown.blocks.MarkdownBlockParser
import com.maubis.markdown.blocks.MarkdownBlockType
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.utils.logNonCriticalError
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

  fun sortChecklistsIfAllowed(formats: List<Format>): List<Format> {
    if (!ScarletApp.prefs.moveCheckedItems) {
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
          MarkdownBlockType.HEADING_1,
          MarkdownBlockType.HEADING_2,
          MarkdownBlockType.HEADING_3,
          MarkdownBlockType.CODE,
          MarkdownBlockType.SEPARATOR,
          MarkdownBlockType.CHECKLIST_CHECKED,
          MarkdownBlockType.CHECKLIST_UNCHECKED))
      newFormats.addAll(moreFormats)
    }
    return newFormats
  }

  private fun convertTextToFormats(text: String): List<Format> {
    return convertTextToFormats(
      text,
      arrayOf(
        MarkdownBlockType.HEADING_1,
        MarkdownBlockType.HEADING_2,
        MarkdownBlockType.HEADING_3,
        MarkdownBlockType.BULLET_1,
        MarkdownBlockType.BULLET_2,
        MarkdownBlockType.BULLET_3,
        MarkdownBlockType.CODE,
        MarkdownBlockType.QUOTE,
        MarkdownBlockType.CHECKLIST_UNCHECKED,
        MarkdownBlockType.CHECKLIST_CHECKED,
        MarkdownBlockType.SEPARATOR))
  }

  private fun convertTextToFormats(text: String, allowedBlockTypes: Array<MarkdownBlockType>): List<Format> {
    val extractedFormats = mutableListOf<Format>()
    val blocks = MarkdownBlockParser(text).parseText()

    var lastFormat: Format? = null
    blocks.forEach { block ->
      val isBlockAllowed = allowedBlockTypes.contains(block.type)
      val newFormat = when {
        !isBlockAllowed -> null
        block.type == MarkdownBlockType.HEADING_1 -> Format(FormatType.HEADING, block.strippedText())
        block.type == MarkdownBlockType.HEADING_2 -> Format(FormatType.SUB_HEADING, block.strippedText())
        block.type == MarkdownBlockType.HEADING_3 -> Format(FormatType.HEADING_3, block.strippedText())
        block.type == MarkdownBlockType.BULLET_1 -> Format(FormatType.BULLET_1, block.strippedText())
        block.type == MarkdownBlockType.BULLET_2 -> Format(FormatType.BULLET_2, block.strippedText())
        block.type == MarkdownBlockType.BULLET_3 -> Format(FormatType.BULLET_3, block.strippedText())
        block.type == MarkdownBlockType.CODE -> Format(FormatType.CODE, block.strippedText())
        block.type == MarkdownBlockType.QUOTE -> Format(FormatType.QUOTE, block.strippedText())
        block.type == MarkdownBlockType.CHECKLIST_UNCHECKED -> Format(FormatType.CHECKLIST_UNCHECKED, block.strippedText())
        block.type == MarkdownBlockType.CHECKLIST_CHECKED -> Format(FormatType.CHECKLIST_CHECKED, block.strippedText())
        block.type == MarkdownBlockType.SEPARATOR -> Format(FormatType.SEPARATOR)
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
          tempLastFormat.text += block.text()
          lastFormat = tempLastFormat
        }
        tempLastFormat == null && newFormat === null -> {
          lastFormat = Format(FormatType.TEXT, block.text())
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
