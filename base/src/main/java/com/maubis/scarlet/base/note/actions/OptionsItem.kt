package com.maubis.scarlet.base.note.actions

import android.view.View

class OptionsItem(
    val title: Int,
    val subtitle: Int,
    val icon: Int,
    val selected: Boolean = false, // indicates its a selected option (blue color)
    val visible: Boolean = true, // indicates if the option is visible
    val invalid: Boolean = false, // indicates if the option will show a checked on the side
    val content: String = "", // indicates that the option will be faded and click removed
    val actionIcon: Int = 0, // content is an alternative to subtitle when it's 0
    val listener: View.OnClickListener
)