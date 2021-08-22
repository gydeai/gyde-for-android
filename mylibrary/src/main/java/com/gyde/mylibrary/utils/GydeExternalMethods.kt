package com.gyde.mylibrary.utils

import android.content.Context
import android.util.Log

class GydeExternalMethods {

    companion object : GydeTooltipWindow.ToolTipClickListener {
        fun showGydeTooltipOnView(
            context: Context,
            viewId: Int,
            title: String,
            description: String,
            tooltipPosition: GydeTooltipPosition?,
            buttonText: String?
        ) {
            val tooltipWindow = GydeTooltipWindow(
                context = context,
                toolTipPosition = tooltipPosition ?: GydeTooltipPosition.DRAW_BOTTOM_CENTER,
                viewId = null,
                titleText = title,
                descriptionText = description,
                buttonText = buttonText ?: "Done",
                nextClickListener = this,
                voiceOverPath = "",
                viewIdInt = viewId
            )

            tooltipWindow.showTooltipFromClientInput()
        }

        override fun nextButtonClicked() {
            Log.e("button click", "clicked")
        }
    }
}