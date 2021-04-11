package com.maubis.scarlet.base.note.selection

import com.maubis.scarlet.base.database.room.note.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}