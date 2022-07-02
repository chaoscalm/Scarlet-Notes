package com.maubis.scarlet.base.security

import android.os.SystemClock
import com.maubis.scarlet.base.ScarletApp

object PinLockController {
  private var sLastLoginTimeMs = 0L

  fun isPinCodeConfigured(): Boolean {
    return ScarletApp.prefs.pinCode.isNotEmpty()
  }

  fun needsAppLock(): Boolean {
    // App lock enabled
    if (isPinCodeConfigured() && ScarletApp.prefs.lockApp) {
      return needsLockCheckImpl()
    }
    return false
  }

  fun needsLockCheck(): Boolean {
    if (ScarletApp.prefs.alwaysNeedToAuthenticate) {
      return true
    }

    return needsLockCheckImpl()
  }

  private fun needsLockCheckImpl(): Boolean {
    val deltaSinceLastUnlock = SystemClock.uptimeMillis() - sLastLoginTimeMs

    // unlock stays 10 minutes
    if (sLastLoginTimeMs == 0L || deltaSinceLastUnlock > 1000 * 60 * 5) {
      return true
    }

    // reset lock time
    notifyPinVerified()
    return false
  }

  fun notifyPinVerified() {
    sLastLoginTimeMs = SystemClock.uptimeMillis()
  }

}