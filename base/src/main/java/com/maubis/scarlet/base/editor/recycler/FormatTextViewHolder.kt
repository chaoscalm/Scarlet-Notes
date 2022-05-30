package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.maubis.markdown.Markdown
import com.maubis.markdown.spannable.clearMarkdownSpans
import com.maubis.markdown.spannable.setFormats
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.MarkdownFormatting
import com.maubis.scarlet.base.editor.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.settings.sEditorLiveMarkdown
import kotlin.math.min

open class FormatTextViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view), TextWatcher {

  protected val text: TextView = root.findViewById(R.id.text)
  protected val edit: EditText = root.findViewById(R.id.edit)
  private val dragHandle: ImageView = root.findViewById(R.id.action_move_icon)

  protected lateinit var format: Format

  init {
    edit.addTextChangedListener(this)
    edit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
      activity.focusedFormat = if (hasFocus) format else null
    }
    edit.setRawInputType(
      InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        or InputType.TYPE_CLASS_TEXT
        or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
    )
  }

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    format = data

    val fontSize = when (data.type) {
      FormatType.HEADING -> config.fontSize * 1.75f
      FormatType.SUB_HEADING -> config.fontSize * 1.5f
      FormatType.HEADING_3 -> config.fontSize * 1.25f
      else -> config.fontSize
    }
    text.setAppearanceFromConfig(config)
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    text.setTextIsSelectable(true)
    text.isVisible = !config.editable

    edit.setAppearanceFromConfig(config)
    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    edit.isVisible = config.editable
    edit.isEnabled = config.editable
    showHintWhenTextIsEmpty()

    when {
      config.editable -> edit.setText(data.text)
      config.isMarkdownEnabled -> text.text = Markdown.renderSegment(data.text, true)
      else -> text.text = data.text
    }

    dragHandle.setColorFilter(config.iconColor)
    dragHandle.isVisible = config.editable
    dragHandle.setOnClickListener {
      openSheet(activity, FormatActionBottomSheet().apply {
        noteUUID = config.noteUUID
        format = data
      })
    }
    dragHandle.setOnLongClickListener {
      activity.startFormatDrag(this)
      true
    }
  }

  override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {

  }

  override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
    if (!edit.isFocused) {
      return
    }

    format.text = text.toString()
    activity.setFormat(format)
    showHintWhenTextIsEmpty()
  }

  // Workaround to avoid this holder being higher than the text contained in it when hint text
  // occupies more lines than the text inserted by the user
  private fun showHintWhenTextIsEmpty() {
    edit.hint = when {
      format.text.isEmpty() -> format.getHint()
      else -> ""
    }
  }

  override fun afterTextChanged(text: Editable) {
    text.clearMarkdownSpans()
    if (sEditorLiveMarkdown && format.type != FormatType.CODE) {
      text.setFormats(Markdown.getSpanInfo(format.text).spans)
    }
  }

  fun requestEditTextFocus() {
    edit.requestFocus()
  }

  fun insertMarkdownFormatting(formatting: MarkdownFormatting) {
    val cursorStartPosition = edit.selectionStart
    val cursorEndPosition = edit.selectionEnd
    val content = edit.text

    val startString = content.substring(0, cursorStartPosition)
    val middleString = content.substring(cursorStartPosition, cursorEndPosition)
    val endString = content.substring(cursorEndPosition, content.length)

    val stringBuilder = StringBuilder()
    stringBuilder.append(startString)
    stringBuilder.append(if (startString.isEmpty() || !formatting.requiresNewLine) "" else "\n")
    stringBuilder.append(formatting.startToken)
    stringBuilder.append(middleString)
    stringBuilder.append(formatting.endToken)
    stringBuilder.append(endString)

    edit.setText(stringBuilder.toString())

    try {
      val additionTokenLength = (if (formatting.requiresNewLine) 1 else 0) + formatting.startToken.length
      edit.setSelection(min(startString.length + additionTokenLength, edit.text.length))
    } catch (e: Exception) {
      Log.d("Scarlet", "Error while setting text selection", e)
    }
  }

  private fun Format.getHint(): String {
    return when (type) {
      FormatType.TEXT, FormatType.TAG -> context.getString(R.string.format_hint_text)
      FormatType.HEADING,
      FormatType.SUB_HEADING,
      FormatType.HEADING_3
      -> context.getString(R.string.format_hint_heading)
      FormatType.NUMBERED_LIST,
      FormatType.CHECKLIST_UNCHECKED,
      FormatType.CHECKLIST_CHECKED
      -> context.getString(R.string.format_hint_list)
      FormatType.CODE -> context.getString(R.string.format_hint_code)
      FormatType.QUOTE -> context.getString(R.string.format_hint_quote)
      else -> ""
    }
  }
}
