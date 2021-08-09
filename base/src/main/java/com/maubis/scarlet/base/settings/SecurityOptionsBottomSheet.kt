package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.security.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.openCreateSheet
import com.maubis.scarlet.base.security.openVerifySheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.deviceHasBiometricEnabled

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
        title = R.string.security_option_set_pin_code,
        subtitle = R.string.security_option_set_pin_code_subtitle,
        icon = R.drawable.ic_option_security,
        listener = {
          when {
            isPinCodeEnabled() -> openResetPasswordDialog(dialog)
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = isPinCodeEnabled()
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_lock_app,
        subtitle = R.string.security_option_lock_app_details,
        icon = R.drawable.ic_apps_white_48dp,
        listener = {
          when {
            isPinCodeEnabled() -> openVerifySheet(
              activity = activity,
              onVerifySuccess = {
                sSecurityAppLockEnabled = !sSecurityAppLockEnabled
                reset(componentContext.androidContext, dialog)
              }
            )
            else -> openCreatePasswordDialog(dialog)
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
            isPinCodeEnabled() -> openVerifySheet(
              activity = activity,
              onVerifySuccess = {
                sSecurityAskPinAlways = !sSecurityAskPinAlways
                reset(componentContext.androidContext, dialog)
              }
            )
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = sSecurityAskPinAlways,
        actionIcon = R.drawable.ic_done_white_48dp
      ))

    val hasFingerprint = deviceHasBiometricEnabled(requireContext())
    options.add(
      LithoOptionsItem(
        title = R.string.security_option_biometrics_enabled,
        subtitle = R.string.security_option_biometrics_enabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            isPinCodeEnabled() -> openVerifySheet(
              activity = activity,
              onVerifySuccess = {
                sSecurityBiometricEnabled = false
                reset(componentContext.androidContext, dialog)
              }
            )
            else -> {
              sSecurityBiometricEnabled = false
              reset(componentContext.androidContext, dialog)
            }
          }
        },
        visible = sSecurityBiometricEnabled && hasFingerprint,
        isSelectable = true,
        selected = true
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.security_option_biometrics_disabled,
        subtitle = R.string.security_option_biometrics_disabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            isPinCodeEnabled() -> openVerifySheet(
              activity = activity,
              onVerifySuccess = {
                sSecurityBiometricEnabled = true
                reset(componentContext.androidContext, dialog)
              }
            )
            else -> {
              sSecurityBiometricEnabled = true
              reset(componentContext.androidContext, dialog)
            }
          }
        },
        visible = !sSecurityBiometricEnabled && hasFingerprint
      ))
    return options
  }

  fun openCreatePasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openCreateSheet(
      activity = activity,
      onCreateSuccess = { reset(dialog.context, dialog) })
  }

  fun openResetPasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openVerifySheet(
      activity,
      onVerifySuccess = {
        openCreatePasswordDialog(dialog)
      },
      onVerifyFailure = {
        openResetPasswordDialog(dialog)
      })
  }
}