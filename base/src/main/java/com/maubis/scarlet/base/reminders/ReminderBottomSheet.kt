package com.maubis.scarlet.base.reminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.view.View.GONE
import android.widget.TextView
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.uibasics.views.UIActionView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.sheets.GenericOptionsBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionsItem
import com.maubis.scarlet.base.common.ui.ThemeColorType
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.dateFormat
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.actions.INoteActionsSheetActivity
import java.util.*

class ReminderBottomSheet : ThemedBottomSheetFragment() {

  private var selectedNote: Note? = null
  private var reminder: Reminder = Reminder(
    0,
    System.currentTimeMillis(),
    ReminderInterval.ONCE)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val note = selectedNote
    if (note === null) {
      return
    }

    val calendar = Calendar.getInstance()
    reminder = note.reminder ?: Reminder(
        0,
        calendar.timeInMillis,
        ReminderInterval.ONCE)

    val isNewReminder = reminder.uid == 0
    if (isNewReminder) {
      calendar.set(Calendar.HOUR_OF_DAY, 8)
      calendar.set(Calendar.MINUTE, 0)
      calendar.set(Calendar.SECOND, 0)
      if (Calendar.getInstance().after(calendar)) {
        calendar.add(Calendar.HOUR_OF_DAY, 24)
      }
      reminder.timestamp = calendar.timeInMillis
    }
    setColors()
    setContent(reminder)
    setListeners(note, isNewReminder)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setListeners(note: Note, isNewReminder: Boolean) {
    val dlg = dialog
    if (dlg === null) {
      return
    }

    val reminderDate = dlg.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dlg.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dlg.findViewById<UIActionView>(R.id.reminder_repeat)

    reminderDate.setOnClickListener {
      if (reminder.interval == ReminderInterval.ONCE) {
        openDatePickerDialog()
      }
    }
    reminderTime.setOnClickListener {
      openTimePickerDialog()
    }
    reminderRepeat.setOnClickListener {
      openFrequencyDialog()
    }

    val removeAlarmBtn = dlg.findViewById<TextView>(R.id.remove_alarm)
    val setAlarmBtn = dlg.findViewById<TextView>(R.id.set_alarm)
    val activity = themedActivity() as INoteActionsSheetActivity
    if (isNewReminder) {
      removeAlarmBtn.visibility = GONE
    }
    removeAlarmBtn.setOnClickListener {
      ReminderJob.cancelJob(reminder.uid)
      note.reminder = null
      note.save(themedContext())
      activity.updateNote(note)
      dismiss()
    }
    setAlarmBtn.setOnClickListener {
      if (Calendar.getInstance().after(reminder.toCalendar())) {
        dismiss()
        return@setOnClickListener
      }

      if (!isNewReminder) {
        ReminderJob.cancelJob(reminder.uid)
      }

      val uid = ReminderJob.scheduleJob(note.uuid, reminder)
      if (uid == -1) {
        dismiss()
        return@setOnClickListener
      }

      reminder.uid = uid
      note.reminder = reminder
      note.save(themedContext())
      activity.updateNote(note)
      dismiss()
    }
  }

  private fun getReminderIntervalLabel(interval: ReminderInterval): Int {
    return when (interval) {
      ReminderInterval.ONCE -> R.string.reminder_frequency_once
      ReminderInterval.DAILY -> R.string.reminder_frequency_daily
    }
  }

  private fun openFrequencyDialog() {
    val isSelected = fun(interval: ReminderInterval): Boolean = interval == reminder.interval
    com.maubis.scarlet.base.common.sheets.openSheet(
      themedActivity() as ThemedActivity,
      GenericOptionsBottomSheet().apply {
        title = R.string.reminder_sheet_repeat
        options = arrayListOf(
          LithoChooseOptionsItem(
            title = getReminderIntervalLabel(ReminderInterval.ONCE),
            listener = {
              reminder.interval = ReminderInterval.ONCE
              setContent(reminder)
              dismiss()
            },
            selected = isSelected(ReminderInterval.ONCE)
          ),
          LithoChooseOptionsItem(
            title = getReminderIntervalLabel(ReminderInterval.DAILY),
            listener = {
              reminder.interval = ReminderInterval.DAILY
              setContent(reminder)
              dismiss()
            },
            selected = isSelected(ReminderInterval.DAILY)
          )
        )
      }
    )
  }

  private fun openDatePickerDialog() {
    val calendar = reminder.toCalendar()
    val dialog = DatePickerDialog(
      themedContext(),
      R.style.DialogTheme,
      { _, year, month, day ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        reminder.timestamp = calendar.timeInMillis
        setContent(reminder)
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH))
    dialog.show()
  }

  private fun openTimePickerDialog() {
    val calendar = reminder.toCalendar()
    val dialog = TimePickerDialog(
      themedContext(),
      R.style.DialogTheme,
      { _, hourOfDay, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        reminder.timestamp = calendar.timeInMillis
        setContent(reminder)
      },
      calendar.get(Calendar.HOUR_OF_DAY),
      calendar.get(Calendar.MINUTE),
      false)
    dialog.show()
  }

  private fun setContent(reminder: Reminder) {
    val dlg = dialog
    if (dlg === null) {
      return
    }

    val reminderDate = dlg.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dlg.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dlg.findViewById<UIActionView>(R.id.reminder_repeat)

    reminderRepeat.setSubtitle(getReminderIntervalLabel(reminder.interval))
    reminderTime.setSubtitle(dateFormat.readableTime(DateFormatter.Formats.HH_MM_A.format, reminder.timestamp))
    reminderDate.setSubtitle(dateFormat.readableTime(DateFormatter.Formats.DD_MMM_YYYY.format, reminder.timestamp))
    reminderDate.alpha = if (reminder.interval == ReminderInterval.ONCE) 1.0f else 0.5f
  }

  private fun setColors() {
    val dlg = dialog
    if (dlg === null) {
      return
    }

    val reminderDate = dlg.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dlg.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dlg.findViewById<UIActionView>(R.id.reminder_repeat)

    val iconColor = appTheme.get(ThemeColorType.TOOLBAR_ICON)
    val textColor = appTheme.get(ThemeColorType.TERTIARY_TEXT)
    val titleColor = appTheme.get(ThemeColorType.SECTION_HEADER)

    reminderDate.setTitleColor(titleColor)
    reminderDate.setSubtitleColor(textColor)
    reminderDate.setImageTint(iconColor)
    reminderDate.setActionTint(iconColor)

    reminderTime.setTitleColor(titleColor)
    reminderTime.setSubtitleColor(textColor)
    reminderTime.setImageTint(iconColor)
    reminderTime.setActionTint(iconColor)

    reminderRepeat.setTitleColor(titleColor)
    reminderRepeat.setSubtitleColor(textColor)
    reminderRepeat.setImageTint(iconColor)
    reminderRepeat.setActionTint(iconColor)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.card_layout)

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = ReminderBottomSheet()
      sheet.selectedNote = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}