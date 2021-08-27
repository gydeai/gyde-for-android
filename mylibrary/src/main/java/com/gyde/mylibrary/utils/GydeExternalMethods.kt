package com.gyde.mylibrary.utils

import android.content.Context
import android.util.Log

class GydeExternalMethods {

    companion object : GydeTooltipWindow.ToolTipClickListener {
        /**
         * This method will show the single tooltip on specified view.
         * Tooltip will be shown and on Done button it will get dismissed.
         * @param context Context : Current activity context
         * @param viewId Int : tooltip will be shown on this view id
         * @param title String : Title for tooltip
         * @param description String : Description / message for tooltip
         * @param tooltipPosition GydeTooltipPosition? : Position of tooltip i.e. top, bottom, left, right
         * @param buttonText String? : Button title text on tooltip
         */
        fun showGydeTooltip(
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

        /**
         * Start Gyde walkthrough by walkthrough id. It will handle single walkthrough flow.
         *
         * @param context Context : Current screen context
         * @param walkthroughId String : Gyde walkthrough Id
         */
        fun startGydeWalkthrough(context: Context, walkthroughId: String) {
            GydeInternalCommonUtils.getWalkthroughStepsFromId(context, walkthroughId)
        }

        override fun nextButtonClicked(context: Context) {
            Log.e("button click", "clicked")
        }
    }
}
