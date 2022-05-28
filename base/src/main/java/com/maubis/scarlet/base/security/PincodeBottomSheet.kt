package com.maubis.scarlet.base.security

import android.app.Dialog
import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EditorActionEvent
import com.facebook.litho.widget.TextChangedEvent
import com.facebook.litho.widget.TextInput
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.isBiometricEnabled
import com.maubis.scarlet.base.common.utils.showBiometricPrompt
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.security.PinLockController.isPinCodeConfigured
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.settings.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sSecurityCode

data class PincodeSheetData(
  @StringRes val title: Int,
  @StringRes val actionTitle: Int,
  @StringRes val biometricTitle: Int = R.string.biometric_prompt_auth_required,
  val onSuccess: () -> Unit,
  val onFailure: () -> Unit = {},
  val isBiometricEnabled: Boolean = false,
  val onActionClicked: (String) -> Unit = { password ->
    when {
      password != "" && password == sSecurityCode -> {
        PinLockController.notifyPinVerified()
        onSuccess()
      }
      else -> onFailure()
    }
  },
  val isRemoveButtonEnabled: Boolean = false,
  val onRemoveButtonClick: () -> Unit = {})

private var sPincodeSheetPasscodeEntered = ""

@LayoutSpec
object PincodeSheetViewSpec {

  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop data: PincodeSheetData, @Prop dismiss: () -> Unit): Component {
    val editBackground = when {
      appTheme.isNightTheme() -> R.drawable.light_secondary_rounded_bg
      else -> R.drawable.secondary_rounded_bg
    }

    val bottomBar = BottomSheetBar.create(context)
      .primaryActionRes(data.actionTitle)
      .onPrimaryClick {
        data.onActionClicked(sPincodeSheetPasscodeEntered)
        sPincodeSheetPasscodeEntered = ""
        dismiss()
      }
    if (data.isRemoveButtonEnabled) {
      bottomBar
        .secondaryActionRes(R.string.security_sheet_button_remove)
        .onSecondaryClick {
          data.onRemoveButtonClick()
          sPincodeSheetPasscodeEntered = ""
          dismiss()
        }
    }
    val component = Column.create(context)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(context)
          .textRes(data.title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        TextInput.create(context)
          .backgroundRes(editBackground)
          .textSizeRes(R.dimen.font_size_xlarge)
          .minWidthDip(128f)
          .inputFilter(InputFilter.LengthFilter(4))
          .alignSelf(YogaAlign.CENTER)
          .hint("****")
          .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
          .textAlignment(View.TEXT_ALIGNMENT_CENTER)
          .typeface(appTypeface.text())
          .textColorStateList(ColorStateList.valueOf(appTheme.get(ThemeColorType.PRIMARY_TEXT)))
          .paddingDip(YogaEdge.HORIZONTAL, 22f)
          .paddingDip(YogaEdge.VERTICAL, 6f)
          .marginDip(YogaEdge.VERTICAL, 16f)
          .imeOptions(EditorInfo.IME_ACTION_DONE)
          .editorActionEventHandler(PincodeSheetView.onPinEditorAction(context))
          .textChangedEventHandler(PincodeSheetView.onTextChangeListener(context)))
      .child(bottomBar)
    return component.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(EditorActionEvent::class)
  fun onPinEditorAction(context: ComponentContext,
                        @Prop data: PincodeSheetData,
                        @Prop dismiss: () -> Unit): Boolean {
    data.onActionClicked(sPincodeSheetPasscodeEntered)
    dismiss()
    return true
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(TextChangedEvent::class)
  fun onTextChangeListener(context: ComponentContext, @FromEvent text: String) {
    sPincodeSheetPasscodeEntered = text
  }
}

class PincodeBottomSheet : LithoBottomSheet() {
  private lateinit var data: PincodeSheetData

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    sPincodeSheetPasscodeEntered = ""
    return PincodeSheetView.create(componentContext)
      .data(data)
      .dismiss { dismiss() }
      .build()
  }

  override fun onResume() {
    super.onResume()
    if (data.isBiometricEnabled) {
      showBiometricPrompt(
        title = data.biometricTitle,
        activity = requireActivity(),
        fragment = this,
        onSuccess = {
          data.onSuccess()
          dismiss()
        }
      )
    }
  }

  companion object {
    fun openForPincodeSetup(activity: ThemedActivity, onCreateSuccess: () -> Unit) {
      openSheet(activity, PincodeBottomSheet().apply {
        data = PincodeSheetData(
          title = R.string.security_sheet_enter_new_pin_title,
          actionTitle = R.string.security_sheet_button_set,
          isBiometricEnabled = false,
          isRemoveButtonEnabled = isPinCodeConfigured(),
          onRemoveButtonClick = {
            sSecurityCode = ""
            sSecurityAppLockEnabled = false
            onCreateSuccess()

            if (activity is MainActivity) {
              activity.loadData()
            }
          },
          onActionClicked = { password: String ->
            if (password.length == 4 && password.toIntOrNull() !== null) {
              sSecurityCode = password
              onCreateSuccess()
            }
          },
          onSuccess = {}
        )
      })
    }

    fun openForVerification(activity: ThemedActivity, onVerifySuccess: () -> Unit) {
      if (!isPinCodeConfigured()) {
        openSheet(activity, NoPincodeBottomSheet().apply {
          this.onSuccess = onVerifySuccess
        })
        return
      }

      openSheet(activity, PincodeBottomSheet().apply {
        data = PincodeSheetData(
          title = R.string.security_sheet_enter_current_pin_title,
          actionTitle = R.string.security_sheet_button_verify,
          onSuccess = onVerifySuccess,
          onFailure = {
            Toast.makeText(activity, R.string.security_sheet_wrong_pin, Toast.LENGTH_SHORT).show()
          },
          isBiometricEnabled = isBiometricEnabled(activity)
        )
      })
    }

    fun openForUnlock(activity: ThemedActivity, onUnlockSuccess: () -> Unit, onUnlockFailure: () -> Unit) {
      if (!isPinCodeConfigured()) {
        openSheet(activity, NoPincodeBottomSheet().apply {
          this.onSuccess = onUnlockSuccess
        })
        return
      }

      if (!needsLockCheck()) {
        return onUnlockSuccess()
      }
      openSheet(activity, PincodeBottomSheet().apply {
        data = PincodeSheetData(
          title = R.string.security_sheet_enter_pin_to_unlock_title,
          biometricTitle = R.string.biometric_prompt_unlock_note,
          actionTitle = R.string.security_sheet_button_unlock,
          onSuccess = onUnlockSuccess,
          onFailure = onUnlockFailure,
          isBiometricEnabled = isBiometricEnabled(activity)
        )
      })
    }
  }
}
