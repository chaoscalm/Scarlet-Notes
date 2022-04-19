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
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.utils.ImageLoadCallback
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.home.sheets.openDeleteFormatDialog
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class FormatImageViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  private val text: TextView = root.findViewById(R.id.text)
  private val image: ImageView = root.findViewById(R.id.image)

  private val actionCamera: ImageView = root.findViewById(R.id.action_camera)
  private val actionGallery: ImageView = root.findViewById(R.id.action_gallery)
  private val dragHandle: ImageView = root.findViewById(R.id.action_move_icon)
  private val imageToolbar: View = root.findViewById(R.id.image_toolbar)
  private val noImageMessage: UITextView = root.findViewById(R.id.no_image_message)

  private var format: Format? = null

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    format = data

    text.setTextColor(config.secondaryTextColor)
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    text.setOnClickListener {
      EasyImage.openGallery(context as AppCompatActivity, data.uid)
    }

    noImageMessage.visibility = View.GONE
    noImageMessage.setTextColor(config.tertiaryTextColor)
    noImageMessage.setOnClickListener {
      openDeleteFormatDialog(activity, data)
    }

    val iconColor = config.iconColor
    noImageMessage.setImageTint(iconColor)
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
    noImageMessage.setBackgroundColor(imageToolbarBg)

    val fileName = data.text
    when {
      fileName.isBlank() -> image.visibility = View.GONE
      else -> {
        val file = imageStorage.getImage(config.noteUUID, data)
        when (file.exists()) {
          true -> populateFile(file)
          false -> {
            noImageMessage.setText(R.string.image_not_on_current_device)
            noImageMessage.isVisible = config.editable
            image.visibility = View.GONE
            imageToolbar.visibility = View.GONE
          }
        }
      }
    }
  }

  fun populateFile(file: File) {
    imageStorage.loadImageToImageView(image, file, object : ImageLoadCallback {
      override fun onSuccess() {
        noImageMessage.visibility = View.GONE
      }

      override fun onError() {
        noImageMessage.visibility = View.VISIBLE
        noImageMessage.setText(R.string.image_cannot_be_loaded)
      }
    })
  }
}
