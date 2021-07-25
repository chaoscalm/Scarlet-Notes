package com.maubis.scarlet.base.note.actions

import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.entities.Note

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}