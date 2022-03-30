package com.maubis.scarlet.base.backup

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.github.bijoysingh.starter.util.PermissionManager

object PermissionUtils {
  fun getStoragePermissionManager(activity: AppCompatActivity): PermissionManager {
    val manager = PermissionManager(activity)
    manager.setPermissions(
      arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE))
    return manager
  }
}