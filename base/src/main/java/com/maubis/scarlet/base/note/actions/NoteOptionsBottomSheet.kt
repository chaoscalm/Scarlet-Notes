package com.maubis.scarlet.base.note.actions

import android.app.Dialog
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.home.sheets.openDeleteNotePermanentlySheet
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.note.folder.sheet.FolderChooserBottomSheet
import com.maubis.scarlet.base.note.reminders.ReminderBottomSheet
import com.maubis.scarlet.base.note.selection.KEY_SELECT_EXTRA_MODE
import com.maubis.scarlet.base.note.selection.KEY_SELECT_EXTRA_NOTE_ID
import com.maubis.scarlet.base.note.selection.SelectNotesActivity
import com.maubis.scarlet.base.note.tag.TagChooserBottomSheet
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.settings.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.ColorPickerDefaultController
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.support.utils.addShortcut
import java.util.*

class NoteOptionsBottomSheet : GridBottomSheetBase() {

  var noteFn: () -> Note? = { null }

  override fun setupViewWithDialog(dialog: Dialog) {
    val note = noteFn()
    if (note === null) {
      dismiss()
      return
    }

    setOptionTitle(dialog, R.string.choose_action)
    setupGrid(dialog, note)
    setupCardViews(note)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setupGrid(dialog: Dialog, note: Note) {
    val gridLayoutIds = arrayOf(
      R.id.quick_actions_properties,
      R.id.note_properties,
      R.id.grid_layout)

    val gridOptionFunctions = arrayOf(
      { noteForAction: Note -> getQuickActions(noteForAction) },
      { noteForAction: Note -> getNotePropertyOptions(noteForAction) },
      { noteForAction: Note -> getOptions(noteForAction) })
    gridOptionFunctions.forEachIndexed { index, function ->
      val items = function(note)
      val optionsGrid: GridLayout = dialog.findViewById(gridLayoutIds[index])
      if (items.isEmpty())
        optionsGrid.isVisible = false
      else
        setOptions(optionsGrid, items)
    }
  }

  private fun setupCardViews(note: Note) {
    val activity = context as ThemedActivity
    val dlg = dialog
    if (activity !is INoteOptionSheetActivity || dlg === null) {
      return
    }

    val selectCardTitle = dlg.findViewById<TextView>(R.id.select_notes_title)
    selectCardTitle.typeface = appTypeface.title()
    val selectCardSubtitle = dlg.findViewById<TextView>(R.id.select_notes_subtitle)
    selectCardSubtitle.typeface = appTypeface.title()

    val tagCardLayout = dlg.findViewById<View>(R.id.tag_card_layout)
    val tags = tagCardLayout.findViewById<TextView>(R.id.tags_title)
    tags.typeface = appTypeface.title()
    val tagSubtitle = tagCardLayout.findViewById<TextView>(R.id.tags_subtitle)
    tagSubtitle.typeface = appTypeface.title()
    tagCardLayout.setOnClickListener {
      openSheet(activity, TagChooserBottomSheet(note, dismissListener = { activity.notifyTagsChanged(note) }))
      dismiss()
    }

    val selectCardLayout = dlg.findViewById<View>(R.id.select_notes_layout)
    selectCardLayout.setOnClickListener {
      val intent = Intent(context, SelectNotesActivity::class.java)
      intent.putExtra(KEY_SELECT_EXTRA_MODE, activity.getSelectMode(note))
      intent.putExtra(KEY_SELECT_EXTRA_NOTE_ID, note.uid)
      activity.startActivity(intent)
      dismiss()
    }
    selectCardLayout.visibility = View.VISIBLE
  }

  private fun getQuickActions(note: Note): List<OptionsItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(
      OptionsItem(
          title = R.string.restore_note,
          icon = R.drawable.ic_restore,
          visible = note.state == NoteState.TRASH
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    options.add(OptionsItem(
        title = R.string.edit_note,
        icon = R.drawable.ic_edit_white_48dp,
        visible = note.state != NoteState.TRASH
    ) {
      note.edit(activity)
      dismiss()
    })
    options.add(
      OptionsItem(
          title = R.string.not_favourite_note,
          icon = R.drawable.ic_favorite_white_48dp,
          visible = note.state == NoteState.FAVOURITE
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.favourite_note,
          icon = R.drawable.ic_favorite_border_white_48dp,
          visible = note.state != NoteState.FAVOURITE
      ) {
        activity.markItem(note, NoteState.FAVOURITE)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.unarchive_note,
          icon = R.drawable.ic_archive_white_48dp,
          visible = note.state == NoteState.ARCHIVED
      ) {
        activity.markItem(note, NoteState.DEFAULT)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.archive_note,
          icon = R.drawable.ic_archive_white_48dp,
          visible = note.state != NoteState.ARCHIVED
      ) {
        activity.markItem(note, NoteState.ARCHIVED)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.send_note,
          icon = R.drawable.ic_share_white_48dp,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.share(activity)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.copy_note,
          icon = R.drawable.ic_content_copy_white_48dp,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.copyToClipboard(activity)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.delete_note_permanently,
          icon = R.drawable.ic_delete_permanently,
          visible = note.state == NoteState.TRASH,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        activity.moveItemToTrashOrDelete(note)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.trash_note,
          icon = R.drawable.ic_delete_white_48dp,
          visible = note.state != NoteState.TRASH,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        activity.moveItemToTrashOrDelete(note)
        dismiss()
      })
    return options
  }

  private fun getNotePropertyOptions(note: Note): List<OptionsItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity || note.state == NoteState.TRASH) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.choose_note_color,
        icon = R.drawable.ic_action_color
    ) {
      val config = ColorPickerDefaultController(
          title = R.string.choose_note_color,
          colors = listOf(
              activity.resources.getIntArray(R.array.bright_colors), activity.resources.getIntArray(R.array.bright_colors_accent)),
          selectedColor = note.color,
          onColorSelected = { color ->
            note.color = color
            activity.updateNote(note)
          }
      )
      openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
      dismiss()
    })
    options.add(OptionsItem(
        title = if (note.folder == null) R.string.folder_option_add_to_notebook else R.string.folder_option_change_notebook,
        icon = R.drawable.ic_folder
    ) {
      openSheet(activity, FolderChooserBottomSheet(note).apply {
        dismissListener = { activity.notifyResetOrDismiss() }
      })
      dismiss()
    })
    options.add(
      OptionsItem(
          title = R.string.lock_note,
          icon = R.drawable.ic_action_lock,
          visible = !note.locked
      ) {
        note.locked = true
        activity.updateNote(note)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.unlock_note,
          icon = R.drawable.ic_action_unlock,
          visible = note.locked
      ) {
        openUnlockSheet(
            activity = activity,
            onUnlockSuccess = {
              note.locked = false
              activity.updateNote(note)
              dismiss()
            },
            onUnlockFailure = { })
      })
    return options
  }

  private fun getOptions(note: Note): List<OptionsItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity || note.state == NoteState.TRASH) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        icon = R.drawable.ic_pin
    ) {
      note.pinned = !note.pinned
      activity.updateNote(note)
      dismiss()
    })
    options.add(
      OptionsItem(
          title = R.string.share_images,
          icon = R.drawable.icon_share_image,
          visible = note.hasImages(),
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        note.shareImages(activity)
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.open_in_notification,
          icon = R.drawable.ic_action_notification,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        val handler = NotificationHandler(themedContext())
        handler.openNotification(NotificationConfig(note = note))
        dismiss()
      })
    options.add(
      OptionsItem(
          title = R.string.delete_note_permanently,
          icon = R.drawable.ic_delete_permanently,
          visible = note.state != NoteState.TRASH,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        openDeleteNotePermanentlySheet(activity, note, { activity.notifyResetOrDismiss() })
        dismiss()
      })
    options.add(
      OptionsItem(
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
                      .setIntent(NoteIntentRouterActivity.view(note))
                      .build()
              addShortcut(activity, shortcut)
          }
      })
    options.add(
      OptionsItem(
          title = R.string.reminder,
          icon = R.drawable.ic_action_reminder_icon,
          invalid = activity.lockedContentIsHidden() && note.locked
      ) {
        ReminderBottomSheet.openSheet(activity, note)
        dismiss()
      })
    options.add(
      OptionsItem(
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
    options.add(
      OptionsItem(
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
    options.add(
      OptionsItem(
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
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_note_options

  override fun getBackgroundCardViewIds(): Array<Int> = emptyArray()

  override fun getOptionsTitleColor(selected: Boolean): Int {
    return ContextCompat.getColor(themedContext(), com.github.bijoysingh.uibasics.R.color.light_primary_text)
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = NoteOptionsBottomSheet()
      sheet.noteFn = { note }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}