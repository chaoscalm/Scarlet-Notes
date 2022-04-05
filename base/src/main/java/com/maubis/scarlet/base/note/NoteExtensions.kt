package com.maubis.scarlet.base.note

import android.content.Context
import androidx.core.content.FileProvider
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.ScarletIntentHandlerActivity
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.*
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.editor.formats.FormatType
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.settings.sInternalShowUUID
import com.maubis.scarlet.base.settings.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sWidgetShowLockedNotes

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

fun Note.getImageFile(): String {
  val formats = getFormats()
  val format = formats.find { it.formatType === FormatType.IMAGE }
  return format?.text ?: ""
}

fun Note.getFullText(): String {
  val fullText = getFormats().joinToString(separator = "\n") { it.markdownText }.trim()
  if (sInternalShowUUID) {
    return "`$uuid`\n$fullText"
  }
  return fullText
}

fun Note.isLockedButAppUnlocked(): Boolean {
  return this.locked && !needsLockCheck() && sSecurityAppLockEnabled
}

fun Note.getTextForWidget(): CharSequence {
  if (locked && !sWidgetShowLockedNotes) {
    return "*********************************************"
  }

  val text = getFullTextForDirectMarkdownRender()
  return Markdown.render(text, true)
}

fun Note.getDisplayTime(context: Context): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    else -> this.timestamp
  }

  val format = when {
    System.currentTimeMillis() - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return readableTime(time, format, context)
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
  return if (appTheme.shouldDarkenCustomColors()) ColorUtil.darkerColor(color) else color
}

/**************************************************************************************
 ******************************* Note Action Functions ********************************
 **************************************************************************************/

fun Note.mark(context: Context, noteState: NoteState) {
  this.state = noteState
  this.updateTimestamp = System.currentTimeMillis()
  save(context)
}

fun Note.edit(context: Context) {
  if (this.locked) {
    if (context is ThemedActivity) {
      openUnlockSheet(
        activity = context,
        onUnlockSuccess = { context.startActivity(ScarletIntentHandlerActivity.edit(context, this)) },
        onUnlockFailure = { edit(context) })
    }
    return
  }
  context.startActivity(ScarletIntentHandlerActivity.edit(context, this))
}

fun Note.hasImages(): Boolean {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  return imageFormats.isNotEmpty()
}

fun Note.shareImages(context: Context) {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  val imageFileUris = imageFormats
    .map { ScarletApp.imageStorage.getImage(uuid.toString(), it) }
    .filter { it.exists() }
    .map { FileProvider.getUriForFile(context, "fs00.scarletnotes.FileProvider", it) }
  when {
    imageFileUris.size == 1 -> shareImage(context, imageFileUris.first())
    imageFileUris.size > 1 -> shareMultipleImages(context, imageFileUris)
  }
}

fun Note.copyToClipboard(context: Context) {
  copyTextToClipboard(context, getFullText())
}

fun Note.share(context: Context) {
  shareText(context, getFullText(), subject = getTitleForSharing())
}