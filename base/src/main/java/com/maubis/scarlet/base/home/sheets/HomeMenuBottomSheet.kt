package com.maubis.scarlet.base.home.sheets

import android.app.Dialog
import android.graphics.Typeface
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.OptionItemLayout
import com.maubis.scarlet.base.common.specs.RoundIcon
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.home.HomeNavigationMode
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.settings.SettingsBottomSheet

class HomeMenuBottomSheet : LithoBottomSheet() {

  override fun isAlwaysExpanded(): Boolean = true

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val items = getItems()
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .child(
        Column.create(componentContext)
          .paddingDip(YogaEdge.TOP, 20f)
          .paddingDip(YogaEdge.BOTTOM, 36f)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .child(
            Row.create(componentContext)
              .child(MenuItem.create(componentContext).props(items[0]).onClick { items[0].listener() })
              .child(MenuItem.create(componentContext).props(items[1]).onClick { items[1].listener() })
              .child(MenuItem.create(componentContext).props(items[2]).onClick { items[2].listener() })
          )
          .child(
            Row.create(componentContext)
              .child(MenuItem.create(componentContext).props(items[3]).onClick { items[3].listener() })
              .child(MenuItem.create(componentContext).props(items[4]).onClick { items[4].listener() })
              .child(MenuItem.create(componentContext).props(items[5]).onClick { items[5].listener() })
          ))
    return component.build()
  }

  override fun bottomMargin(): Float = 0f

  private fun getItems(): List<MenuItemProps> {
    val activity = context as MainActivity
    val items = ArrayList<MenuItemProps>()
    items.add(MenuItemProps(
      title = R.string.nav_home,
      icon = R.drawable.ic_home,
      listener = {
        activity.onModeChange(HomeNavigationMode.DEFAULT)
        dismiss()
      }
    ))
    items.add(MenuItemProps(
      title = R.string.nav_favourites,
      icon = R.drawable.ic_favorite,
      listener = {
        activity.onModeChange(HomeNavigationMode.FAVOURITE)
        dismiss()
      }
    ))
    items.add(MenuItemProps(
      title = R.string.nav_locked,
      icon = R.drawable.ic_lock,
      listener = {
        activity.onModeChange(HomeNavigationMode.LOCKED)
        dismiss()
      }
    ))
    items.add(MenuItemProps(
      title = R.string.nav_archived,
      icon = R.drawable.ic_archive,
      listener = {
        activity.onModeChange(HomeNavigationMode.ARCHIVED)
        dismiss()
      }
    ))
    items.add(MenuItemProps(
      title = R.string.nav_trash,
      icon = R.drawable.ic_delete,
      listener = {
        activity.onModeChange(HomeNavigationMode.TRASH)
        dismiss()
      }
    ))
    items.add(MenuItemProps(
      title = R.string.nav_settings,
      icon = R.drawable.ic_settings,
      listener = {
        SettingsBottomSheet.openSheet(activity)
        dismiss()
      }
    ))
    return items
  }
}

class MenuItemProps(
  val title: Int,
  val icon: Int,
  val visible: Boolean = true,
  val listener: () -> Unit)

@LayoutSpec
object MenuItemSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop props: MenuItemProps): Component {
    val titleColor = ScarletApp.appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val iconColor = ScarletApp.appTheme.getColor(ThemeColor.ICON)

    val row = Column.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.VERTICAL, 16f)
      .child(
        RoundIcon.create(context)
          .iconRes(props.icon)
          .bgColor(titleColor)
          .iconColor(iconColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(15)
          .marginDip(YogaEdge.BOTTOM, 4f))
      .child(
        Text.create(context)
          .textRes(props.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(ScarletApp.appTypeface.title())
          .textStyle(Typeface.BOLD)
          .textColor(titleColor))
    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}