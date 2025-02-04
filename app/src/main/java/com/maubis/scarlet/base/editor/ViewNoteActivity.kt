package com.maubis.scarlet.base.editor

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.common.ui.SecuredActivity
import com.maubis.scarlet.base.common.ui.Theme
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.ColorUtil.darkerColor
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.ActivityAdvancedNoteBinding
import com.maubis.scarlet.base.editor.recycler.*
import com.maubis.scarlet.base.editor.specs.NoteViewBottomBar
import com.maubis.scarlet.base.note.actions.INoteActionsActivity
import com.maubis.scarlet.base.note.actions.NoteActionsBottomSheet
import com.maubis.scarlet.base.note.adjustedColor
import com.maubis.scarlet.base.note.getTagString
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

open class ViewNoteActivity : SecuredActivity(), INoteActionsActivity {

  var focusedFormat: Format? = null

  protected lateinit var note: Note
  protected lateinit var formats: MutableList<Format>

  protected lateinit var views: ActivityAdvancedNoteBinding
  protected lateinit var adapter: FormatAdapter

  protected val colorConfig = NoteViewColorConfig()

  protected open val isEditingMode: Boolean = false
  private val noteSaveDispatcher = Dispatchers.IO.limitedParallelism(1)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityAdvancedNoteBinding.inflate(layoutInflater)
    setContentView(views.root)

    note = loadNote(savedInstanceState)
    setupRecyclerView()
    displayNote()
    applyTheming()
  }

  private fun loadNote(savedInstanceState: Bundle?): Note {
    var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
    }
    return if (noteId != 0) {
      data.notes.getByID(noteId) ?: Note(ScarletApp.prefs.noteDefaultColor)
    } else {
      Note(ScarletApp.prefs.noteDefaultColor)
    }
  }

  private fun setupRecyclerView() {
    adapter = FormatAdapter(this)
    views.formatsRecyclerView.adapter = adapter
    views.formatsRecyclerView.layoutManager = LinearLayoutManager(this)
    views.formatsRecyclerView.setHasFixedSize(true)
    resetAdapterBundle()
  }

  override fun onResume() {
    super.onResume()

    onResumeAction()
    applyTheming()
  }

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

  private fun resetAdapterBundle() {
    val bundle = Bundle().apply {
      putBoolean(KEY_EDITABLE, isEditingMode)
      putInt(KEY_TEXT_SIZE, ScarletApp.prefs.editorTextSize)
      putInt(KEY_NOTE_COLOR, note.adjustedColor())
      putString(INTENT_KEY_NOTE_ID, note.uuid.toString())
    }
    adapter.setExtra(bundle)
  }

  protected open fun displayNote() {
    adapter.clearItems()

    formats = when (isEditingMode) {
      true -> note.contentAsFormats()
      false -> Formats.getEnhancedFormatsForNoteView(note.contentAsFormats())
    }.toMutableList()
    adapter.addItems(formats)

    if (!isEditingMode) {
      addTagsIndicatorIfNeeded()
    }
  }

  private fun addTagsIndicatorIfNeeded() {
    val tagLabel = note.getTagString()
    if (tagLabel.isEmpty()) {
      return
    }

    val format = Format(FormatType.TAG, tagLabel)
    adapter.addItem(format)
  }

  open fun setFormat(format: Format) {
    // do nothing
  }

  open fun createOrChangeToNextFormat(format: Format) {
    // do nothing
  }

  open fun setFormatChecked(format: Format, checked: Boolean) {
    val actualFormats = note.contentAsFormats().toMutableList()
    val position = actualFormats.indexOfFirst { it.uid == format.uid }
    if (position == -1) {
      return
    }

    format.type = if (checked) FormatType.CHECKLIST_CHECKED else FormatType.CHECKLIST_UNCHECKED
    actualFormats[position] = format

    note.content = Formats.getEnhancedNoteContent(Formats.sortChecklistsIfAllowed(actualFormats))
    saveNote()
    displayNote()
  }

  protected fun saveNote() {
    note.updateTimestamp = System.currentTimeMillis()
    lifecycleScope.launch(noteSaveDispatcher) { note.save(this@ViewNoteActivity) }
  }

  fun openMoreOptions() {
    NoteActionsBottomSheet.openSheet(this@ViewNoteActivity, note)
  }

  fun openEditor() {
    startActivity(EditNoteActivity.makeEditNoteIntent(this, note))
  }

  protected fun notifyToolbarColor() {
    val noteColor = note.adjustedColor()
    when {
      ScarletApp.prefs.useNoteColorAsBackground -> {
        colorConfig.backgroundColor = noteColor
        colorConfig.toolbarIconColor = appTheme.getColor(this, Theme.DARK, ThemeColor.ICON)
        colorConfig.statusBarColor = noteColor
        colorConfig.toolbarBackgroundColor = darkerColor(noteColor)
      }
      else -> {
        colorConfig.backgroundColor = appTheme.getColor(ThemeColor.BACKGROUND)
        colorConfig.toolbarIconColor = appTheme.getColor(ThemeColor.ICON)
        colorConfig.statusBarColor = appTheme.getColor(ThemeColor.STATUS_BAR)
        colorConfig.toolbarBackgroundColor = appTheme.getColor(ThemeColor.TOOLBAR_BACKGROUND)
      }
    }

    updateStatusBarTheme(colorConfig.statusBarColor)
    views.root.setBackgroundColor(colorConfig.backgroundColor)
    views.formatsRecyclerView.setBackgroundColor(colorConfig.backgroundColor)

    resetAdapterBundle()
    adapter.notifyDataSetChanged()

    setBottomToolbar()
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

  open fun startFormatDrag(viewHolder: RecyclerView.ViewHolder) {}

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

  override fun applyTheming() {
    notifyToolbarColor()
  }

  override fun onSaveInstanceState(savedInstanceState: Bundle) {
    super.onSaveInstanceState(savedInstanceState)
    savedInstanceState.putInt(INTENT_KEY_NOTE_ID, note.uid)
  }

  fun note() = note

  companion object {
    fun makeIntent(context: Context, note: Note): Intent {
      val intent = Intent(context, ViewNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return intent
    }

    fun makePendingIntentWithStack(context: Context, note: Note): PendingIntent {
      val intent = Intent(context, ViewNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return getPendingIntentWithStack(context, 5000 + note.uid, intent)
    }

    fun makePreferenceAwareIntent(context: Context, note: Note): Intent {
      if (ScarletApp.prefs.skipNoteViewer) {
        return EditNoteActivity.makeEditNoteIntent(context, note)
      }

      return Intent(context, ViewNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
    }
  }

  override fun updateNote(note: Note) {
    note.save(this)
    notifyNoteChange()
  }

  override fun updateNoteState(note: Note, state: NoteState) {
    note.updateState(state, this)
  }

  override fun moveNoteToTrashOrDelete(note: Note) {
    note.moveToTrashOrDelete(this)
    finish()
  }

  override fun notifyTagsChanged() {
    displayNote()
  }

  override fun notifyResetOrDismiss() {
    finish()
  }

  override fun lockedContentIsHidden() = false

  open fun deleteFormat(format: Format) {
    // do nothing
  }

  open fun onFormatMoved(fromPosition: Int, toPosition: Int) {
    // do nothing
  }

  open fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
    return listOf(
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.TAG.ordinal)
        .layoutFile(R.layout.item_format_tag)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.TEXT.ordinal)
        .layoutFile(R.layout.item_format_text)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.HEADING.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.SUB_HEADING.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatTextViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.HEADING_3.ordinal)
        .layoutFile(R.layout.item_format_heading)
        .holderClass(FormatTextViewHolder::class.java)
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
        .viewType(FormatType.BULLET_1.ordinal)
        .layoutFile(R.layout.item_format_bullet)
        .holderClass(FormatBulletViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.BULLET_2.ordinal)
        .layoutFile(R.layout.item_format_nested_bullet)
        .holderClass(FormatBulletViewHolder::class.java)
        .build(),
      MultiRecyclerViewControllerItem.Builder<Format>()
        .viewType(FormatType.BULLET_3.ordinal)
        .layoutFile(R.layout.item_format_nested_bullet)
        .holderClass(FormatBulletViewHolder::class.java)
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
}
