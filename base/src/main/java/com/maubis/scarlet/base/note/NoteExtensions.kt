package com.maubis.scarlet.base.note

import android.content.Context
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.*
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.core.note.getTagUUIDs
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.settings.*
import com.maubis.scarlet.base.support.BitmapHelper
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.sThemeDarkenNoteColor
import com.maubis.scarlet.base.support.utils.ColorUtil
import com.maubis.scarlet.base.support.utils.dateFormat
import java.util.*
import kotlin.collections.ArrayList

fun Note.log(): String {
  val log = HashMap<String, Any>()
  log["note"] = this
  log["_text"] = getFullText()
  log["_image"] = getImageFile()
  log["_fullText"] = getFullText()
  log["_displayTime"] = getDisplayTime()
  log["_tag"] = getTagString()
  log["_formats"] = getFormats()
  return Gson().toJson(log)
}

/**************************************************************************************
 ************* Content and Display Information Functions Functions ********************
 **************************************************************************************/

fun Note.getFullTextForDirectMarkdownRender(): String {
  var text = getFullText()
  text = text.replace("\n[x] ", "\n\u2611 ")
  text = text.replace("\n[ ] ", "\n\u2610 ")
  text = text.replace("\n- ", "\n\u2022 ")
  return text
}

fun Note.getMarkdownForListView(): CharSequence {
  var text = getFullTextForDirectMarkdownRender()
  if (sUIMarkdownEnabledOnHome) {
    return markdownFormatForList(text)
  }
  return text
}

internal fun markdownFormatForList(text: String): CharSequence {
  return Markdown.renderWithCustomFormatting(text, true) { spannable, spanInfo ->
    val s = spanInfo.start
    val e = spanInfo.end
    when (spanInfo.markdownType) {
      MarkdownType.HEADING_1 -> {
        spannable.relativeSize(1.2f, s, e)
          .font(MarkdownConfig.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.HEADING_2 -> {
        spannable.relativeSize(1.1f, s, e)
          .font(MarkdownConfig.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.HEADING_3 -> {
        spannable.relativeSize(1.0f, s, e)
          .font(MarkdownConfig.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.CHECKLIST_CHECKED -> {
        spannable.strike(s, e)
        true
      }
      else -> false
    }
  }
}

fun Note.getTitleForSharing(): String {
  val formats = getFormats()
  if (formats.isEmpty()) {
    return ""
  }
  val format = formats.first()
  val headingFormats = listOf(FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3)
  return when {
    headingFormats.contains(format.formatType) -> format.text
    else -> ""
  }
}

fun Note.getTextForSharing(): String {
  val formats = getFormats().toMutableList()
  if (formats.isEmpty()) {
    return ""
  }

  val format = formats.first()
  if (format.formatType == FormatType.HEADING || format.formatType == FormatType.SUB_HEADING) {
    formats.removeAt(0)
  }

  val stringBuilder = StringBuilder()
  formats.forEach {
    stringBuilder.append(it.markdownText)
    stringBuilder.append("\n")
    if (it.formatType == FormatType.QUOTE) {
      stringBuilder.append("\n")
    }
  }

  val text = stringBuilder.toString().trim()
  if (sInternalShowUUID) {
    return "`$uuid`\n\n$text"
  }
  return text
}

fun Note.getSmartFormats(): List<Format> {
  val formats = getFormats()
  var maxIndex = formats.size
  val smartFormats = ArrayList<Format>()
  formats.forEach {
    if (it.formatType == FormatType.TEXT) {
      val moreFormats = it.text.toInternalFormats()
      moreFormats.forEach { format ->
        format.uid = maxIndex
        smartFormats.add(format)
        maxIndex += 1
      }
    } else {
      smartFormats.add(it)
    }
  }
  return smartFormats
}

fun Note.getImageFile(): String {
  val formats = getFormats()
  val format = formats.find { it.formatType === FormatType.IMAGE }
  return format?.text ?: ""
}

fun Note.getFullText(): String {
  val fullText = getFormats().map { it -> it.markdownText }.joinToString(separator = "\n").trim()
  if (sInternalShowUUID) {
    return "`$uuid`\n$fullText"
  }
  return fullText
}

fun Note.isNoteLockedButAppUnlocked(): Boolean {
  return this.locked && !needsLockCheck() && sSecurityAppLockEnabled
}

fun Note.getLockedAwareTextForHomeList(): CharSequence {
  val lockedText = "******************\n***********\n****************"
  return when {
    isNoteLockedButAppUnlocked() || !this.locked -> getMarkdownForListView()
    !sUIMarkdownEnabledOnHome -> "${getTitleForSharing()}\n$lockedText"
    else -> markdownFormatForList("# ${getTitleForSharing()}\n\n```\n$lockedText\n```")
  }
}

fun Note.getTextForWidget(): CharSequence {
  if (locked && !sWidgetShowLockedNotes) {
    return "******************\n***********\n****************"
  }

  val text = getFullTextForDirectMarkdownRender()
  return when (sWidgetEnableFormatting) {
    true -> Markdown.render(text, true)
    false -> text
  }
}

fun Note.getDisplayTime(): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    (this.timestamp != null) -> this.timestamp
    else -> 0
  }

  val format = when {
    Calendar.getInstance().timeInMillis - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return dateFormat.readableTime(format, time)
}

fun Note.getTagString(): String {
  val tags = getTags()
  return tags.map { "` ${it.title} `" }.joinToString(separator = " ")
}

fun Note.getTags(): Set<Tag> {
  val tags = HashSet<Tag>()
  for (tagID in getTagUUIDs()) {
    val tag = data.tags.getByUUID(tagID)
    if (tag != null) {
      tags.add(tag)
    }
  }
  return tags
}

fun Note.toggleTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> tags.remove(tag.uuid)
    false -> tags.add(tag.uuid)
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.addTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> return
    false -> tags.add(tag.uuid)
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.removeTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> tags.remove(tag.uuid)
    false -> return
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.adjustedColor(): Int {
  return when (sThemeDarkenNoteColor) {
    true -> ColorUtil.darkOrDarkerColor(color ?: sNoteDefaultColor)
    false -> color ?: sNoteDefaultColor
  }
}

/**************************************************************************************
 ******************************* Note Action Functions ********************************
 **************************************************************************************/

fun Note.mark(context: Context, noteState: NoteState) {
  this.state = noteState.name
  this.updateTimestamp = Calendar.getInstance().timeInMillis
  save(context)
}

fun Note.edit(context: Context) {
  if (this.locked) {
    if (context is ThemedActivity) {
      openUnlockSheet(
        activity = context,
        onUnlockSuccess = { context.startActivity(NoteIntentRouterActivity.edit(context, this)) },
        onUnlockFailure = { edit(context) })
    }
    return
  }
  context.startActivity(NoteIntentRouterActivity.edit(context, this))
}

fun Note.hasImages(): Boolean {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  return imageFormats.isNotEmpty()
}

fun Note.shareImages(context: Context) {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  val bitmaps = imageFormats
    .map { ScarletApp.imageStorage.getFile(uuid, it.text) }
    .filter { it.exists() }
    .map { BitmapHelper.loadFromFile(it) }
    .filterNotNull()
  when {
    bitmaps.size == 1 -> BitmapHelper.send(context, bitmaps.first())
    bitmaps.size > 1 -> BitmapHelper.send(context, bitmaps)
  }
}

fun Note.copy(context: Context) {
  TextUtils.copyToClipboard(context, getFullText())
}

fun Note.share(context: Context) {
  IntentUtils.ShareBuilder(context)
          .setSubject(getTitleForSharing())
          .setText(getFullText())
          .setChooserText(context.getString(R.string.share_using))
          .share()
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Note.applySanityChecks() {
  folder = folder ?: ""
  description = description ?: ""
  timestamp = timestamp ?: System.currentTimeMillis()
  color = color ?: sNoteDefaultColor
  state = state ?: NoteState.DEFAULT.name
  tags = tags ?: ""
  uuid = uuid ?: generateUUID()
}

fun Note.save(context: Context) {
  applySanityChecks()
  ScarletApp.data.noteActions(this).save(context)
}

fun Note.delete(context: Context) {
  ScarletApp.data.noteActions(this).delete(context)
}

fun Note.softDelete(context: Context) {
  ScarletApp.data.noteActions(this).softDelete(context)
}
