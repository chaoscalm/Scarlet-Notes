package com.maubis.scarlet.base.common.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.maubis.scarlet.base.R

fun shareImage(context: Context, uri: Uri) {
  val intent = Intent(Intent.ACTION_SEND)
  intent.type = "image/jpeg"
  intent.putExtra(Intent.EXTRA_STREAM, uri)
  intent.clipData = ClipData.newRawUri("", uri)
  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image)))
}

fun shareMultipleImages(context: Context, uris: List<Uri>) {
  val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
  intent.type = "image/jpeg"
  intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_images)))
}

fun shareText(context: Context, text: String, subject: String? = null) {
  val sharingIntent = Intent(Intent.ACTION_SEND)
  sharingIntent.type = "text/plain"
  sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
  sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
  context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_using)))
}

fun copyTextToClipboard(context: Context, textToCopy: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("", textToCopy)
  clipboard.setPrimaryClip(clip)
  Toast.makeText(context, R.string.notice_text_copied, Toast.LENGTH_SHORT).show()
}