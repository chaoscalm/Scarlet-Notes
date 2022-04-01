package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.VerticalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.ui.BottomSheetTabletDialog
import com.maubis.scarlet.base.common.ui.ThemeColorType

fun openSheet(activity: AppCompatActivity, sheet: LithoBottomSheet) {
  sheet.show(activity.supportFragmentManager, sheet.tag)
}

fun getLithoBottomSheetTitle(context: ComponentContext): Text.Builder {
  return Text.create(context)
    .textSizeRes(R.dimen.font_size_xxxlarge)
    .typeface(appTypeface.heading())
    .marginDip(YogaEdge.HORIZONTAL, 20f)
    .marginDip(YogaEdge.TOP, 18f)
    .marginDip(YogaEdge.BOTTOM, 8f)
    .textStyle(Typeface.BOLD)
    .textColor(appTheme.get(ThemeColorType.PRIMARY_TEXT))
}

fun getLithoBottomSheetButton(context: ComponentContext): Text.Builder {
  return Text.create(context)
    .typeface(appTypeface.title())
    .textSizeRes(R.dimen.font_size_large)
    .paddingDip(YogaEdge.VERTICAL, 12f)
    .paddingDip(YogaEdge.HORIZONTAL, 24f)
    .textColorRes(com.github.bijoysingh.uibasics.R.color.light_secondary_text)
    .backgroundRes(R.drawable.accent_rounded_bg)
}

abstract class LithoBottomSheet : BottomSheetDialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val ctx = context ?: activity
    if (ctx === null) {
      return super.onCreateDialog(savedInstanceState)
    }

    val isTablet = ctx.resources.getBoolean(R.bool.is_tablet)
    val dialog = when {
      isTablet -> BottomSheetTabletDialog(ctx, theme)
      else -> super.onCreateDialog(savedInstanceState)
    }
    refresh(ctx, dialog)
    retainInstance = true
    return dialog
  }

  fun refresh(context: Context, dialog: Dialog) {
    val componentContext = ComponentContext(context)
    getFullComponent(componentContext, dialog, getComponent(componentContext, dialog))
  }

  private fun getFullComponent(componentContext: ComponentContext, dialog: Dialog, childComponent: Component) {
    val topHandle = when (appTheme.isNightTheme()) {
      true -> R.drawable.bottom_sheet_top_handle_dark
      false -> R.drawable.bottom_sheet_top_handle_light
    }

    val baseComponent = Column.create(componentContext)
      .paddingDip(YogaEdge.TOP, topMargin())
      .paddingDip(YogaEdge.BOTTOM, bottomMargin())
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .backgroundColor(backgroundColor(componentContext))
      .child(
        Image.create(componentContext)
          .drawableRes(topHandle)
          .widthDip(72f)
          .heightDip(6f)
          .alpha(0.8f)
          .marginDip(YogaEdge.BOTTOM, 8f)
          .build()
      )
      .child(
        VerticalScroll.create(componentContext)
          .nestedScrollingEnabled(true)
          .childComponent(childComponent))
      .build()

    val contentView = LithoView.create(componentContext.androidContext, baseComponent)
    dialog.setContentView(contentView)

    val parentView = contentView.parent
    if (parentView is View) {
      parentView.setBackgroundColor(Color.TRANSPARENT)
    }
  }

  abstract fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component

  open fun backgroundColor(componentContext: ComponentContext) = appTheme.get(ThemeColorType.BACKGROUND)

  open fun topMargin() = 16f

  open fun bottomMargin() = 16f
}