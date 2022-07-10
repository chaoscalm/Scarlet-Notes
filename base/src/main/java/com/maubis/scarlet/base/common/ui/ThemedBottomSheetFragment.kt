package com.maubis.scarlet.base.common.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.utils.enterFullScreenIfEnabled

abstract class ThemedBottomSheetFragment : BottomSheetDialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    @Suppress("DEPRECATION")
    retainInstance = true
    val isTablet = context?.resources?.getBoolean(R.bool.is_tablet) ?: false
    val dialog = when {
      isTablet -> BottomSheetTabletDialog(requireContext(), theme)
      else -> super.onCreateDialog(savedInstanceState)
    }
    dialog.setContentView(inflateLayout())
    dialog.window?.enterFullScreenIfEnabled()
    resetBackground(dialog)
    setupDialogViews(dialog)
    return dialog
  }

  abstract fun inflateLayout(): View

  abstract fun setupDialogViews(dialog: Dialog)

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