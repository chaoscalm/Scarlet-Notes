package com.maubis.scarlet.base.home

import com.maubis.scarlet.base.R

/**
 * Superset of the Note State class
 */
enum class HomeNavigationMode {
  DEFAULT {
    override val toolbarTitleResourceId: Int = R.string.app_name
    override val toolbarIconResourceId: Int = R.mipmap.ic_launcher
  },
  TRASH {
    override val toolbarTitleResourceId: Int = R.string.nav_trash
    override val toolbarIconResourceId: Int = R.drawable.ic_delete
  },
  FAVOURITE {
    override val toolbarTitleResourceId: Int = R.string.nav_favourites
    override val toolbarIconResourceId: Int = R.drawable.ic_favorite
  },
  ARCHIVED {
    override val toolbarTitleResourceId: Int = R.string.nav_archived
    override val toolbarIconResourceId: Int = R.drawable.ic_archive
  },
  LOCKED {
    override val toolbarTitleResourceId: Int = R.string.nav_locked
    override val toolbarIconResourceId: Int = R.drawable.ic_lock
  };

  abstract val toolbarTitleResourceId: Int
  abstract val toolbarIconResourceId: Int
}