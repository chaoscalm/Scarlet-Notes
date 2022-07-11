package com.maubis.scarlet.base.note.selection

import android.app.Dialog
import androidx.core.content.ContextCompat
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.GridActionsBottomSheet
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.GridSectionItem
import com.maubis.scarlet.base.common.specs.GridSectionOptionItem
import com.maubis.scarlet.base.common.utils.copyTextToClipboard
import com.maubis.scarlet.base.common.utils.shareText
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.editor.Formats
import com.maubis.scarlet.base.editor.Formats.toNoteContent
import com.maubis.scarlet.base.home.sheets.AlertBottomSheet
import com.maubis.scarlet.base.home.sheets.AlertSheetConfig
import com.maubis.scarlet.base.note.folder.sheet.MultipleNotesFolderChooserBottomSheet
import com.maubis.scarlet.base.note.tag.SelectedTagChooserBottomSheet
import com.maubis.scarlet.base.security.PincodeBottomSheet

class SelectedNotesActionsBottomSheet : GridActionsBottomSheet() {
  override fun title(): Int = R.string.choose_action

  override fun getItems(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem> {
    val items = ArrayList<GridSectionItem>()
    items.add(getQuickActions(componentContext))
    items.add(getSecondaryActions(componentContext))
    items.add(getTertiaryActions(componentContext))
    return items
  }

  private fun getQuickActions(componentContext: ComponentContext): GridSectionItem {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val options = ArrayList<GridSectionOptionItem>()

    val allItemsInTrash = !activity.getAllSelectedNotes().any { it.state != NoteState.TRASH }
    options.add(
      GridSectionOptionItem(
        label = R.string.restore_note,
        icon = R.drawable.ic_restore,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInTrash
      ))

    val allItemsInFavourite = !activity.getAllSelectedNotes().any { it.state != NoteState.FAVOURITE }
    options.add(
      GridSectionOptionItem(
        label = R.string.not_favourite_note,
        icon = R.drawable.ic_favorite,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInFavourite
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.favourite_note,
        icon = R.drawable.ic_favorite_outline,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.FAVOURITE, activity)
          }
          activity.finish()
        },
        visible = !allItemsInFavourite
      ))

    val allItemsInArchived = !activity.getAllSelectedNotes().any { it.state != NoteState.ARCHIVED }
    options.add(
      GridSectionOptionItem(
        label = R.string.unarchive_note,
        icon = R.drawable.ic_archive,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInArchived
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.archive_note,
        icon = R.drawable.ic_archive,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.ARCHIVED, activity)
          }
          activity.finish()
        },
        visible = !allItemsInArchived
      ))
    options.add(GridSectionOptionItem(
      label = R.string.share_note,
      icon = R.drawable.ic_share,
      listener = lockAwareFunctionRunner(activity) {
        activity.runTextFunction { shareText(activity, it) }
      }
    ))
    options.add(GridSectionOptionItem(
      label = R.string.copy_note,
      icon = R.drawable.ic_copy,
      listener = lockAwareFunctionRunner(activity) {
        activity.runTextFunction {
          copyTextToClipboard(activity, it)
        }
        activity.finish()
      }
    ))
    options.add(
      GridSectionOptionItem(
        label = R.string.trash_note,
        icon = R.drawable.ic_delete,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.updateState(NoteState.TRASH, activity)
          }
          activity.finish()
        },
        visible = !allItemsInTrash
      ))

    return GridSectionItem(
      options = options,
      sectionColor = ContextCompat.getColor(activity, com.github.bijoysingh.uibasics.R.color.material_blue_800))
  }

  private fun getSecondaryActions(componentContext: ComponentContext): GridSectionItem {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val options = ArrayList<GridSectionOptionItem>()

    options.add(GridSectionOptionItem(
      label = R.string.change_tags,
      icon = R.drawable.ic_tag,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, SelectedTagChooserBottomSheet().apply {
          onActionListener = { tag, selectTag ->
            activity.forEachNote {
              when (selectTag) {
                true -> it.tags.add(tag.uuid)
                false -> it.tags.remove(tag.uuid)
              }
              it.save(activity)
            }
            activity.finish()
          }
        })
      }
    ))


    options.add(GridSectionOptionItem(
      label = R.string.folder_option_change_notebook,
      icon = R.drawable.ic_notebook,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, MultipleNotesFolderChooserBottomSheet().apply {
          this.onActionListener = { folder, selectFolder ->
            activity.forEachNote {
              when (selectFolder) {
                true -> it.folder = folder.uuid
                false -> it.folder = null
              }
              it.save(activity)
            }
            activity.finish()
          }
        })
      }
    ))

    val allLocked = !activity.getAllSelectedNotes().any { !it.locked }
    options.add(
      GridSectionOptionItem(
        label = R.string.lock_note,
        icon = R.drawable.ic_lock,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.locked = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allLocked
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.unlock_note,
        icon = R.drawable.ic_unlock,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.locked = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allLocked
      ))

    return GridSectionItem(
      options = options,
      sectionColor = ContextCompat.getColor(activity, com.github.bijoysingh.uibasics.R.color.material_red_800))
  }

  private fun getTertiaryActions(componentContext: ComponentContext): GridSectionItem {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val options = ArrayList<GridSectionOptionItem>()

    val allItemsPinned = !activity.getAllSelectedNotes().any { !it.pinned }
    options.add(
      GridSectionOptionItem(
        label = R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.pinned = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allItemsPinned
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.unpin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.pinned = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allItemsPinned
      ))

    options.add(GridSectionOptionItem(
      label = R.string.merge_notes,
      icon = R.drawable.ic_merge,
      listener = lockAwareFunctionRunner(activity) {
        val selectedNotes = activity.getOrderedSelectedNotes().toMutableList()
        if (selectedNotes.isEmpty()) {
          return@lockAwareFunctionRunner
        }

        val note = selectedNotes.firstOrNull()
        if (note === null) {
          return@lockAwareFunctionRunner
        }

        val formats = note.contentAsFormats().toMutableList()
        selectedNotes.removeAt(0)
        for (noteToAdd in selectedNotes) {
          formats.addAll(noteToAdd.contentAsFormats())
          noteToAdd.delete(activity)
        }
        note.content = Formats.sortChecklistsIfAllowed(formats).toNoteContent()
        note.save(activity)
        activity.finish()
      }
    ))

    options.add(GridSectionOptionItem(
      label = R.string.delete_note_permanently,
      icon = R.drawable.ic_delete_permanently,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, AlertBottomSheet().apply {
          config = AlertSheetConfig(
            description = R.string.delete_sheet_delete_selected_notes_permanently,
            onPositiveClick = {
              activity.forEachNote {
                it.delete(activity)
              }
              activity.finish()
            }
          )
        })
      }
    ))

    val allExcludedFromBackup = !activity.getAllSelectedNotes().any { !it.excludeFromBackup }
    options.add(
      GridSectionOptionItem(
        label = R.string.backup_note_include,
        icon = R.drawable.ic_backup_include,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.excludeFromBackup = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allExcludedFromBackup
      ))
    options.add(
      GridSectionOptionItem(
        label = R.string.backup_note_exclude,
        icon = R.drawable.ic_backup_exclude,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachNote {
            it.excludeFromBackup = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allExcludedFromBackup
      ))
    return GridSectionItem(
      options = options,
      sectionColor = ContextCompat.getColor(activity, com.github.bijoysingh.uibasics.R.color.material_teal_800))
  }

  private fun lockAwareFunctionRunner(
    activity: NotesSelectionActivity,
    listener: () -> Unit): () -> Unit = {
    val hasLockedNote = activity.getAllSelectedNotes().any { it.locked }
    when {
      hasLockedNote -> {
        PincodeBottomSheet.openForUnlock(activity,
          onUnlockSuccess = {
            listener()
            dismiss()
          },
          onUnlockFailure = {})
      }
      else -> {
        listener()
        dismiss()
      }
    }
  }
}