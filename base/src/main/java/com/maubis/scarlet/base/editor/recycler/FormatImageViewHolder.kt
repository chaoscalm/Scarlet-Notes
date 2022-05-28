package com.maubis.scarlet.base.editor.recycler

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.sheet.FormatActionBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class FormatImageViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  private val text: TextView = root.findViewById(R.id.text)
  private val image: ImageView = root.findViewById(R.id.image)

  private val actionCamera: ImageView = root.findViewById(R.id.action_camera)
  private val actionGallery: ImageView = root.findViewById(R.id.action_gallery)
  private val dragHandle: ImageView = root.findViewById(R.id.action_move_icon)
  private val imageToolbar: View = root.findViewById(R.id.image_toolbar)
  private val noImageIcon: ImageView = root.findViewById(R.id.no_image_icon)

  private var format: Format? = null

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    format = data

    text.setTextColor(config.secondaryTextColor)
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    text.setOnClickListener {
      EasyImage.openGallery(context as AppCompatActivity, data.uid)
    }

    val iconColor = config.iconColor
    noImageIcon.setColorFilter(iconColor)
    actionCamera.setColorFilter(iconColor)
    actionGallery.setColorFilter(iconColor)
    actionCamera.setOnClickListener {
      try {
        EasyImage.openCameraForImage(context as AppCompatActivity, data.uid)
      } catch (e: Exception) {
        Toast.makeText(context, "No camera app installed", Toast.LENGTH_SHORT).show()
        Log.e("Scarlet", "Error while opening camera", e)
      }
    }
    actionGallery.setOnClickListener {
      try {
        EasyImage.openGallery(context as AppCompatActivity, data.uid)
      } catch (e: Exception) {
        Toast.makeText(context, "No photo picker app installed", Toast.LENGTH_SHORT).show()
        Log.e("Scarlet", "Error while opening gallery picker", e)
      }
    }
    dragHandle.setColorFilter(config.iconColor)
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
    imageToolbar.isVisible = config.editable

    val imageToolbarBg = config.backgroundColor
    imageToolbar.setBackgroundColor(imageToolbarBg)

    val fileName = data.text
    when {
      fileName.isBlank() -> image.visibility = View.GONE
      else -> {
        val file = imageStorage.getImage(config.noteUUID, data)
        if (file.exists()) {
          populateFile(file)
        } else {
          text.setText(R.string.image_not_found)
          noImageIcon.isVisible = config.editable
          image.visibility = View.GONE
        }
      }
    }
  }

  fun populateFile(file: File) {
    (context as LifecycleOwner).lifecycleScope.launch {
      val bitmap = withContext(Dispatchers.IO) { imageStorage.loadBitmap(file) }
      if (bitmap == null) {
        image.visibility = View.GONE
        noImageIcon.visibility = View.VISIBLE
        text.setText(R.string.image_cannot_be_loaded)
        return@launch
      }

      noImageIcon.visibility = View.GONE
      text.setText(R.string.format_hint_image)
      image.visibility = View.VISIBLE
      image.setImageBitmap(bitmap)
    }
  }
}
