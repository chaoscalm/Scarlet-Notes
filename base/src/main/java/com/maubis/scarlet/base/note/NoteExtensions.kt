package com.maubis.scarlet.base.note

import android.content.Context
import androidx.core.content.FileProvider
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.*
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.settings.sInternalShowUUID
import com.maubis.scarlet.base.settings.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sWidgetShowLockedNotes
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.ColorUtil
import com.maubis.scarlet.base.support.utils.SharingUtils
import com.maubis.scarlet.base.support.utils.dateFormat

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
  val text = getFullTextForDirectMarkdownRender()
  return markdownFormatForList(text)
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
    else -> markdownFormatForList("# ${getTitleForSharing()}\n\n```\n$lockedText\n```")
  }
}

fun Note.getTextForWidget(): CharSequence {
  if (locked && !sWidgetShowLockedNotes) {
    return "******************\n***********\n****************"
  }

  val text = getFullTextForDirectMarkdownRender()
  return Markdown.render(text, true)
}

fun Note.getDisplayTime(): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    else -> this.timestamp
  }

  val format = when {
    System.currentTimeMillis() - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
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
  val imageFileUris = imageFormats
    .map { ScarletApp.imageStorage.getImage(uuid.toString(), it) }
    .filter { it.exists() }
    .map { FileProvider.getUriForFile(context, "fs00.scarletnotes.FileProvider", it) }
  when {
    imageFileUris.size == 1 -> SharingUtils.sendImage(context, imageFileUris.first())
    imageFileUris.size > 1 -> SharingUtils.sendMultipleImages(context, imageFileUris)
  }
}

fun Note.copy(context: Context) {
  TextUtils.copyToClipboard(context, getFullText())
}

fun Note.share(context: Context) {
  SharingUtils.shareText(context, getFullText(), subject = getTitleForSharing())
}