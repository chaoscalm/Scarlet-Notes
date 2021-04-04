package com.maubis.scarlet.base.config

import android.content.Context
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.config.auth.NullAuthenticator
import com.maubis.scarlet.base.core.folder.FolderActor
import com.maubis.scarlet.base.core.note.NoteActor
import com.maubis.scarlet.base.core.tag.TagActor
import com.maubis.scarlet.base.database.FoldersProvider
import com.maubis.scarlet.base.database.NotesProvider
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.room.AppDatabase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag

class ApplicationConfig(context: Context) {
  val database: AppDatabase = AppDatabase.createDatabase(context)
  val notesProvider = NotesProvider()
  val tagsProvider = TagsProvider()
  val foldersProvider = FoldersProvider()

  fun authenticator(): IAuthenticator = NullAuthenticator()

  fun noteActions(note: Note) = NoteActor(note)
  fun tagActions(tag: Tag) = TagActor(tag)
  fun folderActions(folder: Folder) = FolderActor(folder)

  companion object {
    val notesDb get() = instance.notesProvider
    val tagsDb get() = instance.tagsProvider
    val foldersDb get() = instance.foldersProvider
  }
}