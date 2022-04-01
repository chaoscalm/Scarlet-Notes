package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.Formats
import com.maubis.scarlet.base.core.format.MarkdownFormatting
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.creation.specs.NoteEditorBottomBar
import com.maubis.scarlet.base.note.formats.recycler.FormatImageViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatTextViewHolder
import com.maubis.scarlet.base.settings.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.ColorPickerDefaultController
import com.maubis.scarlet.base.support.recycler.SimpleItemTouchHelper
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

open class CreateNoteActivity : ViewAdvancedNoteActivity() {

  private var active = false
  private var maxUid = 0

  private var historyIndex = 0
  private var historySize = 0L
  private var historyModified = false
  private val history: MutableList<Note> = mutableListOf<Note>()

  override val editModeValue: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTouchListener()
    startHandler()
  }

  override fun onCreationFinished() {
    super.onCreationFinished()
    history.add(note.shallowCopy())
    setFolderFromIntent()
  }

  private fun setFolderFromIntent() {
    if (intent === null) {
      return
    }
    val folderUuid = intent.getSerializableExtra(INTENT_KEY_FOLDER) as UUID?
    if (folderUuid === null) {
      return
    }
    val folder = data.folders.getByUUID(folderUuid)
    if (folder === null) {
      return
    }
    note.folder = folder.uuid
  }

  private fun setTouchListener() {
    val callback = SimpleItemTouchHelper(adapter)
    val touchHelper = ItemTouchHelper(callback)
    touchHelper.attachToRecyclerView(views.formatsRecyclerView)
  }

  override fun displayNote() {
    super.displayNote()
    maxUid = formats.size + 1

    val isEmpty = formats.isEmpty()
    when {
      isEmpty -> {
        addEmptyItem(0, FormatType.HEADING)
        addDefaultItem()
      }
      !formats[0].text.startsWith("# ") &&
        formats[0].formatType !== FormatType.HEADING
        && formats[0].formatType !== FormatType.IMAGE -> {
        addEmptyItem(0, FormatType.HEADING)
      }
    }
    focus(0)
  }

  protected open fun addDefaultItem() {
    addEmptyItem(FormatType.TEXT)
  }

  override fun notifyToolbarColor() {
    super.notifyToolbarColor()
    setBottomToolbar()
  }

  override fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    views.lithoBottomToolbar.removeAllViews()
    views.lithoBottomToolbar.addView(
      LithoView.create(
        componentContext,
        NoteEditorBottomBar.create(componentContext)
          .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
          .build()))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
      override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
        if (imageFiles.isEmpty()) {
          return
        }

        val targetFile = imageStorage.storeExistingImage(note, imageFiles.first())
        val index = getFormatIndex(type)
        triggerImageLoaded(index, targetFile)
      }

      override fun onImagePickerError(exception: Exception, source: EasyImage.ImageSource, type: Int) {
        Toast.makeText(this@CreateNoteActivity, R.string.image_picker_error, Toast.LENGTH_LONG).show()
        Log.e("Scarlet", "EasyImage picker error", exception)
      }
    })
  }

  override fun onPause() {
    super.onPause()
    active = false
    updateNoteIfNeeded()
    deleteIfEmpty()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    tryClosingTheKeyboard()
  }

  override fun onResume() {
    super.onResume()
    active = true
  }

  override fun onResumeAction() {
    // do nothing
  }

  private fun deleteIfEmpty() {
    if (note.isNotPersisted()) {
      return
    }
    if (note.getFormats().isEmpty()) {
      note.delete(this)
    }
  }

  protected fun updateNoteIfNeeded() {
    val vLastNoteInstance = history.getOrNull(historyIndex) ?: note
    note.content = Formats.getEnhancedNoteContent(formats)

    // Ignore update if nothing changed. It allows for one undo per few seconds
    when {
      !historyModified && note.isEqual(vLastNoteInstance) -> return
      !historyModified -> addNoteToHistory(note.shallowCopy())
      else -> historyModified = false
    }
    note.updateTimestamp = System.currentTimeMillis()
    saveNoteIfNeeded()
  }

  @Synchronized
  private fun addNoteToHistory(note: Note) {
    while (historyIndex != history.size - 1) {
      history.removeAt(historyIndex)
    }

    history.add(note)
    historySize += note.content.length
    historyIndex += 1

    // 0.5MB limit on history
    if (historySize >= 1024 * 512 || history.size >= 15) {
      val item = history.removeAt(0)
      historySize -= item.content.length
      historyIndex -= 1
    }
  }

  private fun startHandler() {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(object : Runnable {
      override fun run() {
        if (active) {
          updateNoteIfNeeded()
          fullScreenView()
          handler.postDelayed(this, HANDLER_UPDATE_TIME.toLong())
        }
      }
    }, HANDLER_UPDATE_TIME.toLong())
  }

  protected fun addEmptyItem(type: FormatType) {
    addEmptyItem(formats.size, type)
  }

  private fun addEmptyItem(position: Int, type: FormatType) {
    val format = Format(type)
    format.uid = maxUid + 1
    maxUid++

    formats.add(position, format)
    adapter.addItem(format, position)
  }

  fun addEmptyItemAtFocused(type: FormatType) {
    if (focusedFormat == null) {
      addEmptyItem(type)
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      addEmptyItem(type)
      return
    }

    val newPosition = position + 1
    addEmptyItem(newPosition, type)
    views.formatsRecyclerView.layoutManager?.scrollToPosition(newPosition)
    focus(newPosition)
  }

  fun focus(position: Int) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestEditTextFocus()
    }, 100)
  }

  fun triggerMarkdown(formatting: MarkdownFormatting) {
    if (focusedFormat == null) {
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      return
    }

    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.insertMarkdownFormatting(formatting)
    }, 100)
  }

  fun triggerImageLoaded(position: Int, file: File) {
    if (position == -1) {
      return
    }

    val holder = findImageViewHolderAtPosition(position) ?: return
    holder.populateFile(file)

    val formatToChange = formats[position]
    if (formatToChange.text.isNotBlank()) {
      imageStorage.deleteImageIfExists(note.uuid.toString(), formatToChange)
    }
    formatToChange.text = file.name
    setFormat(formatToChange)
  }

  fun onHistoryClick(undo: Boolean) {
    when (undo) {
      true -> {
        historyIndex = if (historyIndex == 0) 0 else (historyIndex - 1)
        note = history[historyIndex].shallowCopy()
        displayNote()
        historyModified = true
      }
      false -> {
        val maxHistoryIndex = history.size - 1
        historyIndex = if (historyIndex == maxHistoryIndex) maxHistoryIndex else (historyIndex + 1)
        note = history[historyIndex].shallowCopy()
        displayNote()
        historyModified = true
      }
    }
  }

  fun onColorChangeClick() {
    val config = ColorPickerDefaultController(
      title = R.string.choose_note_color,
      colors = listOf(resources.getIntArray(R.array.bright_colors), resources.getIntArray(R.array.bright_colors_accent)),
      selectedColor = note.color,
      onColorSelected = { color ->
        setNoteColor(color)
      }
    )
    com.maubis.scarlet.base.support.sheets.openSheet(this, ColorPickerBottomSheet().apply { this.config = config })
  }

  private fun findTextViewHolderAtPosition(position: Int): FormatTextViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    return if (holder !== null && holder is FormatTextViewHolder) holder else null
  }

  private fun findImageViewHolderAtPosition(position: Int): FormatImageViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    return if (holder !== null && holder is FormatImageViewHolder) holder else null
  }

  private fun findViewHolderAtPositionAggressively(position: Int): RecyclerView.ViewHolder? {
    var holder: RecyclerView.ViewHolder? = views.formatsRecyclerView.findViewHolderForAdapterPosition(position)
    if (holder == null) {
      holder = views.formatsRecyclerView.findViewHolderForLayoutPosition(position)
      if (holder == null) {
        return null
      }
    }
    return holder
  }

  override fun setNoteColor(color: Int) {
    if (lastKnownNoteColor == color) {
      return
    }
    note.color = color
    notifyToolbarColor()
    lastKnownNoteColor = color
  }

  override fun setFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }
    formats[position] = format
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(formats, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(formats, i, i - 1)
      }
    }
    updateNoteIfNeeded()
  }

  override fun deleteFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position < 0) {
      return
    }
    focusedFormat = if (focusedFormat == null || focusedFormat!!.uid == format.uid) null else focusedFormat
    formats.removeAt(position)
    adapter.removeItem(position)
    updateNoteIfNeeded()
  }

  override fun createOrChangeToNextFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    val isCheckList =
      (format.formatType === FormatType.CHECKLIST_UNCHECKED
        || format.formatType === FormatType.CHECKLIST_CHECKED)
    val newPosition = position + 1
    when {
      isCheckList -> addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED.getNextFormatType())
      newPosition < formats.size -> focus(position + 1)
      else -> addEmptyItemAtFocused(format.formatType.getNextFormatType())
    }
  }

  companion object {
    const val HANDLER_UPDATE_TIME = 4000
    const val INTENT_KEY_FOLDER = "key_folder"

    fun getNewNoteIntent(context: Context, folderUuid: UUID?): Intent {
      val intent = Intent(context, CreateNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folderUuid)
      return intent
    }

    fun getNewChecklistNoteIntent(context: Context, folderUuid: UUID?): Intent {
      val intent = Intent(context, CreateListNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folderUuid)
      return intent
    }
  }
}
