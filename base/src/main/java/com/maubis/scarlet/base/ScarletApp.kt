package com.maubis.scarlet.base

import android.app.Application
import android.util.Log
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.maubis.scarlet.base.common.ui.ThemeManager
import com.maubis.scarlet.base.common.ui.TypefaceController
import com.maubis.scarlet.base.common.utils.ImageCache
import com.maubis.scarlet.base.common.utils.ImageStore
import com.maubis.scarlet.base.database.ApplicationData
import com.maubis.scarlet.base.reminders.ReminderJobCreator
import com.maubis.scarlet.base.settings.AppPreferences

class ScarletApp : Application() {
  override fun onCreate() {
    super.onCreate()
    data = ApplicationData(this)
    prefs = AppPreferences(getSharedPreferences("scarlet_prefs", MODE_PRIVATE))

    SoLoader.init(this, false)
    try {
      JobManager.create(this).addJobCreator(ReminderJobCreator())
    } catch (exception: Exception) {
      Log.e("Scarlet", "Unable to initialize job manager", exception)
    }

    // Setup Image Cache
    imageStorage = ImageStore(this, ImageCache(this))

    // Setup Application Theme
    appTheme = ThemeManager()
    appTheme.setup(this)
    appTypeface = TypefaceController(this)
  }

  companion object {
    lateinit var data: ApplicationData
    lateinit var imageStorage: ImageStore

    lateinit var prefs: AppPreferences

    lateinit var appTheme: ThemeManager
    lateinit var appTypeface: TypefaceController
  }
}