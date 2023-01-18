package com.maubis.scarlet.base.note.selection

import com.maubis.scarlet.base.database.entities.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}