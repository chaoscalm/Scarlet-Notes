package com.maubis.scarlet.base.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.sheets.ColorPickerBottomSheet
import com.maubis.scarlet.base.common.sheets.ColorPickerDefaultController
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.editor.recycler.*
import com.maubis.scarlet.base.editor.specs.NoteEditorBottomBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

open class EditNoteActivity : ViewNoteActivity() {

  private var maxUid = 0

  private val history = mutableListOf<String>()
  private var currentHistoryPosition = 0

  private lateinit var formatsTouchHelper: ItemTouchHelper

  override val isEditingMode: Boolean = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setDragHandlesTouchListener()
    startBackgroundNoteAutosave()
    if (note.isNotPersisted())
      setNoteDataFromIntent()
    if (savedInstanceState == null)
      history.add(note.content)
  }

  override fun onSaveInstanceState(savedInstanceState: Bundle) {
    super.onSaveInstanceState(savedInstanceState)
    savedInstanceState.putStringArrayList(KEY_HISTORY, ArrayList(history))
    savedInstanceState.putInt(KEY_HISTORY_POSITION, currentHistoryPosition)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    history.addAll(savedInstanceState.getStringArrayList(KEY_HISTORY)!!)
    currentHistoryPosition = savedInstanceState.getInt(KEY_HISTORY_POSITION)
  }

  private fun setNoteDataFromIntent() {
    if (intent == null) {
      return
    }
    note.folder = intent.getSerializableExtra(INTENT_KEY_NOTE_FOLDER) as UUID?
    note.locked = intent.getBooleanExtra(INTENT_KEY_NOTE_LOCKED, false)
    note.state = NoteState.valueOf(intent.getStringExtra(INTENT_KEY_NOTE_STATE) ?: NoteState.DEFAULT.toString())
  }

  private fun setDragHandlesTouchListener() {
    formatsTouchHelper = ItemTouchHelper(FormatTouchHelperCallback(adapter))
    formatsTouchHelper.attachToRecyclerView(views.formatsRecyclerView)
  }

  private fun startBackgroundNoteAutosave() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.RESUMED) {
        while (true) {
          delay(NOTE_AUTOSAVE_INTERVAL)
          addSnapshotToHistoryIfNeeded()
          saveNoteIfNeeded()
        }
      }
    }
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
        formats[0].type !== FormatType.HEADING
        && formats[0].type !== FormatType.IMAGE -> {
        addEmptyItem(0, FormatType.HEADING)
      }
    }
    focusTextFormat(0)
  }

  protected open fun addDefaultItem() {
    addEmptyItem(FormatType.TEXT)
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

  @Suppress("DEPRECATION")
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
        Toast.makeText(this@EditNoteActivity, R.string.image_picker_error, Toast.LENGTH_LONG).show()
        Log.e("Scarlet", "EasyImage picker error", exception)
      }
    })
  }

  override fun onPause() {
    super.onPause()
    saveOrDeleteNote()
  }

  private fun saveOrDeleteNote() {
    note.content = Formats.getEnhancedNoteContent(formats)
    if (note.isEmpty()) {
      note.delete(this)
    } else {
      saveNote()
    }
  }

  private fun saveNoteIfNeeded() {
    note.content = Formats.getEnhancedNoteContent(formats)
    if (note.isEmpty() && note.isNotPersisted()) {
      return
    }
    saveNote()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    tryClosingTheKeyboard()
  }

  override fun onResumeAction() {
    // do nothing
  }

  private fun addSnapshotToHistoryIfNeeded() {
    val currentNoteContent = Formats.getEnhancedNoteContent(formats)
    if (currentNoteContent == history[currentHistoryPosition])
      return

    while (currentHistoryPosition < history.lastIndex) {
      history.removeLast()
    }

    history.add(currentNoteContent)
    currentHistoryPosition += 1

    if (history.size >= 25) {
      history.removeAt(0)
      currentHistoryPosition -= 1
    }
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
    focusTextFormat(newPosition)
  }

  private fun focusTextFormat(position: Int) {
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

  fun performUndo() {
    addSnapshotToHistoryIfNeeded()
    if (currentHistoryPosition == 0)
      return

    currentHistoryPosition -= 1
    note.content = history[currentHistoryPosition]
    displayNote()
  }

  fun performRedo() {
    if (currentHistoryPosition == history.lastIndex)
      return

    currentHistoryPosition += 1
    note.content = history[currentHistoryPosition]
    displayNote()
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
    com.maubis.scarlet.base.common.sheets.openSheet(this, ColorPickerBottomSheet().apply { this.config = config })
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

  override fun startFormatDrag(viewHolder: RecyclerView.ViewHolder) {
    formatsTouchHelper.startDrag(viewHolder)
  }

  private fun setNoteColor(color: Int) {
    if (note.color == color) {
      return
    }
    note.color = color
    notifyToolbarColor()
  }

  override fun setFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }
    formats[position] = format
  }

  override fun onFormatMoved(fromPosition: Int, toPosition: Int) {
    Collections.swap(formats, fromPosition, toPosition)
  }

  override fun deleteFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position < 0) {
      return
    }
    focusedFormat = if (focusedFormat == null || focusedFormat!!.uid == format.uid) null else focusedFormat
    formats.removeAt(position)
    adapter.removeItem(position)
    addSnapshotToHistoryIfNeeded()
  }

  override fun createOrChangeToNextFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    val isCheckList =
      (format.type === FormatType.CHECKLIST_UNCHECKED
        || format.type === FormatType.CHECKLIST_CHECKED)
    val newPosition = position + 1
    when {
      isCheckList -> addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED.getNextFormatType())
      newPosition < formats.size -> focusTextFormat(position + 1)
      else -> addEmptyItemAtFocused(format.type.getNextFormatType())
    }
  }

  override fun setFormatChecked(format: Format, checked: Boolean) {
    format.type = if (checked) FormatType.CHECKLIST_CHECKED else FormatType.CHECKLIST_UNCHECKED
    note.content = Formats.getEnhancedNoteContent(Formats.sortChecklistsIfAllowed(formats))
    saveNote()
    displayNote()
    addSnapshotToHistoryIfNeeded()
  }

  override fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
    return listOf(
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.TEXT.ordinal)
        .layoutFile(R.layout.item_format_text)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.HEADING.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatHeadingViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.SUB_HEADING.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatHeadingViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.HEADING_3.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatHeadingViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.QUOTE.ordinal)
        .layoutFile(R.layout.item_format_quote)
        .holderClass(FormatQuoteViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.CODE.ordinal)
        .layoutFile(R.layout.item_format_code)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.CHECKLIST_CHECKED.ordinal)
        .layoutFile(R.layout.item_format_list)
        .holderClass(FormatListViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal)
        .layoutFile(R.layout.item_format_list)
        .holderClass(FormatListViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.IMAGE.ordinal)
        .layoutFile(R.layout.item_format_image)
        .holderClass(FormatImageViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.SEPARATOR.ordinal)
        .layoutFile(R.layout.item_format_separator)
        .holderClass(FormatSeparatorViewHolder::class.java)
        .build()
    )
  }

  companion object {
    private const val INTENT_KEY_NOTE_FOLDER = "key_folder"
    private const val INTENT_KEY_NOTE_LOCKED = "key_locked"
    private const val INTENT_KEY_NOTE_STATE = "key_note_state"

    private const val NOTE_AUTOSAVE_INTERVAL: Long = 3000
    private const val KEY_HISTORY = "note_history"
    private const val KEY_HISTORY_POSITION = "history_position"

    fun makeNewNoteIntent(context: Context, folderUuid: UUID?, noteState: NoteState, locked: Boolean): Intent {
      val intent = Intent(context, EditNoteActivity::class.java)
      return intentWithNoteData(intent, folderUuid, noteState, locked)
    }

    fun makeNewChecklistNoteIntent(context: Context, folderUuid: UUID?, noteState: NoteState, locked: Boolean): Intent {
      val intent = Intent(context, CreateListNoteActivity::class.java)
      return intentWithNoteData(intent, folderUuid, noteState, locked)
    }

    private fun intentWithNoteData(intent: Intent, folderUuid: UUID?, noteState: NoteState, locked: Boolean): Intent {
      return intent.apply {
        putExtra(INTENT_KEY_NOTE_FOLDER, folderUuid)
        putExtra(INTENT_KEY_NOTE_LOCKED, locked)
        putExtra(INTENT_KEY_NOTE_STATE, noteState.toString())
      }
    }

    fun makeEditNoteIntent(context: Context, note: Note): Intent {
      return Intent(context, EditNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
    }
  }
}
