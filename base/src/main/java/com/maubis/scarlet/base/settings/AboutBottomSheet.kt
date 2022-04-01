package com.maubis.scarlet.base.settings

import android.app.Dialog
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
import com.maubis.scarlet.base.common.ui.ThemeColorType

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
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.about_page_app_version)
          .typeface(appTypeface.title())
          .textColor(appTheme.get(ThemeColorType.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .typeface(appTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(BuildConfig.VERSION_NAME)
          .textColor(appTheme.get(ThemeColorType.TERTIARY_TEXT)))
    return component.build()
  }
}