package com.maubis.scarlet.base.common.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme

abstract class ThemedBottomSheetFragment : BottomSheetDialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    retainInstance = true
    val isTablet = context?.resources?.getBoolean(R.bool.is_tablet) ?: false
    val dialog = when {
      isTablet -> BottomSheetTabletDialog(requireContext(), theme)
      else -> super.onCreateDialog(savedInstanceState)
    }
    dialog.setContentView(View.inflate(context, getLayout(), null))
    resetBackground(dialog)
    setupDialogViews(dialog)
    return dialog
  }

  abstract fun setupDialogViews(dialog: Dialog)

  @LayoutRes
  abstract fun getLayout(): Int

  private fun getBackgroundView(): Int = R.id.container_layout

  protected fun setAlwaysExpanded(dialog: Dialog) {
    with((dialog as BottomSheetDialog).behavior) {
      state = BottomSheetBehavior.STATE_EXPANDED
      skipCollapsed = true
    }
  }

  private fun resetBackground(dialog: Dialog) {
    val backgroundColor = appTheme.getColor(ThemeColor.BACKGROUND)
    val containerLayout = dialog.findViewById<View>(getBackgroundView())
    containerLayout.setBackgroundColor(backgroundColor)
    for (viewId in getBackgroundCardViewIds()) {
      val cardView = dialog.findViewById<CardView>(viewId)
      cardView.setCardBackgroundColor(backgroundColor)
    }
  }

  open fun getOptionsTitleColor(selected: Boolean): Int {
    val colorResource = when {
      appTheme.isNightTheme() && selected -> com.github.bijoysingh.uibasics.R.color.material_blue_300
      appTheme.isNightTheme() -> com.github.bijoysingh.uibasics.R.color.light_secondary_text
      selected -> com.github.bijoysingh.uibasics.R.color.material_blue_700
      else -> com.github.bijoysingh.uibasics.R.color.dark_secondary_text
    }
    return ContextCompat.getColor(requireContext(), colorResource)
  }

  open fun getBackgroundCardViewIds(): Array<Int> = emptyArray()

  fun makeBackgroundTransparent(dialog: Dialog, rootLayoutId: Int) {
    val containerView = dialog.findViewById<View>(getBackgroundView())
    containerView.setBackgroundColor(Color.TRANSPARENT)

    val rootView = dialog.findViewById<View>(rootLayoutId)
    val parentView = rootView.parent
    if (parentView is View) {
      parentView.setBackgroundResource(R.drawable.note_option_bs_gradient)
    }
  }
}