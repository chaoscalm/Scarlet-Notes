package com.maubis.scarlet.base.backup.ui

import android.Manifest
import android.app.Dialog
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.common.ui.ThemeColorType

class StoragePermissionBottomSheet : LithoBottomSheet() {
  private val permissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions(), this::notifyRequestOutcome)

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.storage_permission_dialog_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textRes(R.string.storage_permission_dialog_details)
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        BottomSheetBar.create(componentContext)
          .primaryActionRes(R.string.storage_permission_dialog_action_allow)
          .onPrimaryClick {
            permissionRequestLauncher.launch(
              arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            )
          }
          .secondaryActionRes(R.string.delete_sheet_delete_trash_no)
          .onSecondaryClick { dismiss() }
          .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  private fun notifyRequestOutcome(grantStates: Map<String, Boolean>) {
    if (grantStates.values.all { it == true }) {
      Toast.makeText(context, R.string.notice_permission_granted, Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, R.string.notice_permission_denied, Toast.LENGTH_SHORT).show()
    }
    dismiss()
  }
}