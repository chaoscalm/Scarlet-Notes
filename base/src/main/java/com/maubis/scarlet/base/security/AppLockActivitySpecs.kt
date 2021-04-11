package com.maubis.scarlet.base.security

import android.text.InputType
import android.text.Layout
import android.view.inputmethod.EditorInfo
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.TextChangedEvent
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.getEditorActionListener

@LayoutSpec
object AppLockViewSpec {

  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop fingerprintEnabled: Boolean,
    @Prop onTextChange: (String) -> Unit,
    @Prop onClick: () -> Unit): Component {
    return Column.create(context)
      .backgroundColor(appTheme.get(ThemeColorType.BACKGROUND))
      .child(
        AppLockContentView.create(context)
          .fingerprintEnabled(fingerprintEnabled)
          .onTextChange(onTextChange)
          .onClick(onClick)
          .flexGrow(1f))
      .child(
        Row.create(context)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.HORIZONTAL, 12f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .marginDip(YogaEdge.ALL, 16f)
          .child(
            when {
              fingerprintEnabled -> Image.create(context)
                .drawableRes(R.drawable.ic_option_fingerprint)
                .heightDip(36f)
              else -> null
            }
          )
          .child(EmptySpec.create(context).flexGrow(1f))
          .child(
            Text.create(context)
              .backgroundRes(R.drawable.accent_rounded_bg)
              .textSizeRes(R.dimen.font_size_large)
              .textColorRes(R.color.white)
              .textRes(R.string.security_sheet_button_unlock)
              .textAlignment(Layout.Alignment.ALIGN_CENTER)
              .paddingDip(YogaEdge.VERTICAL, 12f)
              .paddingDip(YogaEdge.HORIZONTAL, 20f)
              .typeface(appTypeface.title())
              .clickHandler(AppLockView.onUnlockClick(context))))
      .build()
  }

  @OnEvent(ClickEvent::class)
  fun onUnlockClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object AppLockContentViewSpec {

  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop fingerprintEnabled: Boolean,
    @Prop onClick: () -> Unit): Component {
    val description = when {
      fingerprintEnabled -> R.string.app_lock_details
      else -> R.string.app_lock_details_no_fingerprint
    }
    val editBackground = when {
      appTheme.isNightTheme() -> R.drawable.light_secondary_rounded_bg
      else -> R.drawable.secondary_rounded_bg
    }

    return Column.create(context)
      .paddingDip(YogaEdge.ALL, 16f)
      .backgroundColor(appTheme.get(ThemeColorType.BACKGROUND))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_xxlarge)
          .textRes(R.string.app_lock_title)
          .textColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
          .typeface(appTypeface.heading()))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_large)
          .textColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
          .textRes(description)
          .typeface(appTypeface.title()))
      .child(EmptySpec.create(context).flexGrow(1f))
      .child(
        EditText.create(context)
          .backgroundRes(editBackground)
          .textSizeRes(R.dimen.font_size_xlarge)
          .minWidthDip(128f)
          .maxLength(4)
          .hint("****")
          .alignSelf(YogaAlign.CENTER)
          .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
          .textAlignment(Layout.Alignment.ALIGN_CENTER)
          .typeface(appTypeface.text())
          .textColor(appTheme.get(ThemeColorType.PRIMARY_TEXT))
          .paddingDip(YogaEdge.HORIZONTAL, 22f)
          .paddingDip(YogaEdge.VERTICAL, 6f)
          .imeOptions(EditorInfo.IME_ACTION_DONE)
          .editorActionListener(getEditorActionListener({
                                                          onClick()
                                                          true
                                                        }))
          .textChangedEventHandler(AppLockContentView.onTextChanged(context)))
      .child(EmptySpec.create(context).flexGrow(1f))
      .build()
  }

  @OnEvent(TextChangedEvent::class)
  fun onTextChanged(context: ComponentContext, @FromEvent text: String, @Prop onTextChange: (String) -> Unit) {
    onTextChange(text)
  }

}