package com.maubis.scarlet.base.security

import android.content.Context
import android.os.Bundle
import androidx.activity.addCallback
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.isBiometricEnabled
import com.maubis.scarlet.base.common.utils.showBiometricPrompt

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
    applyTheming()

    onBackPressedDispatcher.addCallback(this) { finishAffinity() }
  }

  private fun setView() {
    component = AppLockView.create(componentContext)
      .onTextChange { text ->
        passCodeEntered = text
      }
      .onClick {
        if (passCodeEntered.length == 4 && ScarletApp.prefs.pinCode == passCodeEntered) {
          PinLockController.notifyPinVerified()
          tryClosingTheKeyboard()
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
      showBiometricPrompt(
        title = R.string.biometric_prompt_unlock_app,
        subtitle = R.string.biometric_prompt_unlock_app_details,
        activity = this,
        onSuccess = {
          PinLockController.notifyPinVerified()
          finish()
        }
      )
    }

  }

  override fun applyTheming() {
    updateStatusBarTheme()
  }
}
