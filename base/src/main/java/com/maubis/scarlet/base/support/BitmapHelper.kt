package com.maubis.scarlet.base.support

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object BitmapHelper {
  fun send(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/jpeg"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.clipData = ClipData.newRawUri("", uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share Image"))
  }

  fun send(context: Context, uris: List<Uri>) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    intent.type = "image/jpeg"
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share Images"))
  }

  fun saveToFile(cacheFile: File, bitmap: Bitmap) {
    val fOut = FileOutputStream(cacheFile)
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
    fOut.flush()
    fOut.close()
  }
}