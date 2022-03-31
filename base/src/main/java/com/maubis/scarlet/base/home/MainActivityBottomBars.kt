package com.maubis.scarlet.base.home

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.text.Layout
import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.home.sheets.HomeOptionsBottomSheet
import com.maubis.scarlet.base.home.sheets.openDeleteTrashSheet
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.bottomBarCard
import com.maubis.scarlet.base.support.specs.bottomBarRoundIcon
import com.maubis.scarlet.base.support.utils.ColorUtil
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.support.utils.addShortcut

@LayoutSpec
object MainActivityBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig,
    @Prop isInsideFolder: Boolean,
    @Prop isInTrash: Boolean
  ): Component {
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_apps_white_48dp)
                .onClick {
                  openSheet(activity, HomeOptionsBottomSheet())
                })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    if (isInTrash) {
      row.child(
        bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_delete_permanently)
          .onClick { openDeleteTrashSheet(activity) }
      )
    }
    else {
      if (!isInsideFolder) {
        row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.icon_add_notebook)
                .onClick {
                  CreateOrEditFolderBottomSheet.openSheet(
                          activity,
                          Folder(sNoteDefaultColor),
                          { _, _ -> activity.refreshItems() })
                })
      }
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.icon_add_list)
          .onClick {
            val intent = CreateNoteActivity.getNewChecklistNoteIntent(
                activity,
                activity.state.currentFolder?.uuid)
            activity.startActivity(intent)
          })
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.icon_add_note)
          .isLongClickEnabled(true)
          .onLongClick {
            if (!OsVersionUtils.canAddLauncherShortcuts()) {
              return@onLongClick
            }

            val shortcut = ShortcutInfo.Builder(activity, "scarlet_notes___create_note")
                .setShortLabel(activity.getString(R.string.shortcut_add_note))
                .setLongLabel(activity.getString(R.string.shortcut_add_note))
                .setIcon(Icon.createWithResource(activity, R.mipmap.create_launcher))
                .setIntent(NoteIntentRouterActivity.create())
                .build()
            addShortcut(activity, shortcut)
          }
          .onClick {
            val intent = CreateNoteActivity.getNewNoteIntent(
                activity,
                activity.state.currentFolder?.uuid)
            activity.startActivity(intent)
          })
    }
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}

@LayoutSpec
object MainActivityFolderBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop folder: Folder): Component {
    val colorConfig = ToolbarColorConfig(
      toolbarBackgroundColor = folder.color,
      toolbarIconColor = when (ColorUtil.isLightColor(folder.color)) {
        true -> context.getColor(com.github.bijoysingh.uibasics.R.color.dark_tertiary_text)
        false -> context.getColor(com.github.bijoysingh.uibasics.R.color.light_secondary_text)
      }
    )
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_arrow_back_white_48dp)
                .onClick { activity.onFolderChange(null) })
    row.child(
      Text.create(context)
        .typeface(appTypeface.title())
        .textAlignment(Layout.Alignment.ALIGN_CENTER)
        .flexGrow(1f)
        .text(folder.title)
        .textSizeRes(R.dimen.font_size_normal)
        .textColor(colorConfig.toolbarIconColor)
        .clickHandler(MainActivityFolderBottomBar.onClickEvent(context)))
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_edit_white_48dp)
                .isClickDisabled(true)
                .clickHandler(MainActivityFolderBottomBar.onClickEvent(context))
                .onClick {})
    return bottomBarCard(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop folder: Folder) {
    val activity = context.androidContext as MainActivity
    if (activity.state.currentFolder != null) {
      CreateOrEditFolderBottomSheet.openSheet(activity, folder) { _, _ -> activity.notifyFolderChange() }
    }
  }
}
