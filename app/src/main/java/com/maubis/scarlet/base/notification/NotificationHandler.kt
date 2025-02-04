package com.maubis.scarlet.base.notification

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.OsVersionUtils
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.note.getDisplayTime
import com.maubis.scarlet.base.note.getTextWithoutTitle
import com.maubis.scarlet.base.note.getTitle

const val REQUEST_CODE_BASE = 3200
const val REQUEST_CODE_MULTIPLIER = 250
const val NOTE_NOTIFICATION_CHANNEL_ID = "NOTE_NOTIFICATION_CHANNEL"
const val REMINDER_NOTIFICATION_CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"

class NotificationConfig(
    val note: Note,
    val channel: String = NOTE_NOTIFICATION_CHANNEL_ID
)

class NotificationHandler(private val context: Context) {

  private val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  init {
    createNotificationChannel()
  }

  fun updateExistingNotification(config: NotificationConfig) {
    if (!OsVersionUtils.canExtractActiveNotifications())
      return

    for (notification in notificationManager.activeNotifications) {
      if (notification.id == config.note.uid) {
        openNotification(config)
      }
    }
  }

  fun cancelNotification(id: Int) {
    notificationManager.cancel(id)
  }

  fun openNotification(config: NotificationConfig) {
    val pendingIntent = buildActivityPendingIntent(config, ViewNoteActivity.makePreferenceAwareIntent(context.applicationContext, config.note), 1)
    val contentView = getRemoteView(config)
    val notificationBuilder = NotificationCompat.Builder(context, config.channel)
      .setSmallIcon(R.drawable.ic_quote)
      .setContentTitle(config.note.getTitle())
      .setColor(config.note.color)
      .setCategory(NotificationCompat.CATEGORY_EVENT)
      .setContent(contentView)
      .setCustomBigContentView(contentView)
      .setContentIntent(pendingIntent)
      .setAutoCancel(false)

    if (config.channel === REMINDER_NOTIFICATION_CHANNEL_ID) {
      notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
      notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
      notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
      notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
    }

    notificationManager.notify(config.note.uid, notificationBuilder.build())
  }

  private fun createNotificationChannel() {
    if (!OsVersionUtils.canAddNotificationChannels()) {
      return
    }

    val channel = NotificationChannel(
            NOTE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_label),
            NotificationManager.IMPORTANCE_MIN)
    notificationManager.createNotificationChannel(channel)

    val channelForReminder = NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_reminder_channel_label),
            NotificationManager.IMPORTANCE_HIGH)
    notificationManager.createNotificationChannel(channelForReminder)
  }

  private fun getRemoteView(config: NotificationConfig): RemoteViews {
    val contentView = RemoteViews(context.packageName, R.layout.notification_note_layout)
    val hasTitle = config.note.getTitle().isNotEmpty()
    contentView.setViewVisibility(R.id.title, if (hasTitle) VISIBLE else GONE)
    contentView.setTextViewText(R.id.title, config.note.getTitle())
    contentView.setTextViewText(R.id.description, config.note.getTextWithoutTitle())
    contentView.setTextViewText(R.id.timestamp, config.note.getDisplayTime(context))

    val titleColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val descColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)
    contentView.setTextColor(R.id.title, titleColor)
    contentView.setTextColor(R.id.description, titleColor)
    contentView.setTextColor(R.id.timestamp, descColor)

    val backgroundColor = appTheme.getColor(ThemeColor.BACKGROUND)
    contentView.setInt(R.id.root_layout, "setBackgroundColor", backgroundColor)

    val iconColor = appTheme.getColor(ThemeColor.ICON)
    contentView.setInt(R.id.options_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.copy_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.delete_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.edit_button, "setColorFilter", iconColor)

    contentView.setOnClickPendingIntent(
      R.id.options_button,
      buildActivityPendingIntent(config, getNoteOpenIntent(config), 2))
    contentView.setOnClickPendingIntent(
      R.id.edit_button,
      buildActivityPendingIntent(config, getNoteEditIntent(config), 3))
    contentView.setOnClickPendingIntent(
      R.id.copy_button,
      buildBroadcastPendingIntent(config, getNoteActionIntent(config, NotificationActionReceiver.ACTION_COPY), 4))
    contentView.setOnClickPendingIntent(
      R.id.delete_button,
      buildBroadcastPendingIntent(config, getNoteActionIntent(config, NotificationActionReceiver.ACTION_DELETE), 6))

    return contentView
  }

  private fun getNoteOpenIntent(config: NotificationConfig): Intent {
    val intent = Intent(context, ViewNoteActivity::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    return intent
  }

  private fun getNoteEditIntent(config: NotificationConfig): Intent {
    val intent = Intent(context, EditNoteActivity::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    return intent
  }

  private fun buildActivityPendingIntent(config: NotificationConfig, intent: Intent, requestCode: Int): PendingIntent {
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(intent)
    return stackBuilder.getPendingIntent(
      REQUEST_CODE_BASE + config.note.uid + requestCode * REQUEST_CODE_MULTIPLIER,
      PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun getNoteActionIntent(config: NotificationConfig, action: String): Intent {
    val intent = Intent(context, NotificationActionReceiver::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    intent.action = action
    return intent
  }

  private fun buildBroadcastPendingIntent(
    config: NotificationConfig,
    intent: Intent,
    requestCode: Int): PendingIntent {
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_BASE + config.note.uid + requestCode * REQUEST_CODE_MULTIPLIER,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT)
  }
}