package com.maubis.scarlet.base.home

import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.utils.sort
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.isLockedButAppUnlocked
import com.maubis.scarlet.base.settings.notesSortingTechniquePref

class SearchState(
    var text: String = "",
    var mode: HomeNavigationMode = HomeNavigationMode.DEFAULT,
    var currentFolder: Folder? = null,
    var colors: MutableList<Int> = mutableListOf<Int>(),
    var tags: MutableList<Tag> = mutableListOf<Tag>()) {

  fun hasFilter(): Boolean {
    return currentFolder != null
      || tags.isNotEmpty()
      || colors.isNotEmpty()
      || text.isNotBlank()
      || mode != HomeNavigationMode.DEFAULT
  }

  fun isFilteringByTag(tag: Tag) = tags.any { it.uuid == tag.uuid }

  fun clear(): SearchState {
    mode = HomeNavigationMode.DEFAULT
    text = ""
    colors.clear()
    tags.clear()
    currentFolder = null
    return this
  }

  fun clearSearchBar(): SearchState {
    text = ""
    colors.clear()
    tags.clear()
    return this
  }
}

fun findMatchingNotes(state: SearchState): List<Note> {
  val notes = findMatchingNotesIgnoringFolder(state)
    .filter { state.currentFolder?.uuid == it.folder }
  return sort(notes, notesSortingTechniquePref)
}

fun excludeNotesInFolders(notes: List<Note>): List<Note> {
  val notesWithoutFolder = notes.filter { it.folder !in data.folders.getAllUUIDs() }
  return sort(notesWithoutFolder, notesSortingTechniquePref)
}

fun findMatchingNotesIgnoringFolder(state: SearchState): List<Note> {
  return getNotesForMode(state)
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { note -> state.tags.isEmpty() || state.tags.any { note.tags.contains(it.uuid) } }
    .filter {
      when {
        state.text.isBlank() -> true
        it.locked && !it.isLockedButAppUnlocked() -> false
        else -> it.getFullText().contains(state.text, true)
      }
    }
}

fun findMatchingFolders(state: SearchState): List<Folder> {
  if (state.currentFolder != null) {
    return emptyList()
  }

  return data.folders.getAll()
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { it.title.contains(state.text, true) }
}

private fun getNotesForMode(state: SearchState): List<Note> {
  return when (state.mode) {
    HomeNavigationMode.FAVOURITE -> data.notes.getByNoteState(NoteState.FAVOURITE)
    HomeNavigationMode.ARCHIVED -> data.notes.getByNoteState(NoteState.ARCHIVED)
    HomeNavigationMode.TRASH -> data.notes.getByNoteState(NoteState.TRASH)
    HomeNavigationMode.DEFAULT -> data.notes.getByNoteState(NoteState.DEFAULT, NoteState.FAVOURITE)
    HomeNavigationMode.LOCKED -> data.notes.getNoteByLocked(true)
  }
}