package com.maubis.scarlet.base.note.selection

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.utils.shareText
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.ActivitySelectNotesBinding
import com.maubis.scarlet.base.note.getFullText

const val KEY_EXTRA_SELECTED_NOTE_STATE = "EXTRA_SELECTED_NOTE_STATE"
const val KEY_EXTRA_SELECTED_NOTE_ID = "EXTRA_SELECTED_NOTE_ID"

class NotesSelectionActivity : SelectableNotesActivityBase() {

  private val selectedNotes = HashMap<Int, Note>()
  private val orderingNoteIds = ArrayList<Int>()

  private lateinit var views: ActivitySelectNotesBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivitySelectNotesBinding.inflate(layoutInflater)
    setContentView(views.root)
    fetchSelectedNote()
    initUI()
  }

  private fun fetchSelectedNote() {
    val noteId = intent.getIntExtra(KEY_EXTRA_SELECTED_NOTE_ID, 0)
    if (noteId != 0) {
      val note = data.notes.getByID(noteId)
      if (note != null) {
        orderingNoteIds.add(noteId)
        selectedNotes[noteId] = note
      }
    }
  }

  override fun initUI() {
    super.initUI()
    views.primaryFabAction.setOnClickListener {
      runTextFunction { text -> shareText(this, text) }
    }
    views.secondaryFabAction.setOnClickListener {
      openSheet(this, SelectedNotesActionsBottomSheet())
    }
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
          RecyclerView.SCROLL_STATE_DRAGGING -> {
            views.primaryFabAction.hide()
            views.secondaryFabAction.hide()
          }
          RecyclerView.SCROLL_STATE_IDLE -> {
            views.primaryFabAction.show()
            views.secondaryFabAction.show()
          }
        }
      }
    })
  }

  fun forEachSelectedNote(noteFunction: (Note) -> Unit) {
    for (note in selectedNotes.values) {
      noteFunction(note)
    }
  }

  fun runTextFunction(textFunction: (String) -> Unit) {
    textFunction(getSelectedNotesText())
  }

  fun refreshSelectedNotes() {
    for (key in selectedNotes.keys.toList()) {
      val note = data.notes.getByID(key)
      if (note === null) {
        selectedNotes.remove(key)
        continue
      }
      selectedNotes[key] = note
    }
  }

  fun getAllSelectedNotes() = selectedNotes.values

  override fun getLayoutUI() = R.layout.activity_select_notes

  override fun onNoteClicked(note: Note) {
    if (isNoteSelected(note)) {
      selectedNotes.remove(note.uid)
      orderingNoteIds.remove(note.uid)
    } else {
      selectedNotes[note.uid] = note
      orderingNoteIds.add(note.uid)
    }
    adapter.notifyDataSetChanged()

    if (selectedNotes.isEmpty()) {
      onBackPressed()
    }
  }

  fun getOrderedSelectedNotes(): List<Note> {
    val notes = ArrayList<Note>()
    for (noteId in orderingNoteIds) {
      val note = selectedNotes[noteId]
      if (note === null) {
        continue
      }
      notes.add(note)
    }
    return notes
  }

  override fun isNoteSelected(note: Note): Boolean = orderingNoteIds.contains(note.uid)

  override fun getNotes(): List<Note> {
    val state = NoteState.valueOf(intent.getStringExtra(KEY_EXTRA_SELECTED_NOTE_STATE) ?: NoteState.DEFAULT.name)
    return data.notes.getByNoteState(*noteStatesToBeIncluded(state))
  }

  private fun getSelectedNotesText(): String {
    val builder = StringBuilder()
    for (note in getOrderedSelectedNotes()) {
      builder.append(note.getFullText())
      builder.append("\n\n---\n\n")
    }
    return builder.toString()
  }

  private fun noteStatesToBeIncluded(noteState: NoteState): Array<NoteState> {
    return when (noteState) {
      NoteState.ARCHIVED -> arrayOf(NoteState.ARCHIVED)
      NoteState.TRASH -> arrayOf(NoteState.TRASH)
      // Favourite and "default" notes are displayed together in the home screen by default,
      // so if the user starts a selection on a favourite note we can't tell if they were
      // viewing only favourite notes
      else -> arrayOf(NoteState.DEFAULT, NoteState.FAVOURITE)
    }
  }
}
