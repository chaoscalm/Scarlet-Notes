package com.maubis.scarlet.base.reminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View.GONE
import android.widget.TextView
import com.github.bijoysingh.uibasics.views.UIActionView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.GenericOptionsBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoChooseOptionsItem
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.common.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.common.utils.readableTime
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.actions.INoteActionsActivity
import java.util.*

class ReminderBottomSheet : ThemedBottomSheetFragment() {
  private val note: Note by lazy {
    val noteId = requireArguments().getInt(KEY_NOTE_ID)
    data.notes.getByID(noteId) ?: throw IllegalArgumentException("Invalid note ID")
  }

  private var reminder: Reminder = Reminder(
    0,
    System.currentTimeMillis(),
    ReminderInterval.ONCE)

  override fun setupDialogViews(dialog: Dialog) {
    val calendar = Calendar.getInstance()
    reminder = note.reminder ?: Reminder(0, calendar.timeInMillis, ReminderInterval.ONCE)
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
    setColors(dialog)
    setContent(dialog, reminder)
    setListeners(dialog, isNewReminder)
    makeBackgroundTransparent(dialog, R.id.root_layout)
    setAlwaysExpanded(dialog)
  }

  private fun setListeners(dialog: Dialog, isNewReminder: Boolean) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

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

    val removeAlarmBtn = dialog.findViewById<TextView>(R.id.remove_alarm)
    val setAlarmBtn = dialog.findViewById<TextView>(R.id.set_alarm)
    val activity = requireActivity() as INoteActionsActivity
    if (isNewReminder) {
      removeAlarmBtn.visibility = GONE
    }
    removeAlarmBtn.setOnClickListener {
      ReminderJob.cancelJob(reminder.uid)
      note.reminder = null
      note.save(requireContext())
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
      note.save(requireContext())
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
      requireActivity() as ThemedActivity,
      GenericOptionsBottomSheet().apply {
        title = R.string.reminder_sheet_repeat
        options = arrayListOf(
          LithoChooseOptionsItem(
            title = getReminderIntervalLabel(ReminderInterval.ONCE),
            listener = {
              reminder.interval = ReminderInterval.ONCE
              setContent(requireDialog(), reminder)
              dismiss()
            },
            selected = isSelected(ReminderInterval.ONCE)
          ),
          LithoChooseOptionsItem(
            title = getReminderIntervalLabel(ReminderInterval.DAILY),
            listener = {
              reminder.interval = ReminderInterval.DAILY
              setContent(requireDialog(), reminder)
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
      requireContext(),
      R.style.DialogTheme,
      { _, year, month, day ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        reminder.timestamp = calendar.timeInMillis
        setContent(requireDialog(), reminder)
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH))
    dialog.show()
  }

  private fun openTimePickerDialog() {
    val calendar = reminder.toCalendar()
    val dialog = TimePickerDialog(
      requireContext(),
      R.style.DialogTheme,
      { _, hourOfDay, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        reminder.timestamp = calendar.timeInMillis
        setContent(requireDialog(), reminder)
      },
      calendar.get(Calendar.HOUR_OF_DAY),
      calendar.get(Calendar.MINUTE),
      false)
    dialog.show()
  }

  private fun setContent(dialog: Dialog, reminder: Reminder) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    reminderRepeat.setSubtitle(getReminderIntervalLabel(reminder.interval))
    reminderTime.setSubtitle(readableTime(reminder.timestamp, "hh:mm a", requireContext()))
    reminderDate.setSubtitle(readableTime(reminder.timestamp, "dd MMM yyyy", requireContext()))
    reminderDate.alpha = if (reminder.interval == ReminderInterval.ONCE) 1.0f else 0.5f
  }

  private fun setColors(dialog: Dialog) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val iconColor = appTheme.getColor(ThemeColor.TOOLBAR_ICON)
    val textColor = appTheme.getColor(ThemeColor.TERTIARY_TEXT)
    val titleColor = appTheme.getColor(ThemeColor.SECTION_HEADER)

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
    private const val KEY_NOTE_ID = "note_id"

    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = ReminderBottomSheet()
      sheet.arguments = Bundle().apply { putInt(KEY_NOTE_ID, note.uid) }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}