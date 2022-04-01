package com.maubis.scarlet.base.note.selection

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.utils.SharingUtils
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.databinding.ActivitySelectNotesBinding
import com.maubis.scarlet.base.home.HomeNavigationMode
import com.maubis.scarlet.base.note.getFullText

const val KEY_SELECT_EXTRA_MODE = "KEY_SELECT_EXTRA_MODE"
const val KEY_SELECT_EXTRA_NOTE_ID = "KEY_SELECT_EXTRA_NOTE_ID"

class SelectNotesActivity : SelectableNotesActivityBase() {

  private val selectedNotes = HashMap<Int, Note>()
  private val orderingNoteIds = ArrayList<Int>()

  private lateinit var views: ActivitySelectNotesBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivitySelectNotesBinding.inflate(layoutInflater)
    setContentView(views.root)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      val noteId = getIntent().getIntExtra(KEY_SELECT_EXTRA_NOTE_ID, 0)
      if (noteId != 0) {
        val note = data.notes.getByID(noteId)
        if (note !== null) {
          orderingNoteIds.add(noteId)
          selectedNotes.put(noteId, note)
        }
      }
    }

    initUI()
  }

  override fun initUI() {
    super.initUI()
    views.primaryFabAction.setOnClickListener {
      runTextFunction { text -> SharingUtils.shareText(this, text) }
    }
    views.secondaryFabAction.setOnClickListener {
      openSheet(this, SelectedNotesOptionsBottomSheet())
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

  fun runNoteFunction(noteFunction: (Note) -> Unit) {
    for (note in selectedNotes.values) {
      noteFunction(note)
    }
  }

  fun runTextFunction(textFunction: (String) -> Unit) {
    textFunction(getText())
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
      selectedNotes.put(note.uid, note)
      orderingNoteIds.add(note.uid)
    }
    adapter.notifyDataSetChanged()

    if (selectedNotes.isEmpty()) {
      onBackPressed()
    }
  }

  override fun isNoteSelected(note: Note): Boolean = orderingNoteIds.contains(note.uid)

  override fun getNotes(): List<Note> = data.notes.getAll()

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

  private fun getText(): String {
    val builder = StringBuilder()
    for (note in getOrderedSelectedNotes()) {
      builder.append(note.getFullText())
      builder.append("\n\n---\n\n")
    }
    return builder.toString()
  }

  fun getMode(navigationMode: String): Array<String> {
    return when (navigationMode) {
      HomeNavigationMode.FAVOURITE.name -> arrayOf(HomeNavigationMode.FAVOURITE.name)
      HomeNavigationMode.ARCHIVED.name -> arrayOf(HomeNavigationMode.ARCHIVED.name)
      HomeNavigationMode.TRASH.name -> arrayOf(HomeNavigationMode.TRASH.name)
      else -> arrayOf(HomeNavigationMode.DEFAULT.name, HomeNavigationMode.FAVOURITE.name)
    }
  }
}
