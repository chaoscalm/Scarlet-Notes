package com.maubis.scarlet.base.security

import android.app.Dialog
import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.InputType
import android.text.Layout
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.*
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.utils.isBiometricEnabled
import com.maubis.scarlet.base.common.utils.showBiometricPrompt
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.security.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.PinLockController.needsLockCheck
import com.maubis.scarlet.base.settings.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sSecurityCode

data class PincodeSheetData(
  val title: Int,
  val actionTitle: Int,
  val onSuccess: () -> Unit,
  val onFailure: () -> Unit = {},
  val isFingerprintEnabled: Boolean = false,
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
  fun onCreate(context: ComponentContext, @Prop data: PincodeSheetData): Component {
    val editBackground = when {
      appTheme.isNightTheme() -> R.drawable.light_secondary_rounded_bg
      else -> R.drawable.secondary_rounded_bg
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
        Text.create(context)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .textRes(R.string.app_lock_details)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
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
          .marginDip(YogaEdge.VERTICAL, 8f)
          .imeOptions(EditorInfo.IME_ACTION_DONE)
          .editorActionEventHandler(PincodeSheetView.onPinEditorAction(context))
          .textChangedEventHandler(PincodeSheetView.onTextChangeListener(context)))
      .child(
        Row.create(context)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.HORIZONTAL, 8f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .child(
            when {
              data.isFingerprintEnabled -> Image.create(context)
                .drawableRes(R.drawable.ic_option_fingerprint)
                .heightDip(36f)
              else -> null
            }
          )
          .child(
            when {
              data.isRemoveButtonEnabled -> Text.create(context)
                .textSizeRes(R.dimen.font_size_large)
                .textColor(appTheme.get(ThemeColorType.HINT_TEXT))
                .textRes(R.string.security_sheet_button_remove)
                .textAlignment(Layout.Alignment.ALIGN_CENTER)
                .paddingDip(YogaEdge.VERTICAL, 12f)
                .paddingDip(YogaEdge.HORIZONTAL, 20f)
                .typeface(appTypeface.title())
                .clickHandler(PincodeSheetView.onRemoveClick(context))
              else -> null
            }
          )
          .child(EmptySpec.create(context).flexGrow(1f))
          .child(
            Text.create(context)
              .backgroundRes(R.drawable.accent_rounded_bg)
              .textSizeRes(R.dimen.font_size_large)
              .textColorRes(android.R.color.white)
              .textRes(data.actionTitle)
              .textAlignment(Layout.Alignment.ALIGN_CENTER)
              .paddingDip(YogaEdge.VERTICAL, 12f)
              .paddingDip(YogaEdge.HORIZONTAL, 20f)
              .typeface(appTypeface.title())
              .clickHandler(PincodeSheetView.onActionClick(context))))
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

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onActionClick(
          context: ComponentContext,
          @Prop data: PincodeSheetData,
          @Prop dismiss: () -> Unit) {
    data.onActionClicked(sPincodeSheetPasscodeEntered)
    sPincodeSheetPasscodeEntered = ""
    dismiss()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onRemoveClick(
          context: ComponentContext,
          @Prop data: PincodeSheetData,
          @Prop dismiss: () -> Unit) {
    data.onRemoveButtonClick()
    sPincodeSheetPasscodeEntered = ""
    dismiss()
  }
}

class PincodeBottomSheet : LithoBottomSheet() {
  var data = PincodeSheetData(
    title = R.string.no_pincode_sheet_title,
    actionTitle = R.string.no_pincode_sheet_details,
    onSuccess = {})

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    sPincodeSheetPasscodeEntered = ""
    return PincodeSheetView.create(componentContext)
      .data(data)
      .dismiss { dismiss() }
      .build()
  }

  override fun onResume() {
    super.onResume()
    val compatActivity = activity
    if (compatActivity === null || compatActivity !is AppCompatActivity) {
      return
    }

    if (data.isFingerprintEnabled) {
      showBiometricPrompt(compatActivity, this, {
        data.onSuccess()
        dismiss()
      })
    }
  }
}

fun openPincodeSetupSheet(
  activity: ThemedActivity,
  onCreateSuccess: () -> Unit) {

  openSheet(activity, PincodeBottomSheet().apply {
    data = PincodeSheetData(
      title = R.string.security_sheet_enter_new_pin_title,
      actionTitle = R.string.security_sheet_button_set,
      isFingerprintEnabled = false,
      isRemoveButtonEnabled = true,
      onRemoveButtonClick = {
        sSecurityCode = ""
        sSecurityAppLockEnabled = false
        sNoPinSetupNoticeShown = false
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

fun openVerifySheet(
  activity: ThemedActivity,
  onVerifySuccess: () -> Unit,
  onVerifyFailure: () -> Unit = {}) {
  openSheet(activity, PincodeBottomSheet().apply {
    data = PincodeSheetData(
      title = R.string.security_sheet_enter_current_pin_title,
      actionTitle = R.string.security_sheet_button_verify,
      onSuccess = onVerifySuccess,
      onFailure = onVerifyFailure,
      isFingerprintEnabled = isBiometricEnabled(activity)
    )
  })
}

fun openUnlockSheet(
  activity: ThemedActivity,
  onUnlockSuccess: () -> Unit,
  onUnlockFailure: () -> Unit) {
  if (!isPinCodeEnabled()) {
    if (sNoPinSetupNoticeShown) {
      onUnlockSuccess()
      return
    }
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
      actionTitle = R.string.security_sheet_button_unlock,
      onSuccess = onUnlockSuccess,
      onFailure = onUnlockFailure,
      isFingerprintEnabled = isBiometricEnabled(activity)
    )
  })
}