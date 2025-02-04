package com.maubis.scarlet.base.editor.specs

import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.specs.EmptySpec
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.common.specs.bottomBar
import com.maubis.scarlet.base.common.specs.bottomBarRoundIcon
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.home.sheets.openDeleteNotePermanentlySheet
import com.maubis.scarlet.base.note.copyToClipboard
import com.maubis.scarlet.base.note.share

@LayoutSpec
object NoteViewBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as ViewNoteActivity
    val note = activity.note()
    val row = Row.create(context)
      .widthPercent(100f)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
      .alignItems(YogaAlign.CENTER)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_bottom_menu)
                .onClick { activity.openMoreOptions() })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    if (note.state != NoteState.TRASH) {
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_delete)
          .onClick { activity.moveNoteToTrashOrDelete(note) })
    }
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_copy)
                .onClick { note.copyToClipboard(activity) })
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_share)
                .onClick { note.share(activity) })

    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
    if (note.state == NoteState.TRASH) {
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_delete_permanently)
          .onClick {
            openDeleteNotePermanentlySheet(activity, note) { activity.notifyResetOrDismiss() }
          })
    } else {
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_edit)
          .onClick { activity.openEditor() })
    }
    return bottomBar(context, row.build(), colorConfig).build()
  }
}
