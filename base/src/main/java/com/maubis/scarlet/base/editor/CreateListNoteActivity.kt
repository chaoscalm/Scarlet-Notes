package com.maubis.scarlet.base.editor

import com.maubis.scarlet.base.editor.formats.FormatType

class CreateListNoteActivity : EditNoteActivity() {

  override fun addDefaultItem() {
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
  }
}
