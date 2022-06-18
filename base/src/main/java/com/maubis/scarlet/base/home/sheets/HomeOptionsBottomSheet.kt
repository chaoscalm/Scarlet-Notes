package com.maubis.scarlet.base.home.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.LithoBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoLabelOptionsItem
import com.maubis.scarlet.base.common.sheets.OptionLabelItemLayout
import com.maubis.scarlet.base.home.HomeNavigationMode
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.settings.SettingsBottomSheet

class HomeOptionsBottomSheet : LithoBottomSheet() {

  override fun isAlwaysExpanded(): Boolean = true

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val options = getOptions()
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .child(
        Column.create(componentContext)
          .paddingDip(YogaEdge.TOP, 20f)
          .paddingDip(YogaEdge.BOTTOM, 36f)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .child(
            Row.create(componentContext)
              .child(OptionLabelItemLayout.create(componentContext).option(options[0]).onClick { options[0].listener() })
              .child(OptionLabelItemLayout.create(componentContext).option(options[1]).onClick { options[1].listener() })
              .child(OptionLabelItemLayout.create(componentContext).option(options[2]).onClick { options[2].listener() })
          )
          .child(
            Row.create(componentContext)
              .child(OptionLabelItemLayout.create(componentContext).option(options[3]).onClick { options[3].listener() })
              .child(OptionLabelItemLayout.create(componentContext).option(options[4]).onClick { options[4].listener() })
              .child(OptionLabelItemLayout.create(componentContext).option(options[5]).onClick { options[5].listener() })
          ))
    return component.build()
  }

  override fun bottomMargin(): Float = 0f

  private fun getOptions(): List<LithoLabelOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoLabelOptionsItem>()
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_home,
      icon = R.drawable.ic_home_white_48dp,
      listener = {
        activity.onModeChange(HomeNavigationMode.DEFAULT)
        dismiss()
      }
    ))
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_favourites,
      icon = R.drawable.ic_favorite,
      listener = {
        activity.onModeChange(HomeNavigationMode.FAVOURITE)
        dismiss()
      }
    ))
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_locked,
      icon = R.drawable.ic_lock,
      listener = {
        activity.onModeChange(HomeNavigationMode.LOCKED)
        dismiss()
      }
    ))
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_archived,
      icon = R.drawable.ic_archive,
      listener = {
        activity.onModeChange(HomeNavigationMode.ARCHIVED)
        dismiss()
      }
    ))
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_trash,
      icon = R.drawable.ic_delete,
      listener = {
        activity.onModeChange(HomeNavigationMode.TRASH)
        dismiss()
      }
    ))
    options.add(LithoLabelOptionsItem(
      title = R.string.nav_settings,
      icon = R.drawable.ic_settings,
      listener = {
        SettingsBottomSheet.openSheet(activity)
        dismiss()
      }
    ))
    return options
  }
}