package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.isBiometricAuthAvailable
import com.maubis.scarlet.base.security.PinLockController.isPinCodeConfigured
import com.maubis.scarlet.base.security.PincodeBottomSheet

const val KEY_SECURITY_CODE = "KEY_SECURITY_CODE"
const val KEY_FINGERPRINT_ENABLED = "KEY_FINGERPRINT_ENABLED"
const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
const val KEY_ASK_PIN_ALWAYS = "ask_pin_always"

var sSecurityCode: String
  get() = appPreferences.getString(KEY_SECURITY_CODE, "")!!
  set(value) = appPreferences.edit { putString(KEY_SECURITY_CODE, value) }
var sSecurityBiometricEnabled: Boolean
  get() = appPreferences.getBoolean(KEY_FINGERPRINT_ENABLED, true)
  set(value) = appPreferences.edit { putBoolean(KEY_FINGERPRINT_ENABLED, value) }
var sSecurityAppLockEnabled: Boolean
  get() = appPreferences.getBoolean(KEY_APP_LOCK_ENABLED, false)
  set(value) = appPreferences.edit { putBoolean(KEY_APP_LOCK_ENABLED, value) }
var sSecurityAskPinAlways: Boolean
  get() = appPreferences.getBoolean(KEY_ASK_PIN_ALWAYS, true)
  set(value) = appPreferences.edit { putBoolean(KEY_ASK_PIN_ALWAYS, value) }

class SecurityOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.security_option_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as ThemedActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = if (isPinCodeConfigured()) {
          R.string.security_option_set_pin_code_edit
        } else {
          R.string.security_option_set_pin_code_configure
        },
        subtitle = R.string.security_option_set_pin_code_subtitle,
        icon = R.drawable.ic_option_security,
        listener = {
          when {
            isPinCodeConfigured() -> openResetPinDialog(dialog)
            else -> openCreatePinDialog(dialog)
          }
        }
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_lock_app,
        subtitle = R.string.security_option_lock_app_details,
        icon = R.drawable.ic_apps_white_48dp,
        listener = {
          when {
            isPinCodeConfigured() -> {
              PincodeBottomSheet.openForVerification(activity,
                onVerifySuccess = {
                  sSecurityAppLockEnabled = !sSecurityAppLockEnabled
                  refresh(componentContext.androidContext, dialog)
                }
              )
            }
            else -> openCreatePinDialog(dialog)
          }
        },
        isSelectable = true,
        selected = sSecurityAppLockEnabled,
        actionIcon = R.drawable.ic_done_white_48dp
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_ask_pin_always,
        subtitle = R.string.security_option_ask_pin_always_details,
        icon = R.drawable.ic_action_grid,
        listener = {
          when {
            isPinCodeConfigured() -> {
              PincodeBottomSheet.openForVerification(activity,
                onVerifySuccess = {
                  sSecurityAskPinAlways = !sSecurityAskPinAlways
                  refresh(componentContext.androidContext, dialog)
                }
              )
            }
            else -> openCreatePinDialog(dialog)
          }
        },
        isSelectable = true,
        selected = sSecurityAskPinAlways,
        actionIcon = R.drawable.ic_done_white_48dp
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_biometrics,
        subtitle = R.string.security_option_biometrics_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            isPinCodeConfigured() -> {
              PincodeBottomSheet.openForVerification(activity,
                onVerifySuccess = {
                  sSecurityBiometricEnabled = false
                  refresh(componentContext.androidContext, dialog)
                }
              )
            }
            else -> {
              sSecurityBiometricEnabled = false
              refresh(componentContext.androidContext, dialog)
            }
          }
        },
        visible = isBiometricAuthAvailable(requireContext()),
        isSelectable = true,
        selected = sSecurityBiometricEnabled
      ))
    return options
  }

  private fun openCreatePinDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    PincodeBottomSheet.openForPincodeSetup(activity,
      onCreateSuccess = { refresh(dialog.context, dialog) })
  }

  private fun openResetPinDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    PincodeBottomSheet.openForVerification(activity,
      onVerifySuccess = { openCreatePinDialog(dialog) })
  }
}