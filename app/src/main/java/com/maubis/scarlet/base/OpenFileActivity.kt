package com.maubis.scarlet.base

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.maubis.markdown.Markdown.applyMarkdownSpans
import com.maubis.markdown.spannable.clearMarkdownSpans
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.ui.SecuredActivity
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.common.utils.setIconTint
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.databinding.ActivityExternalIntentBinding
import com.maubis.scarlet.base.editor.ViewNoteActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OpenFileActivity : SecuredActivity() {

  var filenameText: String = ""
  var contentText: String = ""

  private lateinit var views: ActivityExternalIntentBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val hasFileIntent = handleFileIntent(intent)
    if (!hasFileIntent) {
      finish()
      return
    }

    views = ActivityExternalIntentBinding.inflate(layoutInflater)
    setContentView(views.root)
    setupListeners()
    applyTheming()

    val spannable = SpannableString(contentText)
    spannable.applyMarkdownSpans(contentText)
    views.content.setText(spannable, TextView.BufferType.SPANNABLE)
    views.content.doOnTextChanged { text, _, _, _ ->
      if (text is Editable) {
        text.clearMarkdownSpans()
        text.applyMarkdownSpans(text.toString())
      }
    }
    views.toolbar.fileName.text = filenameText
  }

  private fun setupListeners() {
    views.toolbar.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    views.toolbar.importButton.setOnClickListener {
      lifecycleScope.launch {
        val note = Note.create(title = "", description = contentText)
        withContext(Dispatchers.IO) { note.save(this@OpenFileActivity) }
        startActivity(ViewNoteActivity.makeIntent(this@OpenFileActivity, note))
        finish()
      }
    }
  }

  private fun handleFileIntent(intent: Intent): Boolean {
    val data = intent.data
    val lastPathSegment = data?.lastPathSegment
    if (data === null || lastPathSegment === null) {
      return false
    }

    try {
      contentResolver.openInputStream(data)?.use { stream ->
        contentText = stream.bufferedReader().use { it.readText() }
      }
      filenameText = lastPathSegment
      return true
    } catch (exception: Exception) {
      return false
    }
  }

  override fun applyTheming() {
    updateStatusBarTheme()

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val textColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    views.toolbar.fileName.setTextColor(textColor)
    views.content.setTextColor(textColor)

    val iconColor = appTheme.getColor(ThemeColor.ICON)
    views.toolbar.backButton.setIconTint(iconColor)
    views.toolbar.importButton.setImageTint(iconColor)
    views.toolbar.importButton.setTextColor(iconColor)
  }
}
