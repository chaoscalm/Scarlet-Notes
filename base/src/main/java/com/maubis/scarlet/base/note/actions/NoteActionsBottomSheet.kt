package com.maubis.scarlet.base.note.actions

import android.app.Dialog
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.bijoysingh.uibasics.views.UILabelView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.ScarletIntentHandlerActivity
import com.maubis.scarlet.base.common.sheets.ColorPickerBottomSheet
import com.maubis.scarlet.base.common.sheets.ColorPickerDefaultController
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.OsVersionUtils
import com.maubis.scarlet.base.common.utils.addShortcut
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.home.sheets.openDeleteNotePermanentlySheet
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.folder.sheet.FolderChooserBottomSheet
import com.maubis.scarlet.base.note.selection.KEY_EXTRA_SELECTED_NOTE_ID
import com.maubis.scarlet.base.note.selection.KEY_EXTRA_SELECTED_NOTE_STATE
import com.maubis.scarlet.base.note.selection.NotesSelectionActivity
import com.maubis.scarlet.base.note.tag.TagChooserBottomSheet
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.reminders.ReminderBottomSheet
import com.maubis.scarlet.base.security.openUnlockSheet
import java.util.*

class NoteActionsBottomSheet : ThemedBottomSheetFragment() {
  private val note: Note by lazy {
    val noteId = requireArguments().getInt(KEY_NOTE_ID)
    data.notes.getByID(noteId) ?: throw IllegalArgumentException("Invalid note ID")
  }

  override fun setupDialogViews(dialog: Dialog) {
    setActionTitle(dialog, R.string.choose_action)
    setupGrid(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setupGrid(dialog: Dialog) {
    val gridLayoutIds = arrayOf(
      R.id.quick_actions_properties,
      R.id.note_properties,
      R.id.grid_layout)

    val gridOptionFunctions = arrayOf(
      this::getQuickActions,
      this::getNotePropertyActions,
      this::getExtraActions)
    gridOptionFunctions.forEachIndexed { index, function ->
      val items = function()
      val optionsGrid: GridLayout = dialog.findViewById(gridLayoutIds[index])
      if (items.isEmpty())
        optionsGrid.isVisible = false
      else
        setActions(optionsGrid, items)
    }
  }

  private fun getQuickActions(): List<NoteActionItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteActionsActivity) {
      return emptyList()
    }

    val actions = ArrayList<NoteActionItem>()
    actions.add(
      NoteActionItem(
        title = R.string.restore_note,
        icon = R.drawable.ic_restore,
        visible = note.state == NoteState.TRASH
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.edit_note,
        icon = R.drawable.ic_edit_white_48dp,
        visible = note.state != NoteState.TRASH
      ) {
        note.edit(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.select_action,
        icon = R.drawable.ic_action_select,
      ) {
        val intent = Intent(context, NotesSelectionActivity::class.java)
        intent.putExtra(KEY_EXTRA_SELECTED_NOTE_STATE, note.state.name)
        intent.putExtra(KEY_EXTRA_SELECTED_NOTE_ID, note.uid)
        activity.startActivity(intent)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.copy_note,
        icon = R.drawable.ic_content_copy_white_48dp,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.copyToClipboard(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.send_note,
        icon = R.drawable.ic_share_white_48dp,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.share(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.unarchive_note,
        icon = R.drawable.ic_archive_white_48dp,
        visible = note.state == NoteState.ARCHIVED
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.archive_note,
        icon = R.drawable.ic_archive_white_48dp,
        visible = note.state != NoteState.ARCHIVED
      ) {
        activity.markItem(note, NoteState.ARCHIVED)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        visible = note.state == NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        activity.moveItemToTrashOrDelete(note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.trash_note,
        icon = R.drawable.ic_delete_white_48dp,
        visible = note.state != NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        activity.moveItemToTrashOrDelete(note)
        dismiss()
      })
    return actions
  }

  private fun getNotePropertyActions(): List<NoteActionItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteActionsActivity || note.state == NoteState.TRASH) {
      return emptyList()
    }

    val actions = ArrayList<NoteActionItem>()
    actions.add(NoteActionItem(
      title = R.string.folder_option_change_notebook,
      icon = R.drawable.ic_folder,
      listener = {
        FolderChooserBottomSheet.openSheet(activity, note)
        dismiss()
      }
    ))
    actions.add(NoteActionItem(
      title = R.string.choose_note_color,
      icon = R.drawable.ic_action_color,
      listener = {
        val config = ColorPickerDefaultController(
          title = R.string.choose_note_color,
          colors = listOf(
            activity.resources.getIntArray(R.array.bright_colors),
            activity.resources.getIntArray(R.array.bright_colors_accent)),
          selectedColor = note.color,
          onColorSelected = { color ->
            note.color = color
            activity.updateNote(note)
          }
        )
        openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
        dismiss()
      }
    ))
    actions.add(NoteActionItem(
      title = R.string.change_tags,
      icon = R.drawable.ic_action_tags,
      listener = {
        TagChooserBottomSheet.openSheet(activity, note)
        dismiss()
      }
    ))
    actions.add(
      NoteActionItem(
        title = R.string.not_favourite_note,
        icon = R.drawable.ic_favorite_white_48dp,
        visible = note.state == NoteState.FAVOURITE
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.favourite_note,
        icon = R.drawable.ic_favorite_border_white_48dp,
        visible = note.state != NoteState.FAVOURITE,
        listener = {
          activity.markItem(note, NoteState.FAVOURITE)
          dismiss()
        }
      ))
    actions.add(
      NoteActionItem(
        title = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = {
          note.pinned = !note.pinned
          activity.updateNote(note)
          dismiss()
        }
      ))
    actions.add(
      NoteActionItem(
        title = R.string.lock_note,
        icon = R.drawable.ic_action_lock,
        visible = !note.locked,
        listener = {
          note.locked = true
          activity.updateNote(note)
          dismiss()
        }
      ))
    actions.add(
      NoteActionItem(
        title = R.string.unlock_note,
        icon = R.drawable.ic_action_unlock,
        visible = note.locked,
        listener = {
          openUnlockSheet(
            activity = activity,
            onUnlockSuccess = {
              note.locked = false
              activity.updateNote(note)
              dismiss()
            },
            onUnlockFailure = {})
        }
      ))
    return actions
  }

  private fun getExtraActions(): List<NoteActionItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteActionsActivity || note.state == NoteState.TRASH) {
      return emptyList()
    }

    val actions = ArrayList<NoteActionItem>()
    actions.add(
      NoteActionItem(
        title = R.string.share_images,
        icon = R.drawable.icon_share_image,
        visible = note.hasImages(),
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.shareImages(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.open_in_notification,
        icon = R.drawable.ic_action_notification,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        val handler = NotificationHandler(requireContext())
        handler.openNotification(NotificationConfig(note = note))
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.reminder,
        icon = R.drawable.ic_action_reminder_icon,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        ReminderBottomSheet.openSheet(activity, note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.pin_to_launcher,
        icon = R.drawable.icon_shortcut,
        visible = OsVersionUtils.canAddLauncherShortcuts(),
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        if (OsVersionUtils.canAddLauncherShortcuts()) {
          var title = note.getTitleForSharing()
          if (title.isBlank()) {
            title = note.getFullText().split("\n").firstOrNull() ?: "Note"
          }

          val shortcut = ShortcutInfo.Builder(activity, "scarlet_notes___${note.uuid}")
            .setShortLabel(title)
            .setLongLabel(title)
            .setIcon(Icon.createWithResource(activity, R.mipmap.open_note_launcher))
            .setIntent(ScarletIntentHandlerActivity.view(note))
            .build()
          addShortcut(activity, shortcut)
        }
      })
    actions.add(
      NoteActionItem(
        title = R.string.duplicate,
        icon = R.drawable.ic_duplicate,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        val copiedNote = note.shallowCopy()
        copiedNote.uid = 0
        copiedNote.uuid = UUID.randomUUID()
        copiedNote.save(activity)
        activity.notifyResetOrDismiss()
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.backup_note_include,
        icon = R.drawable.ic_action_backup,
        visible = note.excludeFromBackup,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.excludeFromBackup = false
        note.save(activity)
        activity.updateNote(note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.backup_note_exclude,
        icon = R.drawable.ic_action_backup_no,
        visible = !note.excludeFromBackup,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.excludeFromBackup = true
        note.save(activity)
        activity.updateNote(note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        visible = note.state != NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        openDeleteNotePermanentlySheet(activity, note) { activity.notifyResetOrDismiss() }
        dismiss()
      })
    return actions
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_note_options

  override fun getOptionsTitleColor(selected: Boolean): Int {
    return ContextCompat.getColor(requireContext(), com.github.bijoysingh.uibasics.R.color.light_primary_text)
  }

  private fun setActionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title)
    titleView.setTextColor(appTheme.get(ThemeColorType.SECONDARY_TEXT))
    titleView.setText(title)
  }

  private fun setActions(layoutGrid: GridLayout, options: List<NoteActionItem>) {
    val context = layoutGrid.context
    layoutGrid.columnCount = if (context.resources.getBoolean(R.bool.is_tablet)) 4 else 3
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_grid_item, null) as UILabelView
      contentView.label.typeface = appTypeface.title()
      contentView.setText(option.title)
      contentView.setImageResource(option.icon)
      contentView.setTextColor(getOptionsTitleColor(option.selected))
      contentView.setImageTint(getOptionsTitleColor(option.selected))

      if (!option.invalid) {
        contentView.setOnClickListener(option.listener)
      } else {
        contentView.alpha = 0.4f
      }
      layoutGrid.addView(contentView)
    }
  }

  companion object {
    private const val KEY_NOTE_ID = "note_id"

    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = NoteActionsBottomSheet()
      sheet.arguments = Bundle().apply { putInt(KEY_NOTE_ID, note.uid) }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}