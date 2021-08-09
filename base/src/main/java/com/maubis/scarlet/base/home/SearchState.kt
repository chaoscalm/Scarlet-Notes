package com.maubis.scarlet.base.home

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked
import com.maubis.scarlet.base.settings.SortingOptionsBottomSheet

class SearchState(
        var text: String = "",
        var mode: HomeNavigationMode = HomeNavigationMode.DEFAULT,
        var currentFolder: Folder? = null,
        var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
        var tags: MutableList<Tag> = emptyList<Tag>().toMutableList()) {

  fun hasFilter(): Boolean {
    return currentFolder != null
      || tags.isNotEmpty()
      || colors.isNotEmpty()
      || text.isNotBlank()
      || mode !== HomeNavigationMode.DEFAULT
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

  fun copy(): SearchState {
    return SearchState(
      text,
      mode,
      currentFolder,
      colors.filter { true }.toMutableList(),
      tags.filter { true }.toMutableList())
  }
}

fun unifiedSearchSynchronous(state: SearchState): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = unifiedSearchWithoutFolder(state)
    .filter {
      val currentFolder = state.currentFolder
      if (currentFolder == null)
        it.folder.isBlank()
      else
        currentFolder.uuid == it.folder
    }
  return sort(notes, sorting)
}

fun filterFolder(notes: List<Note>, folder: Folder): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val filteredNotes = notes.filter { it.folder == folder.uuid }
  return sort(filteredNotes, sorting)
}

fun filterOutFolders(notes: List<Note>): List<Note> {
  val allFoldersUUIDs = ScarletApp.data.folders.getAll().map { it.uuid }
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val filteredNotes = notes.filter { !allFoldersUUIDs.contains(it.folder) }
  return sort(filteredNotes, sorting)
}

fun unifiedSearchWithoutFolder(state: SearchState): List<Note> {
  return getNotesForMode(state)
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { note -> state.tags.isEmpty() || state.tags.filter { note.tags.contains(it.uuid) }.isNotEmpty() }
    .filter {
      when {
        state.text.isBlank() -> true
        it.locked && !it.isNoteLockedButAppUnlocked() -> false
        else -> it.getFullText().contains(state.text, true)
      }
    }
}

fun filterDirectlyValidFolders(state: SearchState): List<Folder> {
  if (state.currentFolder != null) {
    return emptyList()
  }

  return data.folders.getAll()
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { it.title.contains(state.text, true) }
}

fun getNotesForMode(state: SearchState): List<Note> {
  return when (state.mode) {
    HomeNavigationMode.FAVOURITE -> data.notes.getByNoteState(arrayOf(NoteState.FAVOURITE))
    HomeNavigationMode.ARCHIVED -> data.notes.getByNoteState(arrayOf(NoteState.ARCHIVED))
    HomeNavigationMode.TRASH -> data.notes.getByNoteState(arrayOf(NoteState.TRASH))
    HomeNavigationMode.DEFAULT -> data.notes.getByNoteState(arrayOf(NoteState.DEFAULT, NoteState.FAVOURITE))
    HomeNavigationMode.LOCKED -> data.notes.getNoteByLocked(true)
  }
}