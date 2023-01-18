package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.isBiometricAuthAvailable
import com.maubis.scarlet.base.security.PinLockController.isPinCodeConfigured
import com.maubis.scarlet.base.security.PincodeBottomSheet

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
        icon = R.drawable.ic_pincode,
        listener = {
          if (isPinCodeConfigured()) {
            openResetPinDialog(dialog)
          } else {
            openCreatePinDialog(dialog)
          }
        }
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_lock_app,
        subtitle = R.string.security_option_lock_app_details,
        icon = R.drawable.ic_app_lock,
        listener = {
          PincodeBottomSheet.openForVerification(activity,
            onVerifySuccess = {
              ScarletApp.prefs.lockApp = !ScarletApp.prefs.lockApp
              refresh(componentContext.androidContext, dialog)
            }
          )
        },
        isSelectable = true,
        selected = ScarletApp.prefs.lockApp
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_ask_pin_always,
        subtitle = R.string.security_option_ask_pin_always_details,
        icon = R.drawable.ic_lock,
        listener = {
          PincodeBottomSheet.openForVerification(activity,
            onVerifySuccess = {
              ScarletApp.prefs.alwaysNeedToAuthenticate = !ScarletApp.prefs.alwaysNeedToAuthenticate
              refresh(componentContext.androidContext, dialog)
            }
          )
        },
        isSelectable = true,
        selected = ScarletApp.prefs.alwaysNeedToAuthenticate
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.security_option_biometrics,
        subtitle = R.string.security_option_biometrics_subtitle,
        icon = R.drawable.ic_fingerprint,
        listener = {
          PincodeBottomSheet.openForVerification(activity,
            onVerifySuccess = {
              ScarletApp.prefs.authenticateWithBiometric = !ScarletApp.prefs.authenticateWithBiometric
              refresh(componentContext.androidContext, dialog)
            }
          )
        },
        visible = isBiometricAuthAvailable(requireContext()),
        isSelectable = true,
        selected = ScarletApp.prefs.authenticateWithBiometric
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