package com.maubis.scarlet.base.config

import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.core.folder.FolderActor
import com.maubis.scarlet.base.core.note.NoteActor
import com.maubis.scarlet.base.core.tag.TagActor
import com.maubis.scarlet.base.database.FoldersProvider
import com.maubis.scarlet.base.database.NotesProvider
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseState
import com.maubis.scarlet.base.database.room.AppDatabase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag

abstract class CoreConfig {

  abstract fun database(): AppDatabase

  abstract fun authenticator(): IAuthenticator

  abstract fun notesDatabase(): NotesProvider

  abstract fun tagsDatabase(): TagsProvider

  abstract fun foldersDatabase(): FoldersProvider

  abstract fun noteActions(note: Note): NoteActor

  abstract fun tagActions(tag: Tag): TagActor

  abstract fun folderActions(folder: Folder): FolderActor

  abstract fun remoteDatabaseState(): IRemoteDatabaseState

  abstract fun startListener(activity: AppCompatActivity)

  companion object {
    val notesDb get() = instance.notesDatabase()
    val tagsDb get() = instance.tagsDatabase()
    val foldersDb get() = instance.foldersDatabase()
  }
}