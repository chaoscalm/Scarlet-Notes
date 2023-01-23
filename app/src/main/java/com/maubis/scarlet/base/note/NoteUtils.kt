package com.maubis.scarlet.base.note

import android.content.Context
import androidx.core.content.FileProvider
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.*
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.*
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.security.PincodeBottomSheet

/**************************************************************************************
 ************* Content and Display Information Functions Functions ********************
 **************************************************************************************/

fun Note.getTitleForSharing(): String {
  val formats = contentAsFormats()
  if (formats.isEmpty()) {
    return ""
  }
  val format = formats.first()
  val headingFormats = listOf(FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3)
  return when {
    headingFormats.contains(format.type) -> format.text
    else -> ""
  }
}

fun Note.getTextForSharing(): String {
  val formats = contentAsFormats().toMutableList()
  if (formats.isEmpty()) {
    return ""
  }

  val format = formats.first()
  if (format.type == FormatType.HEADING || format.type == FormatType.SUB_HEADING) {
    formats.removeAt(0)
  }

  val stringBuilder = StringBuilder()
  formats.forEach {
    stringBuilder.append(it.markdownText)
    stringBuilder.append("\n")
    if (it.type == FormatType.QUOTE) {
      stringBuilder.append("\n")
    }
  }
  return stringBuilder.toString().trim()
}

fun Note.getImageFile(): String {
  val formats = contentAsFormats()
  val format = formats.find { it.type === FormatType.IMAGE }
  return format?.text ?: ""
}

fun Note.getFullText(): String {
  return contentAsFormats()
    .joinToString(separator = "\n") { it.markdownText }
    .trim()
}

fun Note.getFullTextForPreview(): String {
  var text = getFullText()
  // Ideally we'd use a custom LeadingMarginSpan to render these "fake checkboxes". However,
  // custom spans don't seem to work in widgets so this is the best compromise we can do
  text = text.replace("\n[x] ", "\n\u2611 ")
  text = text.replace("\n[ ] ", "\n\u2610 ")
  return text
}

fun Note.isLockedButAppUnlocked(): Boolean {
  return this.locked && !needsLockCheck() && ScarletApp.prefs.lockApp
}

fun Note.getTextForWidget(): CharSequence {
  if (locked && !ScarletApp.prefs.showLockedNotesInWidgets) {
    return "*********************************************"
  }

  return renderMarkdownForNotePreview(getFullTextForPreview())
}

fun renderMarkdownForNotePreview(text: String): CharSequence {
  return Markdown.renderWithCustomFormatting(text, strip = true) { spannable, spanInfo ->
    val start = spanInfo.start
    val end = spanInfo.end
    when (spanInfo.markdownType) {
      MarkdownType.HEADING_1 -> {
        spannable.relativeSize(1.2f, start, end)
          .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
          .bold(start, end)
        true
      }
      MarkdownType.HEADING_2 -> {
        spannable.relativeSize(1.1f, start, end)
          .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
          .bold(start, end)
        true
      }
      MarkdownType.HEADING_3 -> {
        spannable.relativeSize(1.0f, start, end)
          .font(MarkdownConfig.spanConfig.headingTypeface, start, end)
          .bold(start, end)
        true
      }
      MarkdownType.BULLET_1 -> {
        spannable.bullet(1, start, end)
        true
      }
      MarkdownType.BULLET_2 -> {
        spannable.bullet(2, start, end)
        true
      }
      MarkdownType.BULLET_3 -> {
        spannable.bullet(3, start, end)
        true
      }
      else -> false
    }
  }
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
  val tags = tags.mapNotNull { uuid -> data.tags.getByUUID(uuid) }
  return tags.joinToString(separator = " ") { "` ${it.title} `" }
}

fun Note.adjustedColor(): Int {
  return if (appTheme.shouldDarkenCustomColors()) ColorUtil.darkerColor(color) else color
}

/**************************************************************************************
 ******************************* Note Action Functions ********************************
 **************************************************************************************/

fun Note.edit(context: Context) {
  if (this.locked) {
    if (context is ThemedActivity) {
      PincodeBottomSheet.openForUnlock(context,
        onUnlockSuccess = { context.startActivity(EditNoteActivity.makeEditNoteIntent(context, this)) },
        onUnlockFailure = { edit(context) })
    }
    return
  }
  context.startActivity(EditNoteActivity.makeEditNoteIntent(context, this))
}

fun Note.hasImages(): Boolean {
  val imageFormats = contentAsFormats().filter { it.type == FormatType.IMAGE }
  return imageFormats.isNotEmpty()
}

fun Note.shareImages(context: Context) {
  val imageFormats = contentAsFormats().filter { it.type == FormatType.IMAGE }
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