package com.maubis.scarlet.base.common.utils

import androidx.annotation.StringRes
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.getFullText

enum class SortingTechnique(@StringRes val label: Int) {
  LAST_MODIFIED(R.string.sort_sheet_last_modified),
  NEWEST_FIRST(R.string.sort_sheet_newest_first),
  OLDEST_FIRST(R.string.sort_sheet_oldest_first),
  ALPHABETICAL(R.string.sort_sheet_alphabetical)
}

/**
 * Helper class which allow comparison of a pair of objects
 */
class ComparablePair<T : Comparable<T>, U : Comparable<U>>(private val first: T, private val second: U) : Comparable<ComparablePair<T, U>> {
  override fun compareTo(other: ComparablePair<T, U>): Int {
    val firstComparison = first.compareTo(other.first)
    return when {
      firstComparison == 0 -> second.compareTo(other.second)
      else -> firstComparison
    }
  }
}

fun sort(notes: List<Note>, sortingTechnique: SortingTechnique): List<Note> {
  return when (sortingTechnique) {
    SortingTechnique.LAST_MODIFIED -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.updateTimestamp
    }
    SortingTechnique.NEWEST_FIRST -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.timestamp
    }
    SortingTechnique.OLDEST_FIRST -> notes.sortedBy { note ->
      if (note.pinned) Long.MIN_VALUE
      else note.timestamp
    }
    SortingTechnique.ALPHABETICAL -> notes.sortedBy { note ->
      val content = note.getFullText().substringBefore('\n').filter { it.isLetterOrDigit() }
      val sortValue = when {
        (note.pinned || content.isBlank()) -> 0
        else -> content[0].uppercaseChar().code
      }
      ComparablePair(sortValue, note.updateTimestamp)
    }
  }
}