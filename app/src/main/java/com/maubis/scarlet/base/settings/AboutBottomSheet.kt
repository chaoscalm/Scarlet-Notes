package com.maubis.scarlet.base.settings

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.common.ui.ThemeColor

class AboutBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val appName = getString(R.string.app_name)
    val aboutAppDetails = getString(R.string.about_page_description, appName)
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.home_option_about_page)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(aboutAppDetails)
          .textColor(appTheme.getColor(ThemeColor.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.about_page_app_version)
          .typeface(appTypeface.title())
          .textColor(appTheme.getColor(ThemeColor.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(getString(R.string.about_page_app_version_number, BuildConfig.VERSION_NAME))
          .textColor(appTheme.getColor(ThemeColor.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.about_page_license)
          .typeface(appTypeface.title())
          .textColor(appTheme.getColor(ThemeColor.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textRes(R.string.about_page_license_description)
          .textColor(appTheme.getColor(ThemeColor.TERTIARY_TEXT)))
      .child(
        BottomSheetBar.create(componentContext)
          .primaryActionRes(R.string.about_page_license_read_full)
          .onPrimaryClick {
            tryOpenInBrowser("https://github.com/Fs00/Scarlet-Notes/blob/master/LICENSE")
          })
    return component.build()
  }

  private fun tryOpenInBrowser(url: String) {
    val licenseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
      startActivity(licenseIntent)
    } catch (_: ActivityNotFoundException) {
      Toast.makeText(context, R.string.web_browser_missing, Toast.LENGTH_SHORT).show()
    }
  }
}