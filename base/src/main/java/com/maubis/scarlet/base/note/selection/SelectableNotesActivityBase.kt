package com.maubis.scarlet.base.note.selection

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.recycler.RecyclerItem
import com.maubis.scarlet.base.common.ui.SecuredActivity
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.utils.sort
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.home.recycler.NoNotesRecyclerItem
import com.maubis.scarlet.base.note.folder.SelectorFolderRecyclerItem
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.recycler.getSelectableRecyclerItemControllerList
import com.maubis.scarlet.base.settings.STORE_KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.notesSortingTechniquePref
import com.maubis.scarlet.base.settings.sNoteItemLineCount
import com.maubis.scarlet.base.settings.sUIUseGridView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

abstract class SelectableNotesActivityBase : SecuredActivity(), INoteSelectorActivity {

  lateinit var recyclerView: RecyclerView
  lateinit var adapter: NoteAppAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayoutUI())
  }

  open fun initUI() {
    notifyThemeChange()
    setupRecyclerView()
    loadNotes()
    findViewById<View>(R.id.back_button).setOnClickListener {
      onBackPressed()
    }
  }

  private fun loadNotes() {
    lifecycleScope.launch(Dispatchers.IO) {
      val notes = sort(getNotes(), notesSortingTechniquePref)
        .sortedBy { it.folder }
        .map { NoteRecyclerItem(this@SelectableNotesActivityBase, it) }

      val items = mutableListOf<RecyclerItem>()
      var lastFolder: UUID? = null
      notes.forEach {
        val noteFolderId = it.note.folder
        if (noteFolderId != null && lastFolder != noteFolderId) {
          val folder = data.folders.getByUUID(noteFolderId)
          if (folder !== null) {
            items.add(SelectorFolderRecyclerItem(this@SelectableNotesActivityBase, folder))
            lastFolder = noteFolderId
          }
        }
        items.add(it)
      }

      withContext(Dispatchers.Main) {
        adapter.clearItems()
        if (items.isEmpty()) {
          adapter.addItem(NoNotesRecyclerItem())
        }
        items.forEach { adapter.addItem(it) }
      }
    }
  }

  abstract fun getNotes(): List<Note>

  open fun getLayoutUI(): Int = R.layout.activity_select_note

  private fun setupRecyclerView() {
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    val adapterExtra = Bundle()
    adapterExtra.putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)

    adapter = NoteAppAdapter(this, getSelectableRecyclerItemControllerList(sUIUseGridView, isTablet))
    adapter.setExtra(adapterExtra)
    recyclerView = findViewById(R.id.recycler_view)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = getLayoutManager(sUIUseGridView, isTablet)
    recyclerView.setHasFixedSize(true)
  }

  override fun notifyThemeChange() {
    updateStatusBarTheme()

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = appTheme.get(ThemeColorType.TOOLBAR_ICON);
    findViewById<ImageView>(R.id.back_button).setColorFilter(toolbarIconColor)
    findViewById<TextView>(R.id.toolbar_title).setTextColor(toolbarIconColor)
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    if (isTabletView) {
      return StaggeredGridLayoutManager(2, GridLayout.VERTICAL)
    }
    return if (isStaggeredView)
      StaggeredGridLayoutManager(2, GridLayout.VERTICAL)
    else
      LinearLayoutManager(this)
  }
}
