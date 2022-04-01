package com.maubis.scarlet.base.note.creation.specs

import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.note.copy
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.share
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.bottomBarCard
import com.maubis.scarlet.base.support.specs.bottomBarRoundIcon

@LayoutSpec
object NoteViewBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as ViewAdvancedNoteActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_apps_white_48dp)
                .onClick { activity.openMoreOptions() })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.icon_delete)
                .onClick { activity.moveItemToTrashOrDelete(activity.note()) })
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_content_copy_white_48dp)
                .onClick { activity.note().copy(activity) })
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_share_white_48dp)
                .onClick { activity.note().share(activity) })


    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_edit_white_48dp)
                .onClick { activity.openEditor() })
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}
