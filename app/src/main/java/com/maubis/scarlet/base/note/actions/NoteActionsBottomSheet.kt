package com.maubis.scarlet.base.note.actions

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.bijoysingh.uibasics.views.UILabelView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.ColorPickerBottomSheet
import com.maubis.scarlet.base.common.sheets.ColorPickerDefaultController
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.OsVersionUtils
import com.maubis.scarlet.base.common.utils.ShortcutHandler
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.BottomSheetNoteActionsBinding
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
import com.maubis.scarlet.base.security.PincodeBottomSheet
import java.util.*

class NoteActionsBottomSheet : ThemedBottomSheetFragment() {
  private val note: Note by lazy {
    val noteId = requireArguments().getInt(KEY_NOTE_ID)
    data.notes.getByID(noteId) ?: throw IllegalArgumentException("Invalid note ID")
  }

  private lateinit var views: BottomSheetNoteActionsBinding

  override fun setupDialogViews(dialog: Dialog) {
    setupGrids()
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun inflateLayout(): View {
    views = BottomSheetNoteActionsBinding.inflate(layoutInflater)
    return views.root
  }

  private fun setupGrids() {
    val gridLayouts = arrayOf(views.quickActionsGrid, views.notePropertiesGrid, views.extraActionsGrid)
    val gridItemsCreators = arrayOf(this::getQuickActions, this::getNotePropertyActions, this::getExtraActions)

    gridItemsCreators.forEachIndexed { index, itemCreator ->
      val items = itemCreator()
      val grid = gridLayouts[index]
      if (items.isEmpty())
        grid.isVisible = false
      else
        setActions(grid, items)
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
        activity.updateNoteState(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.edit_note,
        icon = R.drawable.ic_edit,
        visible = note.state != NoteState.TRASH
      ) {
        note.edit(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.select_action,
        icon = R.drawable.ic_select,
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
        icon = R.drawable.ic_copy,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        note.copyToClipboard(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.share_note,
        icon = R.drawable.ic_share,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        note.share(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.unarchive_note,
        icon = R.drawable.ic_unarchive,
        visible = note.state == NoteState.ARCHIVED
      ) {
        activity.updateNoteState(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.archive_note,
        icon = R.drawable.ic_archive,
        visible = note.state != NoteState.ARCHIVED
      ) {
        activity.updateNoteState(note, NoteState.ARCHIVED)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        visible = note.state == NoteState.TRASH,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        openDeleteNotePermanentlySheet(activity, note) { activity.notifyResetOrDismiss() }
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.trash_note,
        icon = R.drawable.ic_delete,
        visible = note.state != NoteState.TRASH,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        activity.moveNoteToTrashOrDelete(note)
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
      icon = R.drawable.ic_notebook,
      listener = {
        FolderChooserBottomSheet.openSheet(activity, note)
        dismiss()
      }
    ))
    actions.add(NoteActionItem(
      title = R.string.choose_note_color,
      icon = R.drawable.ic_color_picker,
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
      icon = R.drawable.ic_tag,
      listener = {
        TagChooserBottomSheet.openSheet(activity, note)
        dismiss()
      }
    ))
    actions.add(
      NoteActionItem(
        title = R.string.not_favourite_note,
        icon = R.drawable.ic_favorite_outline,
        visible = note.state == NoteState.FAVOURITE
      ) {
        activity.updateNoteState(note, NoteState.DEFAULT)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.favourite_note,
        icon = R.drawable.ic_favorite,
        visible = note.state != NoteState.FAVOURITE,
        listener = {
          activity.updateNoteState(note, NoteState.FAVOURITE)
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
        icon = R.drawable.ic_lock,
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
        icon = R.drawable.ic_unlock,
        visible = note.locked,
        listener = {
          PincodeBottomSheet.openForUnlock(activity,
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
        icon = R.drawable.ic_share_images,
        visible = note.hasImages(),
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        note.shareImages(activity)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.open_in_notification,
        icon = R.drawable.ic_notification,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        val handler = NotificationHandler(requireContext())
        handler.openNotification(NotificationConfig(note = note))
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.reminder,
        icon = R.drawable.ic_reminder,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        ReminderBottomSheet.openSheet(activity, note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.pin_to_launcher,
        icon = R.drawable.ic_launcher_shortcut,
        visible = OsVersionUtils.canAddLauncherShortcuts(),
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        if (OsVersionUtils.canAddLauncherShortcuts())
          ShortcutHandler.addLauncherShortcut(activity, note)
      })
    actions.add(
      NoteActionItem(
        title = R.string.duplicate,
        icon = R.drawable.ic_duplicate,
        disabled = activity.lockedContentIsHidden() && note.locked
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
        icon = R.drawable.ic_backup_include,
        visible = note.excludeFromBackup,
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        note.excludeFromBackup = false
        note.save(activity)
        activity.updateNote(note)
        dismiss()
      })
    actions.add(
      NoteActionItem(
        title = R.string.backup_note_exclude,
        icon = R.drawable.ic_backup_exclude,
        visible = !note.excludeFromBackup,
        disabled = activity.lockedContentIsHidden() && note.locked
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
        disabled = activity.lockedContentIsHidden() && note.locked
      ) {
        openDeleteNotePermanentlySheet(activity, note) { activity.notifyResetOrDismiss() }
        dismiss()
      })
    return actions
  }

  private fun setActions(grid: GridLayout, items: List<NoteActionItem>) {
    val actionColor = ContextCompat.getColor(requireContext(), com.github.bijoysingh.uibasics.R.color.light_primary_text)
    grid.columnCount = if (requireContext().resources.getBoolean(R.bool.is_tablet)) 4 else 3
    for (item in items) {
      if (!item.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_grid_item, null) as UILabelView
      contentView.label.typeface = appTypeface.title()
      contentView.setText(item.title)
      contentView.setImageResource(item.icon)
      contentView.setTextColor(actionColor)
      contentView.setImageTint(actionColor)

      if (!item.disabled) {
        contentView.setOnClickListener(item.listener)
      } else {
        contentView.alpha = 0.4f
      }
      grid.addView(contentView)
    }
  }

  private class NoteActionItem(
    val title: Int,
    val icon: Int,
    val visible: Boolean = true,
    val disabled: Boolean = false,
    val listener: View.OnClickListener
  )

  companion object {
    private const val KEY_NOTE_ID = "note_id"

    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = NoteActionsBottomSheet()
      sheet.arguments = Bundle().apply { putInt(KEY_NOTE_ID, note.uid) }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}