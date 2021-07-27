package com.maubis.scarlet.base.note.creation.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.sectionPreservingSort
import com.maubis.scarlet.base.core.note.*
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.ActivityAdvancedNoteBinding
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.actions.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.actions.NoteOptionsBottomSheet
import com.maubis.scarlet.base.note.creation.specs.NoteViewBottomBar
import com.maubis.scarlet.base.note.creation.specs.NoteViewTopBar
import com.maubis.scarlet.base.note.formats.FormatAdapter
import com.maubis.scarlet.base.note.formats.IFormatRecyclerViewActivity
import com.maubis.scarlet.base.note.formats.getFormatControllerItems
import com.maubis.scarlet.base.note.formats.recycler.KEY_EDITABLE
import com.maubis.scarlet.base.note.formats.recycler.KEY_NOTE_COLOR
import com.maubis.scarlet.base.settings.STORE_KEY_TEXT_SIZE
import com.maubis.scarlet.base.settings.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sEditorTextSize
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.settings.sUIUseNoteColorAsBackground
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.ui.*
import com.maubis.scarlet.base.support.utils.ColorUtil
import com.maubis.scarlet.base.support.utils.ColorUtil.darkOrDarkerColor
import com.maubis.scarlet.base.widget.getPendingIntentWithStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

const val INTENT_KEY_NOTE_ID = "NOTE_ID"

data class NoteViewColorConfig(
  var backgroundColor: Int = Color.BLACK,
  var toolbarBackgroundColor: Int = Color.BLACK,
  var toolbarIconColor: Int = Color.BLACK,
  var statusBarColor: Int = Color.BLACK)

open class ViewAdvancedNoteActivity : SecuredActivity(), INoteOptionSheetActivity, IFormatRecyclerViewActivity {

  var focusedFormat: Format? = null

  protected var note: Note? = null
  protected lateinit var formats: MutableList<Format>
  protected val formatsInitialised = AtomicBoolean(false)

  protected lateinit var views: ActivityAdvancedNoteBinding
  protected lateinit var adapter: FormatAdapter

  val creationFinished = AtomicBoolean(false)
  val colorConfig = NoteViewColorConfig()
  var lastKnownNoteColor = 0

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityAdvancedNoteBinding.inflate(layoutInflater)
    setContentView(views.root)
    setRecyclerView()

    GlobalScope.launch(Dispatchers.IO) {
      var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
      if (noteId == 0 && savedInstanceState != null) {
        noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
      }
      if (noteId != 0) {
        note = data.notes.getByID(noteId)
      }
      if (note === null) {
        note = NoteBuilder().emptyNote(sNoteDefaultColor)
      }
      GlobalScope.launch(Dispatchers.Main) {
        resetBundle()
        setNote()
        notifyThemeChange()
        onCreationFinished()
      }
      creationFinished.set(true)
    }
  }

  override fun onResume() {
    super.onResume()

    if (!creationFinished.get()) {
      return
    }
    onResumeAction()
    notifyThemeChange()
  }

  protected open fun onCreationFinished() {}

  protected open fun onResumeAction() {
    GlobalScope.launch(Dispatchers.IO) {
      note = data.notes.getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
      when {
        note == null -> finish()
        else -> GlobalScope.launch(Dispatchers.Main) { setNote() }
      }
    }
  }

  private fun resetBundle() {
    val currentNote = note
    val bundle = Bundle()
    bundle.putBoolean(KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, appPreferences.getBoolean(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, appTheme.isNightTheme())
    bundle.putInt(STORE_KEY_TEXT_SIZE, sEditorTextSize)
    bundle.putInt(KEY_NOTE_COLOR, currentNote?.adjustedColor() ?: sNoteDefaultColor)
    bundle.putString(INTENT_KEY_NOTE_ID, currentNote?.uuid ?: generateUUID())
    adapter.setExtra(bundle)
  }

  protected open fun setNote() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    setNoteColor(currentNote.color)
    adapter.clearItems()

    formats = when (editModeValue) {
      true -> currentNote.getFormats()
      false -> currentNote.getSmartFormats()
    }.toMutableList()
    adapter.addItems(formats)
    formatsInitialised.set(true)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }
  }

  private fun maybeAddTags() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val tagLabel = currentNote.getTagString()
    if (tagLabel.isEmpty()) {
      return
    }

    val format = Format(FormatType.TAG, tagLabel)
    adapter.addItem(format)
  }

  private fun maybeAddEmptySpace() {
    adapter.addItem(Format(FormatType.EMPTY))
  }

  private fun setRecyclerView() {
    adapter = FormatAdapter(this)
    views.formatsRecyclerView.adapter = adapter
    views.formatsRecyclerView.layoutManager = LinearLayoutManager(this)
    views.formatsRecyclerView.setHasFixedSize(false)
  }

  open fun setFormat(format: Format) {
    // do nothing
  }

  open fun createOrChangeToNextFormat(format: Format) {
    // do nothing
  }

  open fun setFormatChecked(format: Format, checked: Boolean) {
    val trueFormats = note!!.getFormats().toMutableList()
    val truePosition = trueFormats.indexOfFirst { it.uid == format.uid }
    if (truePosition == -1) {
      return
    }

    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    format.formatType = if (checked) FormatType.CHECKLIST_CHECKED else FormatType.CHECKLIST_UNCHECKED
    formats[position] = format
    adapter.updateItem(format, position)

    trueFormats[truePosition] = format

    note!!.description = FormatBuilder().getSmarterDescription(sectionPreservingSort(trueFormats))
    setNote()
    saveNoteIfNeeded()
  }

  fun openMoreOptions() {
    NoteOptionsBottomSheet.openSheet(this@ViewAdvancedNoteActivity, note!!)
  }

  fun openEditor() {
    startActivity(NoteIntentRouterActivity.edit(this, note!!))
  }

  protected open fun notifyToolbarColor() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val noteColor = currentNote.adjustedColor()
    when {
      !sUIUseNoteColorAsBackground -> {
        colorConfig.backgroundColor = appTheme.get(ThemeColorType.BACKGROUND)
        colorConfig.toolbarIconColor = appTheme.get(ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = colorConfig.backgroundColor
        colorConfig.toolbarBackgroundColor = appTheme.get(ThemeColorType.TOOLBAR_BACKGROUND)
      }
      ColorUtil.isLightColored(noteColor) -> {
        colorConfig.backgroundColor = noteColor
        colorConfig.toolbarIconColor = appTheme.get(this, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = darkOrDarkerColor(noteColor)
        colorConfig.toolbarBackgroundColor = colorConfig.statusBarColor
      }
      else -> {
        colorConfig.backgroundColor = noteColor
        colorConfig.toolbarIconColor = appTheme.get(this, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = darkOrDarkerColor(noteColor)
        colorConfig.toolbarBackgroundColor = colorConfig.statusBarColor
      }
    }

    setSystemTheme(colorConfig.statusBarColor)
    views.root.setBackgroundColor(colorConfig.backgroundColor)
    views.formatsRecyclerView.setBackgroundColor(colorConfig.backgroundColor)

    resetBundle()
    adapter.notifyDataSetChanged()

    setBottomToolbar()
    setTopToolbar()
  }

  protected open fun setBottomToolbar() {
    views.lithoBottomToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    views.lithoBottomToolbar.addView(
      LithoView.create(
        componentContext,
        NoteViewBottomBar.create(componentContext)
          .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
          .build()))
  }

  protected open fun setTopToolbar() {
    views.lithoTopToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    views.lithoTopToolbar.addView(
      LithoView.create(
        componentContext,
        NoteViewTopBar.create(componentContext).build()))
  }

  protected open fun setNoteColor(color: Int) {

  }

  protected fun saveNoteIfNeeded() {
    if (note!!.getFormats().isEmpty() && note!!.isUnsaved()) {
      return
    }
    note!!.updateTimestamp = System.currentTimeMillis()
    note!!.save(this)
  }

  fun notifyNoteChange() {
    notifyToolbarColor()
  }

  protected fun getFormatIndex(format: Format): Int = getFormatIndex(format.uid)

  protected fun getFormatIndex(formatUid: Int): Int {
    var position = 0
    for (fmt in formats) {
      if (fmt.uid == formatUid) {
        return position
      }
      position++
    }
    return -1
  }

  override fun notifyThemeChange() {
    notifyToolbarColor()
  }

  override fun onSaveInstanceState(savedInstanceState: Bundle) {
    super.onSaveInstanceState(savedInstanceState)
    savedInstanceState.putInt(INTENT_KEY_NOTE_ID, if (note == null || note!!.uid == null) 0 else note!!.uid)
  }

  fun note() = note!!

  companion object {
    const val HANDLER_UPDATE_TIME = 1000

    fun getIntent(context: Context, note: Note): Intent {
      val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return intent
    }

    fun getIntentWithStack(context: Context, note: Note): PendingIntent {
      val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return getPendingIntentWithStack(context, 5000 + note.uid, intent)
    }
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */

  override fun updateNote(note: Note) {
    note.save(this)
    notifyNoteChange()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    note.softDelete(this)
    finish()
  }

  override fun notifyTagsChanged(note: Note) {
    setNote()
  }

  override fun getSelectMode(note: Note): String {
    return NoteState.DEFAULT.name
  }

  override fun notifyResetOrDismiss() {
    finish()
  }

  override fun lockedContentIsHidden() = false

  /**
   * End : INoteOptionSheetActivity
   */

  /**
   * Start : IFormatRecyclerView Functions
   */

  override fun context(): Context {
    return this
  }

  override fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
    return getFormatControllerItems()
  }

  override fun deleteFormat(format: Format) {
    // do nothing
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    // do nothing
  }

  /**
   * End : IFormatRecyclerView
   */

}
