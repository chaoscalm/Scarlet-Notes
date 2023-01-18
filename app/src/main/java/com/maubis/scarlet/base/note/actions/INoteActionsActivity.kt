package com.maubis.scarlet.base.note.actions

import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState

interface INoteActionsActivity {
  fun updateNote(note: Note)

  fun updateNoteState(note: Note, state: NoteState)

  fun moveNoteToTrashOrDelete(note: Note)

  fun notifyTagsChanged()

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}