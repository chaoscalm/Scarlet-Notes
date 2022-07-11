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
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.resetIconTint
import com.maubis.scarlet.base.common.utils.setIconTint
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.databinding.ActivityMainBinding
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.home.recycler.NoNotesRecyclerItem
import com.maubis.scarlet.base.note.actions.INoteActionsActivity
import com.maubis.scarlet.base.note.folder.FolderRecyclerItem
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.settings.SettingsBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : SecuredActivity(), INoteActionsActivity {
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
  private lateinit var tagAndColorPicker: TagsAndColorPicker

  val state: SearchState = SearchState(mode = HomeNavigationMode.DEFAULT)
  var isInSearchMode: Boolean = false
    private set
  val isInTrash: Boolean get() = state.mode == HomeNavigationMode.TRASH

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityMainBinding.inflate(layoutInflater)
    setContentView(views.root)

    setupMainToolbar()
    setupRecyclerView()
    setListeners()
    applyTheming()
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
    state.mode = HomeNavigationMode.values()[savedInstanceState.getInt(NAVIGATION_MODE)]
    state.colors.addAll(savedInstanceState.getIntegerArrayList(SEARCH_COLORS) ?: emptyList())
    savedInstanceState.getSerializable(CURRENT_FOLDER_UUID)?.let {
      state.currentFolder = data.folders.getByUUID(it as UUID)
    }
    savedInstanceState.getStringArrayList(TAGS_UUIDS)?.forEach {
      data.tags.getByUUID(UUID.fromString(it))?.let { state.tags.add(it) }
    }
  }

  private fun setListeners() {
    snackbar = NoteDeletionSnackbar(views.bottomSnackbar) { refreshList() }
    views.searchToolbar.closeIcon.setOnClickListener { onBackPressed() }
    tagAndColorPicker = TagsAndColorPicker(
      this,
      views.searchToolbar.tagsFlexBox,
      onTagClick = { tag ->
        if (state.isFilteringByTag(tag)) {
          state.tags.removeAll { it.uuid == tag.uuid }
        } else {
          state.tags.add(tag)
        }
        tagAndColorPicker.refreshUI()
        startSearch(views.searchToolbar.textField.text.toString())
      },
      onColorClick = { color ->
        if (state.colors.contains(color)) {
          state.colors.remove(color)
        } else {
          state.colors.add(color)
        }
        tagAndColorPicker.refreshUI()
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

    val titleColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val hintColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)
    val iconColor = appTheme.getColor(ThemeColor.ICON)
    views.mainToolbar.title.setTextColor(titleColor)
    views.mainToolbar.title.typeface = ScarletApp.appTypeface.heading()
    views.mainToolbar.searchIcon.setIconTint(iconColor)
    views.mainToolbar.settingsIcon.setIconTint(iconColor)
    views.searchToolbar.textField.setTextColor(titleColor)
    views.searchToolbar.textField.setHintTextColor(hintColor)
    views.searchToolbar.separator.setBackgroundColor(hintColor)
    views.searchToolbar.closeIcon.setIconTint(iconColor)
  }

  private fun setupRecyclerView() {
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    adapter = NoteAppAdapter(this, ScarletApp.prefs.displayNotesListAsGrid, isTablet)
    views.recyclerView.layoutManager = getLayoutManager(ScarletApp.prefs.displayNotesListAsGrid, isTablet)
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
    refreshList()

    if (isInSearchMode)
      enterSearchMode()
  }

  override fun onStop() {
    super.onStop()
    if (isFinishing && PermissionUtils.hasExternalStorageAccess(this)) {
      NoteExporter.tryAutoExport()
    }
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

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    resetAndLoadData()
  }

  fun resetAndLoadData() {
    state.clear()
    loadData()
  }

  fun loadData() = onModeChange(state.mode)

  fun onModeChange(mode: HomeNavigationMode) {
    state.mode = mode
    state.currentFolder = null
    refreshList()
    updateMainToolbar()
  }

  fun onFolderChange(folder: Folder?) {
    state.currentFolder = folder
    refreshList()
  }

  fun refreshList() {
    lifecycleScope.launch {
      val items = withContext(Dispatchers.IO) { computeItemsToBeShown() }
      displayItems(items)
    }
    updateBottomToolbar()
    updateFolderToolbar()
  }

  private fun computeItemsToBeShown(): List<RecyclerItem> {
    if (state.currentFolder != null) {
      return findMatchingNotes(state)
        .map { NoteRecyclerItem(this, it) }
    }

    val allMatchingNotes = findMatchingNotesIgnoringFolder(state)
    val matchingFolders = findMatchingFolders(state)
    val allItems = mutableListOf<RecyclerItem>()
    allItems.addAll(
      data.folders.getAll()
        .filter { folder ->
          val isFolderNotEmpty = allMatchingNotes.any { it.folder == folder.uuid }
          when {
            isInTrash -> isFolderNotEmpty && matchingFolders.contains(folder)
            state.hasFilter() -> isFolderNotEmpty || matchingFolders.contains(folder)
            else -> true
          }
        }
        .map { folder ->
          FolderRecyclerItem(
            context = this,
            folder = folder,
            notesCount = allMatchingNotes.count { it.folder == folder.uuid },
            click = { onFolderChange(folder) },
            longClick = {
              CreateOrEditFolderBottomSheet.openSheet(this, folder) { refreshList() }
            }
          )
        }
    )
    allItems.addAll(excludeNotesInFolders(allMatchingNotes).map { NoteRecyclerItem(this, it) })
    return allItems
  }

  private fun displayItems(notes: List<RecyclerItem>) {
    adapter.clearItems()
    if (notes.isEmpty()) {
      adapter.addItem(NoNotesRecyclerItem())
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
  }

  private fun enterSearchMode() {
    isInSearchMode = true
    views.searchToolbar.textField.setText(state.text)
    views.mainToolbar.root.visibility = View.GONE
    views.searchToolbar.root.visibility = View.VISIBLE
    views.searchToolbar.textField.requestFocus()
    tryOpeningTheKeyboard(views.searchToolbar.textField)
    lifecycleScope.launch {
      withContext(Dispatchers.IO) { tagAndColorPicker.loadData() }
      tagAndColorPicker.refreshUI()
    }
  }

  private fun quitSearchMode() {
    isInSearchMode = false
    views.searchToolbar.textField.setText("")
    tryClosingTheKeyboard()
    views.mainToolbar.root.visibility = View.VISIBLE
    views.searchToolbar.root.visibility = View.GONE
    state.clearSearchBar()
    refreshList()
  }

  private fun startSearch(keyword: String) {
    state.text = keyword
    refreshList()
  }

  override fun applyTheming() {
    updateStatusBarTheme()
    views.containerLayoutMain.setBackgroundColor(getThemeColor())
    updateBottomToolbar()
  }

  private fun updateMainToolbar() {
    views.mainToolbar.title.text = getString(state.mode.toolbarTitleResourceId)
    views.mainToolbar.leftIcon.setImageDrawable(getDrawable(state.mode.toolbarIconResourceId))
    if (state.mode != HomeNavigationMode.DEFAULT) {
      val iconColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
      views.mainToolbar.leftIcon.setIconTint(iconColor)
    } else
      views.mainToolbar.leftIcon.resetIconTint()
  }

  private fun updateBottomToolbar() {
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

  private fun updateFolderToolbar() {
    val componentContext = ComponentContext(this)
    views.folderToolbar.removeAllViews()
    val currentFolder = state.currentFolder
    if (currentFolder != null) {
      views.folderToolbar.addView(LithoView.create(componentContext,
        MainActivityFolderBottomBar.create(componentContext)
          .folder(currentFolder)
          .build()))
    }
  }

  fun launchNewNoteEditor() {
    val intent = EditNoteActivity.makeNewNoteIntent(
      this,
      state.currentFolder?.uuid,
      newNoteStateForNavigationMode(state.mode),
      locked = state.mode == HomeNavigationMode.LOCKED)
    startActivity(intent)
  }

  fun launchNewChecklistNoteEditor() {
    val intent = EditNoteActivity.makeNewChecklistNoteIntent(
      this,
      state.currentFolder?.uuid,
      newNoteStateForNavigationMode(state.mode),
      locked = state.mode == HomeNavigationMode.LOCKED)
    startActivity(intent)
  }

  private fun newNoteStateForNavigationMode(navigationMode: HomeNavigationMode): NoteState {
    return when (navigationMode) {
      HomeNavigationMode.DEFAULT, HomeNavigationMode.LOCKED -> NoteState.DEFAULT
      HomeNavigationMode.FAVOURITE -> NoteState.FAVOURITE
      HomeNavigationMode.ARCHIVED -> NoteState.ARCHIVED
      HomeNavigationMode.TRASH -> NoteState.TRASH
    }
  }

  override fun updateNote(note: Note) {
    note.save(this)
    refreshList()
  }

  override fun updateNoteState(note: Note, state: NoteState) {
    note.updateState(state, this)
    refreshList()
  }

  override fun moveNoteToTrashOrDelete(note: Note) {
    snackbar.softUndo(this, note)
    note.moveToTrashOrDelete(this)
    refreshList()
  }

  override fun notifyTagsChanged() {
    refreshList()
  }

  override fun notifyResetOrDismiss() {
    refreshList()
  }

  override fun lockedContentIsHidden() = true
}
