package com.maubis.scarlet.base.support.ui

import android.content.Intent
import com.maubis.scarlet.base.security.AppLockActivity
import com.maubis.scarlet.base.security.PinLockController

abstract class SecuredActivity : ThemedActivity() {
  override fun onResume() {
    super.onResume()
    if (PinLockController.needsAppLock()) {
      startActivity(Intent(this, AppLockActivity::class.java))
    }
  }
}