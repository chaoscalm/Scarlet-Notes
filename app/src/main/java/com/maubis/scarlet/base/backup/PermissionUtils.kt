package com.maubis.scarlet.base.backup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat

object PermissionUtils {
  fun hasExternalStorageAccess(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
  }
}