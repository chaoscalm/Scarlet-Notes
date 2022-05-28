package com.maubis.scarlet.base.common.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.settings.sSecurityBiometricEnabled

fun isBiometricAuthAvailable(context: Context): Boolean {
  val biometricManager = BiometricManager.from(context)
  return biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
}

fun isBiometricEnabled(context: Context) = sSecurityBiometricEnabled && isBiometricAuthAvailable(context)

fun showBiometricPrompt(
  @StringRes title: Int,
  @StringRes subtitle: Int? = null,
  activity: FragmentActivity,
  fragment: Fragment? = null,
  onSuccess: () -> Unit = {},
  onFailure: () -> Unit = {}) {
  val executor = ContextCompat.getMainExecutor(activity)

  val callback = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
      super.onAuthenticationError(errorCode, errString)
      onFailure()
    }

    override fun onAuthenticationFailed() {
      super.onAuthenticationFailed()
      onFailure()
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      super.onAuthenticationSucceeded(result)
      onSuccess()
    }
  }

  val prompt = when {
    fragment != null -> BiometricPrompt(fragment, executor, callback)
    else -> BiometricPrompt(activity, executor, callback)
  }
  val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle(activity.getString(title))
    .setDescription(subtitle?.let { activity.getString(subtitle) })
    .setAllowedAuthenticators(BIOMETRIC_WEAK)
    .setNegativeButtonText(activity.getString(R.string.delete_sheet_delete_trash_no))
    .build()
  prompt.authenticate(promptInfo)
}