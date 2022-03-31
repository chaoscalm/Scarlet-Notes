package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity

data class FolderOptionsItem(
        val folder: Folder,
        val isSelected: Boolean = false,
        val listener: () -> Unit = {})

@LayoutSpec
object FolderItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop option: FolderOptionsItem): Component {
    val titleColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
    val selectedColor = when (appTheme.isNightTheme()) {
      true -> context.getColor(com.github.bijoysingh.uibasics.R.color.material_blue_400)
      false -> context.getColor(com.github.bijoysingh.uibasics.R.color.material_blue_700)
    }

    val icon: Int
    val bgColor: Int
    val bgAlpha: Int
    val textColor: Int
    val typeface: Typeface
    when (option.isSelected) {
      true -> {
        icon = R.drawable.ic_folder
        bgColor = selectedColor
        bgAlpha = 200
        textColor = selectedColor
        typeface = appTypeface.subHeading()
      }
      false -> {
        icon = R.drawable.ic_folder
        bgColor = titleColor
        bgAlpha = 15
        textColor = titleColor
        typeface = appTypeface.title()
      }
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        RoundIcon.create(context)
          .iconRes(icon)
          .bgColor(bgColor)
          .iconColor(titleColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(bgAlpha)
          .onClick { }
          .isClickDisabled(true)
          .marginDip(YogaEdge.END, 16f))
      .child(
        Text.create(context)
          .flexGrow(1f)
          .text(option.folder.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(typeface)
          .textStyle(Typeface.BOLD)
          .textColor(textColor))
    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop option: FolderOptionsItem) {
    option.listener()
  }
}

abstract class FolderChooserBottomSheetBase : LithoBottomSheet() {

  var dismissListener: () -> Unit = {}

  protected abstract fun preComponentRender(componentContext: ComponentContext)
  protected abstract fun onFolderSelected(folder: Folder)
  protected abstract fun isFolderSelected(folder: Folder): Boolean

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    preComponentRender(componentContext)
    val activity = context as ThemedActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
    val foldersComponent = Column.create(componentContext)
      .paddingDip(YogaEdge.TOP, 8f)
      .paddingDip(YogaEdge.BOTTOM, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.folder_option_change_notebook)
          .marginDip(YogaEdge.BOTTOM, 12f))
    getFolderOptions().forEach {
      foldersComponent.child(FolderItemLayout.create(componentContext).option(it))
    }

    val addTag = LithoOptionsItem(
      title = R.string.folder_sheet_add_note,
      subtitle = 0,
      icon = R.drawable.icon_add_notebook,
      listener = {
        CreateOrEditFolderBottomSheet.openSheet(activity, Folder(sNoteDefaultColor)) { folder, _ ->
          onFolderSelected(folder)
          refresh(activity, dialog)
        }
      })
    foldersComponent.child(OptionItemLayout.create(componentContext)
                             .option(addTag)
                             .backgroundRes(R.drawable.accent_rounded_bg)
                             .marginDip(YogaEdge.TOP, 16f)
                             .onClick { addTag.listener() })

    component.child(foldersComponent)
    return component.build()
  }

  private fun getFolderOptions(): List<FolderOptionsItem> {
    val activity = context as AppCompatActivity
    val options = ArrayList<FolderOptionsItem>()
    for (folder in data.folders.getAll()) {
      options.add(
        FolderOptionsItem(
          folder = folder,
          listener = {
            onFolderSelected(folder)
            refresh(activity, requireDialog())
          },
          isSelected = isFolderSelected(folder)
        ))
    }
    return options
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    dismissListener()
  }
}