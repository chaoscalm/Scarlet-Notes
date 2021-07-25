package com.maubis.scarlet.base.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.GridLayout.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.backup.support.NoteExporter
import com.maubis.scarlet.base.backup.support.PermissionUtils
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.databinding.ActivityMainBinding
import com.maubis.scarlet.base.home.*
import com.maubis.scarlet.base.home.recycler.*
import com.maubis.scarlet.base.note.actions.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.folder.FolderRecyclerItem
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.softDelete
import com.maubis.scarlet.base.note.tag.TagsAndColorPickerViewHolder
import com.maubis.scarlet.base.settings.STORE_KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.maubis.scarlet.base.settings.sNoteItemLineCount
import com.maubis.scarlet.base.settings.sUIUseGridView
import com.maubis.scarlet.base.support.database.HouseKeeperJob
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.ui.*
import kotlinx.coroutines.*

class MainActivity : SecuredActivity(), INoteOptionSheetActivity {
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
    outState.putString(CURRENT_FOLDER_UUID, state.currentFolder?.uuid)
    outState.putStringArrayList(TAGS_UUIDS, ArrayList(state.tags.map { it.uuid }))
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)

    if (savedInstanceState != null) {
      isInSearchMode = savedInstanceState.getBoolean(IS_IN_SEARCH_MODE)
      state.text = savedInstanceState.getString(SEARCH_TEXT, "")
      state.colors = savedInstanceState.getIntegerArrayList(SEARCH_COLORS) ?: ArrayList()
      state.mode = HomeNavigationMode.values()[savedInstanceState.getInt(NAVIGATION_MODE)]
      savedInstanceState.getString(CURRENT_FOLDER_UUID)?.let {
        state.currentFolder = data.folders.getByUUID(it)
      }
      savedInstanceState.getStringArrayList(TAGS_UUIDS)?.forEach {
        data.tags.getByUUID(it)?.let { state.tags.add(it) }
      }
    }
  }

  private fun setListeners() {
    snackbar = NoteDeletionSnackbar(views.bottomSnackbar) { refreshItems() }
    views.searchToolbar.backButton.setOnClickListener { onBackPressed() }
    views.searchToolbar.closeIcon.setOnClickListener { onBackPressed() }
    views.searchToolbar.textField.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        startSearch(charSequence.toString())
      }

      override fun afterTextChanged(editable: Editable) {}
    })
    tagAndColorPicker = TagsAndColorPickerViewHolder(
      this,
      views.searchToolbar.tagsFlexBox,
      { tag ->
        val isTagSelected = state.tags.filter { it.uuid == tag.uuid }.isNotEmpty()
        when (isTagSelected) {
          true -> {
            state.tags.removeAll { it.uuid == tag.uuid }
            startSearch(views.searchToolbar.textField.text.toString())
            tagAndColorPicker.notifyChanged()
          }
          false -> {
            openTag(tag)
            tagAndColorPicker.notifyChanged()
          }
        }
      },
      { color ->
        when (state.colors.contains(color)) {
          true -> state.colors.remove(color)
          false -> state.colors.add(color)
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
      SettingsOptionsBottomSheet.openSheet(this)
    }

    val titleColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
    views.mainToolbar.title.setTextColor(titleColor)
    views.mainToolbar.title.typeface = ScarletApp.appTypeface.heading()

    val toolbarIconColor = appTheme.get(ThemeColorType.SECONDARY_TEXT)
    views.mainToolbar.searchIcon.setColorFilter(toolbarIconColor)
    views.mainToolbar.settingsIcon.setColorFilter(toolbarIconColor)
  }

  private fun setupRecyclerView() {
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = appPreferences.getBoolean(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = appPreferences.getBoolean(KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle().apply {
      putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
      putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)
    }

    adapter = NoteAppAdapter(this, sUIUseGridView, isTablet)
    adapter.setExtra(adapterExtra)
    views.recyclerView.layoutManager = getLayoutManager(sUIUseGridView, isTablet)
    views.recyclerView.adapter = adapter
    views.recyclerView.setHasFixedSize(false)
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    return when {
      isTabletView || isStaggeredView -> StaggeredGridLayoutManager(2, VERTICAL)
      else -> LinearLayoutManager(this)
    }
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
    if (!isInSearchMode) {
      addInformationItem()
    }
    if (notes.isEmpty()) {
      adapter.addItem(EmptyRecyclerItem())
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
  }

  private fun addInformationItem() {
    val informationItem = when {
      shouldShowThemeInformationItem() -> getThemeInformationItem(this)
      shouldShowBackupInformationItem() -> getBackupInformationItem(this)
      else -> null
    }
    if (informationItem === null) {
      return
    }
    adapter.addItem(informationItem, 0)
  }

  private suspend fun unifiedSearchSynchronous(): List<RecyclerItem> {
    val allItems = emptyList<RecyclerItem>().toMutableList()
    if (state.currentFolder != null) {
      val allNotes = unifiedSearchSynchronous(state)
      allItems.addAll(allNotes
                        .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
                        .map { it.await() })
      return allItems
    }

    val allNotes = unifiedSearchWithoutFolder(state)
    val directAcceptableFolders = filterDirectlyValidFolders(state)
    allItems.addAll(data.folders.getAll()
                      .map {
                        GlobalScope.async(Dispatchers.IO) {
                          val isDirectFolder = directAcceptableFolders.contains(it)
                          val notesCount = filterFolder(allNotes, it).size
                          if (state.hasFilter() && notesCount == 0 && !isDirectFolder) {
                            return@async null
                          }

                          FolderRecyclerItem(
                            context = this@MainActivity,
                            folder = it,
                            click = { onFolderChange(it) },
                            longClick = {
                              CreateOrEditFolderBottomSheet.openSheet(this@MainActivity, it, { _, _ -> refreshItems() })
                            },
                            selected = state.currentFolder?.uuid == it.uuid,
                            contents = notesCount)
                        }
                      }
                      .map { it.await() }
                      .filterNotNull())
    allItems.addAll(filterOutFolders(allNotes)
                      .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
                      .map { it.await() })
    return allItems
  }

  fun refreshItems() {
    GlobalScope.launch(Dispatchers.Main) {
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      handleNewItems(items.await())
    }
  }

  fun openTag(tag: Tag) {
    state.mode = if (state.mode == HomeNavigationMode.LOCKED) HomeNavigationMode.DEFAULT else state.mode
    state.tags.add(tag)
    refreshItems()
    updateToolbars()
  }

  override fun onResume() {
    super.onResume()
    refreshItems()
    notifyFolderChange()

    if (isInSearchMode)
      enterSearchMode()
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
    tryOpeningTheKeyboard()
    GlobalScope.launch(Dispatchers.Main) {
      withContext(Dispatchers.IO) { tagAndColorPicker.reset() }
      tagAndColorPicker.notifyChanged()
    }
    views.searchToolbar.textField.requestFocus()
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

  override fun onDestroy() {
    super.onDestroy()
    HouseKeeperJob.schedule()
  }

  override fun onStop() {
    super.onStop()
    if (PermissionUtils().getStoragePermissionManager(this).hasAllPermissions()) {
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()
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
    note.softDelete(this)
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
