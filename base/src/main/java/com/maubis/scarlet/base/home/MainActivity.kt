package com.maubis.scarlet.base.home

import android.os.Bundle
import android.view.View
import android.widget.GridLayout.VERTICAL
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.backup.NoteExporter
import com.maubis.scarlet.base.backup.PermissionUtils
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.specs.ToolbarColorConfig
import com.maubis.scarlet.base.common.ui.SecuredActivity
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.sThemeIsAutomatic
import com.maubis.scarlet.base.common.ui.setThemeFromSystem
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.databinding.ActivityMainBinding
import com.maubis.scarlet.base.home.recycler.NoNotesRecyclerItem
import com.maubis.scarlet.base.note.actions.INoteActionsSheetActivity
import com.maubis.scarlet.base.note.folder.FolderRecyclerItem
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.settings.STORE_KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.SettingsBottomSheet
import com.maubis.scarlet.base.settings.sNoteItemLineCount
import com.maubis.scarlet.base.settings.sUIUseGridView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : SecuredActivity(), INoteActionsSheetActivity {
  companion object {
    private const val IS_IN_SEARCH_MODE: String = "IS_IN_SEARCH_MODE"
    private const val NAVIGATION_MODE: String = "NAVIGATION_MODE"
    private const val SEARCH_TEXT: String = "SEARCH_TEXT"
    private const val CURRENT_FOLDER_UUID: String = "CURRENT_FOLDER_UUID"
    private const val TAGS_UUIDS: String = "TAGS_UUIDS"
    private const val SEARCH_COLORS: String = "SEARCH_COLORS"
  }

  private lateinit var views: ActivityMainBinding
  private lateinit var adapter: NoteAppAdapter
  private lateinit var snackbar: NoteDeletionSnackbar
  private lateinit var tagAndColorPicker: TagsAndColorPickerViewHolder

  val state: SearchState = SearchState(mode = HomeNavigationMode.DEFAULT)
  var isInSearchMode: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityMainBinding.inflate(layoutInflater)
    setContentView(views.root)

    if (sThemeIsAutomatic) {
      setThemeFromSystem(this)
    }
    appTheme.notifyChange(this)

    setupMainToolbar()
    setupRecyclerView()
    setListeners()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putBoolean(IS_IN_SEARCH_MODE, isInSearchMode)
    outState.putString(SEARCH_TEXT, state.text)
    outState.putIntegerArrayList(SEARCH_COLORS, ArrayList(state.colors))
    outState.putInt(NAVIGATION_MODE, state.mode.ordinal)
    outState.putSerializable(CURRENT_FOLDER_UUID, state.currentFolder?.uuid)
    outState.putStringArrayList(TAGS_UUIDS, ArrayList(state.tags.map { it.uuid.toString() }))
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)

    isInSearchMode = savedInstanceState.getBoolean(IS_IN_SEARCH_MODE)
    state.text = savedInstanceState.getString(SEARCH_TEXT, "")
    state.colors = savedInstanceState.getIntegerArrayList(SEARCH_COLORS) ?: ArrayList()
    state.mode = HomeNavigationMode.values()[savedInstanceState.getInt(NAVIGATION_MODE)]
    savedInstanceState.getSerializable(CURRENT_FOLDER_UUID)?.let {
      state.currentFolder = data.folders.getByUUID(it as UUID)
    }
    savedInstanceState.getStringArrayList(TAGS_UUIDS)?.forEach {
      data.tags.getByUUID(UUID.fromString(it))?.let { state.tags.add(it) }
    }
  }

  private fun setListeners() {
    snackbar = NoteDeletionSnackbar(views.bottomSnackbar) { refreshItems() }
    views.searchToolbar.backButton.setOnClickListener { onBackPressed() }
    views.searchToolbar.closeIcon.setOnClickListener { onBackPressed() }
    tagAndColorPicker = TagsAndColorPickerViewHolder(
      this,
      views.searchToolbar.tagsFlexBox,
      onTagClick = { tag ->
        if (state.isFilteringByTag(tag)) {
          state.tags.removeAll { it.uuid == tag.uuid }
          startSearch(views.searchToolbar.textField.text.toString())
          tagAndColorPicker.notifyChanged()
        } else {
          openTag(tag)
          tagAndColorPicker.notifyChanged()
        }
      },
      onColorClick = { color ->
        if (state.colors.contains(color)) {
          state.colors.remove(color)
        } else {
          state.colors.add(color)
        }
        tagAndColorPicker.notifyChanged()
        startSearch(views.searchToolbar.textField.text.toString())
      })
  }

  private fun setupMainToolbar() {
    views.mainToolbar.searchIcon.setOnClickListener {
      enterSearchMode()
    }

    views.mainToolbar.settingsIcon.setOnClickListener {
      SettingsBottomSheet.openSheet(this)
    }

    val titleColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
    val hintColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)
    views.mainToolbar.title.setTextColor(titleColor)
    views.mainToolbar.title.typeface = ScarletApp.appTypeface.heading()
    views.mainToolbar.searchIcon.setColorFilter(titleColor)
    views.mainToolbar.settingsIcon.setColorFilter(titleColor)
    views.searchToolbar.textField.setTextColor(titleColor)
    views.searchToolbar.textField.setHintTextColor(hintColor)
    views.searchToolbar.separator.setBackgroundColor(hintColor)
    views.searchToolbar.backButton.setColorFilter(titleColor)
    views.searchToolbar.closeIcon.setColorFilter(titleColor)
  }

  private fun setupRecyclerView() {
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    val adapterExtra = Bundle().apply {
      putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)
    }
    adapter = NoteAppAdapter(this, sUIUseGridView, isTablet)
    adapter.setExtra(adapterExtra)
    views.recyclerView.layoutManager = getLayoutManager(sUIUseGridView, isTablet)
    views.recyclerView.adapter = adapter
    views.recyclerView.setHasFixedSize(true)
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    return when {
      isTabletView || isStaggeredView -> StaggeredGridLayoutManager(2, VERTICAL)
      else -> LinearLayoutManager(this)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    // This must be done after onRestoreInstanceState(), otherwise automatic TextView state restoring
    // will trigger a search which can cause wrong items to appear
    views.searchToolbar.textField.doOnTextChanged { text, _, _, _ -> startSearch(text.toString()) }
  }

  override fun onResume() {
    super.onResume()
    refreshItems()
    notifyFolderChange()

    if (isInSearchMode)
      enterSearchMode()
  }

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    resetAndLoadData()
  }

  fun onModeChange(mode: HomeNavigationMode) {
    state.mode = mode
    state.currentFolder = null
    notifyFolderChange()
    refreshItems()
    updateToolbars()
  }

  private fun updateToolbars() {
    views.mainToolbar.title.text = getString(state.mode.toolbarTitleResourceId)
    updateMainToolbarLeftIcon()
    setBottomToolbar()
  }

  private fun updateMainToolbarLeftIcon() {
    views.mainToolbar.leftIcon.setImageDrawable(getDrawable(state.mode.toolbarIconResourceId))
    if (state.mode != HomeNavigationMode.DEFAULT) {
      val iconColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
      views.mainToolbar.leftIcon.setColorFilter(iconColor)
    }
    else
      views.mainToolbar.leftIcon.clearColorFilter()
  }

  fun onFolderChange(folder: Folder?) {
    state.currentFolder = folder
    refreshItems()
    notifyFolderChange()
  }

  fun notifyFolderChange() {
    val componentContext = ComponentContext(this)
    views.folderToolbar.removeAllViews()
    setBottomToolbar()

    val currentFolder = state.currentFolder
    if (currentFolder != null) {
      views.folderToolbar.addView(LithoView.create(componentContext,
          MainActivityFolderBottomBar.create(componentContext)
              .folder(currentFolder)
              .build()))
    }
  }

  private fun handleNewItems(notes: List<RecyclerItem>) {
    adapter.clearItems()
    if (notes.isEmpty()) {
      adapter.addItem(NoNotesRecyclerItem())
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
  }

  private fun unifiedSearchSynchronous(): List<RecyclerItem> {
    val allItems = mutableListOf<RecyclerItem>()
    if (state.currentFolder != null) {
      val allNotes = unifiedSearchSynchronous(state)
      allItems.addAll(allNotes.map { NoteRecyclerItem(this, it) })
      return allItems
    }

    val allNotes = unifiedSearchWithoutFolder(state)
    val directAcceptableFolders = filterDirectlyValidFolders(state)
    allItems.addAll(
        data.folders.getAll()
            .map {
                val isDirectFolder = directAcceptableFolders.contains(it)
                val notesCount = filterFolder(allNotes, it).size
                if (state.hasFilter() && notesCount == 0 && !isDirectFolder) {
                  return@map null
                }

                FolderRecyclerItem(
                  context = this,
                  folder = it,
                  click = { onFolderChange(it) },
                  longClick = {
                    CreateOrEditFolderBottomSheet.openSheet(this, it, { _, _ -> refreshItems() })
                  },
                  selected = state.currentFolder?.uuid == it.uuid,
                  contents = notesCount)
            }
            .filterNotNull()
    )
    allItems.addAll(filterOutFolders(allNotes).map { NoteRecyclerItem(this, it) })
    return allItems
  }

  fun refreshItems() {
    lifecycleScope.launch {
      val items = withContext(Dispatchers.IO) { unifiedSearchSynchronous() }
      handleNewItems(items)
    }
  }

  private fun openTag(tag: Tag) {
    state.mode = if (state.mode == HomeNavigationMode.LOCKED) HomeNavigationMode.DEFAULT else state.mode
    state.tags.add(tag)
    refreshItems()
    updateToolbars()
  }

  fun resetAndLoadData() {
    state.clear()
    loadData()
  }

  fun loadData() = onModeChange(state.mode)

  private fun enterSearchMode() {
    isInSearchMode = true
    views.searchToolbar.textField.setText(state.text)
    views.mainToolbar.root.visibility = View.GONE
    views.searchToolbar.root.visibility = View.VISIBLE
    views.searchToolbar.textField.requestFocus()
    tryOpeningTheKeyboard(views.searchToolbar.textField)
    lifecycleScope.launch {
      withContext(Dispatchers.IO) { tagAndColorPicker.reset() }
      tagAndColorPicker.notifyChanged()
    }
  }

  private fun quitSearchMode() {
    isInSearchMode = false
    views.searchToolbar.textField.setText("")
    tryClosingTheKeyboard()
    views.mainToolbar.root.visibility = View.VISIBLE
    views.searchToolbar.root.visibility = View.GONE
    state.clearSearchBar()
    refreshItems()
  }

  private fun startSearch(keyword: String) {
    state.text = keyword
    refreshItems()
  }

  override fun onBackPressed() {
    when {
      isInSearchMode && views.searchToolbar.textField.text.toString().isBlank() -> quitSearchMode()
      isInSearchMode -> views.searchToolbar.textField.setText("")
      state.currentFolder != null -> onFolderChange(null)
      state.hasFilter() -> {
        state.clear()
        onModeChange(HomeNavigationMode.DEFAULT)
      }
      else -> super.onBackPressed()
    }
  }

  override fun onStop() {
    super.onStop()
    if (isFinishing && PermissionUtils.getStoragePermissionManager(this).hasAllPermissions()) {
      NoteExporter.tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    updateStatusBarTheme()
    views.containerLayoutMain.setBackgroundColor(getThemeColor())
    setBottomToolbar()
  }

  private fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    views.lithoBottomToolbar.removeAllViews()
    views.lithoBottomToolbar.addView(
      LithoView.create(
        componentContext,
        MainActivityBottomBar.create(componentContext)
          .colorConfig(ToolbarColorConfig())
          .isInsideFolder(state.currentFolder != null)
          .isInTrash(state.mode == HomeNavigationMode.TRASH)
          .build()))
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */
  override fun updateNote(note: Note) {
    note.save(this)
    refreshItems()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
    refreshItems()
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    snackbar.softUndo(this, note)
    note.moveToTrashOrDelete(this)
    refreshItems()
  }

  override fun notifyTagsChanged(note: Note) {
    refreshItems()
  }

  override fun getSelectMode(note: Note): String {
    return state.mode.name
  }

  override fun notifyResetOrDismiss() {
    refreshItems()
  }

  override fun lockedContentIsHidden() = true

  /**
   * End : INoteOptionSheetActivity
   */
}
