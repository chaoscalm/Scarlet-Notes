package com.maubis.scarlet.base

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.database.ApplicationData
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator
import com.maubis.scarlet.base.support.ui.ThemeManager
import com.maubis.scarlet.base.support.ui.TypefaceController
import com.maubis.scarlet.base.support.utils.DateFormatUtils
import com.maubis.scarlet.base.support.utils.ImageCache
import com.maubis.scarlet.base.support.utils.dateFormat

class ScarletApp : Application() {
  override fun onCreate() {
    super.onCreate()
    data = ApplicationData(this)

    appPreferences = getSharedPreferences("scarlet_prefs", MODE_PRIVATE)

    dateFormat = DateFormatUtils(this)
    SoLoader.init(this, false)
    try {
      JobManager.create(this).addJobCreator(ReminderJobCreator())
    } catch (exception: Exception) {
      Log.e("Scarlet", "Unable to initialize job manager", exception)
    }

    // Setup Image Cache
    imageStorage = NoteImage(this)
    imageCache = ImageCache(this)

    // Setup Application Theme
    appTheme = ThemeManager()
    appTheme.setup(this)
    appTypeface = TypefaceController(this)
  }

  companion object {
    lateinit var data: ApplicationData

    lateinit var imageStorage: NoteImage
    lateinit var imageCache: ImageCache

    lateinit var appPreferences: SharedPreferences

    lateinit var appTheme: ThemeManager
    lateinit var appTypeface: TypefaceController
  }
}