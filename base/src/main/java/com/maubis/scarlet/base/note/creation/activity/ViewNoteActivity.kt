package com.maubis.scarlet.base.note.creation.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
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
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.ActivityAdvancedNoteBinding
import com.maubis.scarlet.base.note.actions.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.actions.NoteOptionsBottomSheet
import com.maubis.scarlet.base.note.adjustedColor
import com.maubis.scarlet.base.note.creation.specs.NoteViewBottomBar
import com.maubis.scarlet.base.note.creation.specs.NoteViewTopBar
import com.maubis.scarlet.base.note.formats.FormatAdapter
import com.maubis.scarlet.base.note.formats.IFormatRecyclerViewActivity
import com.maubis.scarlet.base.note.formats.getFormatControllerItems
import com.maubis.scarlet.base.note.formats.recycler.KEY_EDITABLE
import com.maubis.scarlet.base.note.formats.recycler.KEY_NOTE_COLOR
import com.maubis.scarlet.base.note.getSmartFormats
import com.maubis.scarlet.base.note.getTagString
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.settings.STORE_KEY_TEXT_SIZE
import com.maubis.scarlet.base.settings.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sEditorTextSize
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.settings.sUIUseNoteColorAsBackground
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.ui.KEY_NIGHT_THEME
import com.maubis.scarlet.base.support.ui.SecuredActivity
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.ColorUtil
import com.maubis.scarlet.base.support.utils.ColorUtil.darkOrDarkerColor
import com.maubis.scarlet.base.widget.getPendingIntentWithStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val INTENT_KEY_NOTE_ID = "NOTE_ID"

data class NoteViewColorConfig(
  var backgroundColor: Int = Color.BLACK,
  var toolbarBackgroundColor: Int = Color.BLACK,
  var toolbarIconColor: Int = Color.BLACK,
  var statusBarColor: Int = Color.BLACK)

open class ViewAdvancedNoteActivity : SecuredActivity(), INoteOptionSheetActivity, IFormatRecyclerViewActivity {

  var focusedFormat: Format? = null

  protected lateinit var note: Note
  protected lateinit var formats: MutableList<Format>

  protected lateinit var views: ActivityAdvancedNoteBinding
  protected lateinit var adapter: FormatAdapter

  val colorConfig = NoteViewColorConfig()
  var lastKnownNoteColor = 0

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityAdvancedNoteBinding.inflate(layoutInflater)
    setContentView(views.root)
    setRecyclerView()

    var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
    }
    note = if (noteId != 0) {
      data.notes.getByID(noteId) ?: NoteBuilder().emptyNote(sNoteDefaultColor)
    } else {
      NoteBuilder().emptyNote(sNoteDefaultColor)
    }
    resetBundle()
    displayNote()
    notifyThemeChange()
    onCreationFinished()
  }

  override fun onResume() {
    super.onResume()

    onResumeAction()
    notifyThemeChange()
  }

  protected open fun onCreationFinished() {}

  protected open fun onResumeAction() {
    lifecycleScope.launch(Dispatchers.IO) {
      when (val reloadedNote = data.notes.getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))) {
          null -> finish()
          else -> {
            note = reloadedNote
            withContext(Dispatchers.Main) { displayNote() }
          }
      }
    }
  }

  private fun resetBundle() {
    val bundle = Bundle()
    bundle.putBoolean(KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, appPreferences.getBoolean(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, appTheme.isNightTheme())
    bundle.putInt(STORE_KEY_TEXT_SIZE, sEditorTextSize)
    bundle.putInt(KEY_NOTE_COLOR, note.adjustedColor())
    bundle.putString(INTENT_KEY_NOTE_ID, note.uuid.toString())
    adapter.setExtra(bundle)
  }

  protected open fun displayNote() {
    setNoteColor(note.color)
    adapter.clearItems()

    formats = when (editModeValue) {
      true -> note.getFormats()
      false -> note.getSmartFormats()
    }.toMutableList()
    adapter.addItems(formats)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }
  }

  private fun maybeAddTags() {
    val tagLabel = note.getTagString()
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
    val trueFormats = note.getFormats().toMutableList()
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

    note.content = FormatBuilder().getSmarterContent(sectionPreservingSort(trueFormats))
    displayNote()
    saveNoteIfNeeded()
  }

  fun openMoreOptions() {
    NoteOptionsBottomSheet.openSheet(this@ViewAdvancedNoteActivity, note)
  }

  fun openEditor() {
    startActivity(NoteIntentRouterActivity.edit(this, note))
  }

  protected open fun notifyToolbarColor() {
    val noteColor = note.adjustedColor()
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
    if (note.getFormats().isEmpty() && note.isNotPersisted()) {
      return
    }
    note.updateTimestamp = System.currentTimeMillis()
    note.save(this)
  }

  private fun notifyNoteChange() {
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
    savedInstanceState.putInt(INTENT_KEY_NOTE_ID, note.uid)
  }

  fun note() = note

  companion object {
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
    displayNote()
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
