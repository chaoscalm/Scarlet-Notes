package com.maubis.scarlet.base.security

import android.content.Context
import android.os.Bundle
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.settings.sSecurityCode
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.isBiometricEnabled
import com.maubis.scarlet.base.support.utils.showBiometricPrompt

class AppLockActivity : ThemedActivity() {
  lateinit var context: Context
  lateinit var component: Component
  lateinit var componentContext: ComponentContext

  private var passCodeEntered: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    context = this
    componentContext = ComponentContext(context)

    setView()
    notifyThemeChange()
  }

  private fun setView() {
    component = AppLockView.create(componentContext)
      .fingerprintEnabled(isBiometricEnabled(this))
      .onTextChange { text ->
        passCodeEntered = text
      }
      .onClick {
        if (passCodeEntered.length == 4 && sSecurityCode == passCodeEntered) {
          PinLockController.notifyPinVerified()
          finish()
        }
      }
      .build()
    setContentView(LithoView.create(componentContext, component))
  }

  override fun onResume() {
    super.onResume()
    passCodeEntered = ""

    if (isBiometricEnabled(this)) {
      showBiometricPrompt(this, onSuccess = {
        PinLockController.notifyPinVerified()
        finish()
      }, title = R.string.biometric_prompt_unlock_app, subtitle = R.string.biometric_prompt_unlock_app_details)
    }

  }

  override fun notifyThemeChange() {
    updateStatusBarTheme()
  }
}
