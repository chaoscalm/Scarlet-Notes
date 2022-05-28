package com.maubis.scarlet.base.security

import android.os.SystemClock
import com.maubis.scarlet.base.settings.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sSecurityAskPinAlways
import com.maubis.scarlet.base.settings.sSecurityCode

object PinLockController {
  private var sLastLoginTimeMs = 0L

  fun isPinCodeConfigured(): Boolean {
    return sSecurityCode.isNotEmpty()
  }

  fun needsAppLock(): Boolean {
    // App lock enabled
    if (isPinCodeConfigured() && sSecurityAppLockEnabled) {
      return needsLockCheckImpl()
    }
    return false
  }

  fun needsLockCheck(): Boolean {
    if (sSecurityAskPinAlways) {
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