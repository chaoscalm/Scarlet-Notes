package com.maubis.scarlet.base.support

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

object SharingUtils {
  fun sendImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/jpeg"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.clipData = ClipData.newRawUri("", uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share Image"))
  }

  fun sendMultipleImages(context: Context, uris: List<Uri>) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    intent.type = "image/jpeg"
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share Images"))
  }
}