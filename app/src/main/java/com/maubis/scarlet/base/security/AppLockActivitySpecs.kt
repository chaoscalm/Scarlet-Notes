package com.maubis.scarlet.base.security

import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.InputType
import android.text.Layout
import android.view.View
import android.view.inputmethod.EditorInfo
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EditorActionEvent
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.TextChangedEvent
import com.facebook.litho.widget.TextInput
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.ui.ThemeColor

@LayoutSpec
object AppLockViewSpec {

  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop onTextChange: (String) -> Unit,
    @Prop onClick: () -> Unit): Component {
    return Column.create(context)
      .backgroundColor(appTheme.getColor(ThemeColor.BACKGROUND))
      .child(
        AppLockContentView.create(context)
          .onTextChange(onTextChange)
          .onClick(onClick)
          .flexGrow(1f))
      .child(
        Row.create(context)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.HORIZONTAL, 12f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .marginDip(YogaEdge.ALL, 16f)
          .child(EmptySpec.create(context).flexGrow(1f))
          .child(
            Text.create(context)
              .backgroundRes(R.drawable.accent_rounded_bg)
              .textSizeRes(R.dimen.font_size_large)
              .textColorRes(android.R.color.white)
              .textRes(R.string.security_sheet_button_unlock)
              .textAlignment(Layout.Alignment.ALIGN_CENTER)
              .paddingDip(YogaEdge.VERTICAL, 12f)
              .paddingDip(YogaEdge.HORIZONTAL, 20f)
              .typeface(appTypeface.title())
              .clickHandler(AppLockView.onUnlockClick(context))))
      .build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onUnlockClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object AppLockContentViewSpec {

  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    val editBackground = when {
      appTheme.isNightTheme() -> R.drawable.light_secondary_rounded_bg
      else -> R.drawable.secondary_rounded_bg
    }

    return Column.create(context)
      .paddingDip(YogaEdge.ALL, 16f)
      .backgroundColor(appTheme.getColor(ThemeColor.BACKGROUND))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_xxlarge)
          .textRes(R.string.app_lock_title)
          .textColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
          .typeface(appTypeface.heading()))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_large)
          .textColor(appTheme.getColor(ThemeColor.SECONDARY_TEXT))
          .textRes(R.string.app_lock_details)
          .typeface(appTypeface.title()))
      .child(EmptySpec.create(context).flexGrow(1f))
      .child(
        TextInput.create(context)
          .backgroundRes(editBackground)
          .textSizeRes(R.dimen.font_size_xlarge)
          .minWidthDip(128f)
          .inputFilter(InputFilter.LengthFilter(4))
          .hint("****")
          .alignSelf(YogaAlign.CENTER)
          .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
          .textAlignment(View.TEXT_ALIGNMENT_CENTER)
          .typeface(appTypeface.text())
          .textColorStateList(ColorStateList.valueOf(appTheme.getColor(ThemeColor.PRIMARY_TEXT)))
          .paddingDip(YogaEdge.HORIZONTAL, 22f)
          .paddingDip(YogaEdge.VERTICAL, 6f)
          .imeOptions(EditorInfo.IME_ACTION_DONE)
          .editorActionEventHandler(AppLockContentView.onPinEditorAction(context))
          .textChangedEventHandler(AppLockContentView.onTextChanged(context)))
      .child(EmptySpec.create(context).flexGrow(1f))
      .build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(EditorActionEvent::class)
  fun onPinEditorAction(context: ComponentContext, @Prop onClick: () -> Unit): Boolean {
    onClick()
    return true
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(TextChangedEvent::class)
  fun onTextChanged(context: ComponentContext, @FromEvent text: String, @Prop onTextChange: (String) -> Unit) {
    onTextChange(text)
  }
}