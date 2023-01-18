package com.maubis.scarlet.base.note.selection

import android.app.Dialog
import androidx.core.content.ContextCompat
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.GridActionsBottomSheet
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.specs.GridActionItem
import com.maubis.scarlet.base.common.specs.GridSection
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
  override fun isAlwaysExpanded(): Boolean = false

  override fun titleRes(): Int? = null

  override fun getSections(componentContext: ComponentContext, dialog: Dialog): List<GridSection> {
    val sections = ArrayList<GridSection>()
    sections.add(getQuickActions(componentContext))
    sections.add(getSecondaryActions(componentContext))
    sections.add(getTertiaryActions(componentContext))
    return sections
  }

  private fun getQuickActions(componentContext: ComponentContext): GridSection {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val items = ArrayList<GridActionItem>()

    val allItemsInTrash = !activity.getAllSelectedNotes().any { it.state != NoteState.TRASH }
    items.add(
      GridActionItem(
        label = R.string.restore_note,
        icon = R.drawable.ic_restore,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInTrash
      ))

    val allItemsInFavourite = !activity.getAllSelectedNotes().any { it.state != NoteState.FAVOURITE }
    items.add(
      GridActionItem(
        label = R.string.not_favourite_note,
        icon = R.drawable.ic_favorite,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInFavourite
      ))
    items.add(
      GridActionItem(
        label = R.string.favourite_note,
        icon = R.drawable.ic_favorite_outline,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.FAVOURITE, activity)
          }
          activity.finish()
        },
        visible = !allItemsInFavourite
      ))

    val allItemsInArchived = !activity.getAllSelectedNotes().any { it.state != NoteState.ARCHIVED }
    items.add(
      GridActionItem(
        label = R.string.unarchive_note,
        icon = R.drawable.ic_archive,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.DEFAULT, activity)
          }
          activity.finish()
        },
        visible = allItemsInArchived
      ))
    items.add(
      GridActionItem(
        label = R.string.archive_note,
        icon = R.drawable.ic_archive,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.ARCHIVED, activity)
          }
          activity.finish()
        },
        visible = !allItemsInArchived
      ))
    items.add(GridActionItem(
      label = R.string.share_note,
      icon = R.drawable.ic_share,
      listener = lockAwareFunctionRunner(activity) {
        activity.runTextFunction { shareText(activity, it) }
      }
    ))
    items.add(GridActionItem(
      label = R.string.copy_note,
      icon = R.drawable.ic_copy,
      listener = lockAwareFunctionRunner(activity) {
        activity.runTextFunction {
          copyTextToClipboard(activity, it)
        }
        activity.finish()
      }
    ))
    items.add(
      GridActionItem(
        label = R.string.trash_note,
        icon = R.drawable.ic_delete,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.updateState(NoteState.TRASH, activity)
          }
          activity.finish()
        },
        visible = !allItemsInTrash
      ))

    return GridSection(
      items,
      sectionColor = ContextCompat.getColor(activity, com.github.bijoysingh.uibasics.R.color.material_blue_800))
  }

  private fun getSecondaryActions(componentContext: ComponentContext): GridSection {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val items = ArrayList<GridActionItem>()

    items.add(GridActionItem(
      label = R.string.change_tags,
      icon = R.drawable.ic_tag,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, SelectedTagChooserBottomSheet().apply {
          onActionListener = { tag, selectTag ->
            activity.forEachSelectedNote {
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

    items.add(GridActionItem(
      label = R.string.folder_option_change_notebook,
      icon = R.drawable.ic_notebook,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, MultipleNotesFolderChooserBottomSheet().apply {
          this.onActionListener = { folder, selectFolder ->
            activity.forEachSelectedNote {
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
    items.add(
      GridActionItem(
        label = R.string.lock_note,
        icon = R.drawable.ic_lock,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.locked = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allLocked
      ))
    items.add(
      GridActionItem(
        label = R.string.unlock_note,
        icon = R.drawable.ic_unlock,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.locked = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allLocked
      ))

    return GridSection(
      items,
      sectionColor = ContextCompat.getColor(activity, com.github.bijoysingh.uibasics.R.color.material_red_800))
  }

  private fun getTertiaryActions(componentContext: ComponentContext): GridSection {
    val activity = componentContext.androidContext as NotesSelectionActivity
    val items = ArrayList<GridActionItem>()

    val allItemsPinned = !activity.getAllSelectedNotes().any { !it.pinned }
    items.add(
      GridActionItem(
        label = R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.pinned = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allItemsPinned
      ))
    items.add(
      GridActionItem(
        label = R.string.unpin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.pinned = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allItemsPinned
      ))

    items.add(GridActionItem(
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

    items.add(GridActionItem(
      label = R.string.delete_note_permanently,
      icon = R.drawable.ic_delete_permanently,
      listener = lockAwareFunctionRunner(activity) {
        openSheet(activity, AlertBottomSheet().apply {
          config = AlertSheetConfig(
            description = R.string.delete_sheet_delete_selected_notes_permanently,
            onPositiveClick = {
              activity.forEachSelectedNote {
                it.delete(activity)
              }
              activity.finish()
            }
          )
        })
      }
    ))

    val allExcludedFromBackup = !activity.getAllSelectedNotes().any { !it.excludeFromBackup }
    items.add(
      GridActionItem(
        label = R.string.backup_note_include,
        icon = R.drawable.ic_backup_include,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.excludeFromBackup = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allExcludedFromBackup
      ))
    items.add(
      GridActionItem(
        label = R.string.backup_note_exclude,
        icon = R.drawable.ic_backup_exclude,
        listener = lockAwareFunctionRunner(activity) {
          activity.forEachSelectedNote {
            it.excludeFromBackup = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allExcludedFromBackup
      ))
    return GridSection(
      items,
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