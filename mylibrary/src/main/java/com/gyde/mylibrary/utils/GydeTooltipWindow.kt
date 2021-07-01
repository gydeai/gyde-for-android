package com.gyde.mylibrary.utils

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.gyde.mylibrary.R
import java.io.IOException

internal class GydeTooltipWindow(
    private var context: Context,
    private var toolTipPosition: GydeTooltipPosition,
    private val viewId: String?,
    private val titleText: String?,
    private val descriptionText: String?,
    private val buttonText: String?,
    private val nextClickListener: TooTipClickListener,
    private val voiceOverPath: String?,
) {

    interface TooTipClickListener {
        fun nextButtonClicked()
    }

    private lateinit var view: View
    private var contentView: View
    private var mTooltipTitle: TextView
    private var mTooltipDescription: TextView
    private var mImgPlayAudio: ImageView
    private var mImgClose: ImageView
    private var mNextButton: Button
    private var mImageArrow: ImageView
    private var tipWindow: PopupWindow? = null
    private var inflater: LayoutInflater
    private var mIsAudioPlaying: Boolean = false
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val MSG_DISMISS_TOOLTIP = 5000
    }

    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DISMISS_TOOLTIP -> if (tipWindow != null && tipWindow!!.isShowing) tipWindow!!.dismiss()
            }
        }
    }

    fun showTooltip(nextStepDescription: Int) {
        if (!viewId.isNullOrEmpty()) {
            val resID = context.resources.getIdentifier(viewId, "id", context.packageName)
            view = (context as Activity).findViewById(resID) as View
        }
        val arrowPosition = GydeTooltipArrowPosition.ARROW_DEFAULT_CENTER
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            25f,
            context.resources.displayMetrics
        ).toInt()
        when (arrowPosition) {
            GydeTooltipArrowPosition.ARROW_TOP_RIGHT -> {
                val layoutParams = LinearLayout.LayoutParams(height, height)
                layoutParams.gravity = Gravity.END
                layoutParams.setMargins(0, 0, 10, 0)
                mImageArrow.layoutParams = layoutParams
            }
            GydeTooltipArrowPosition.ARROW_TOP_CENTER -> {
                val layoutParams = LinearLayout.LayoutParams(height, height)
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 10, 0)
                mImageArrow.layoutParams = layoutParams
            }
            GydeTooltipArrowPosition.ARROW_DEFAULT_CENTER -> {
                val layoutParams = LinearLayout.LayoutParams(height, height)
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 0, 0)
                mImageArrow.layoutParams = layoutParams
            }
        }
        tipWindow?.height = ActionBar.LayoutParams.WRAP_CONTENT
        tipWindow?.width = ActionBar.LayoutParams.WRAP_CONTENT
        tipWindow?.isOutsideTouchable = false
        tipWindow?.isTouchable = true
        tipWindow?.isFocusable = false
        tipWindow?.setBackgroundDrawable(BitmapDrawable())
        tipWindow?.contentView = contentView
        val screenPos = IntArray(2)
        view.getLocationOnScreen(screenPos)

        val anchorRect = Rect(
            screenPos[0], screenPos[1],
            screenPos[0] +
                    view.width,
            screenPos[1] + view.height
        )

        contentView.measure(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )
        val contentViewHeight = contentView.measuredHeight
        val contentViewWidth = contentView.measuredWidth

        var positionX = 0
        var positionY = 0
        when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM -> {
                positionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                positionY = anchorRect.bottom - anchorRect.height() / 2 + 10
            }
            GydeTooltipPosition.DRAW_TOP -> {
                positionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                positionY = anchorRect.top - anchorRect.height()
            }
            GydeTooltipPosition.DRAW_LEFT -> {
                DRAW_RIGHT@ positionX = anchorRect.left - contentViewWidth - 30
                positionY = anchorRect.top
            }
            GydeTooltipPosition.DRAW_RIGHT -> {
                positionX = anchorRect.right
                positionY = anchorRect.top
            }
        }
        tipWindow?.showAtLocation(
            view, Gravity.NO_GRAVITY, positionX,
            positionY
        )
        setDescriptionText()
        mNextButton.setOnClickListener { v: View? ->
            tipWindow?.dismiss()
            nextClickListener.nextButtonClicked()
            if (nextStepDescription == 2) {
                (context as Activity).finish()
            }
        }

        setVolumeDrawable()
        playAudio(voiceOverPath ?: "")
        mImgPlayAudio.setOnClickListener {
            if (voiceOverPath != null && voiceOverPath.isNotEmpty()) {
                if (Util.isPlayVoiceOverEnabled) {
                    Util.isPlayVoiceOverEnabled = false
                } else {
                    Util.isPlayVoiceOverEnabled = true
                    playAudio(voiceOverPath)
                }
            }
            setVolumeDrawable()
        }
        mImgClose.setOnClickListener {
            tipWindow?.dismiss()
            Util.walkthroughSteps.clear()
            Util.stepCounter = 0
        }
    }

    private fun playAudio(voiceOverPath: String) {
        if (!mIsAudioPlaying && Util.isPlayVoiceOverEnabled && voiceOverPath.isNotEmpty()) {
            mIsAudioPlaying = true
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            try {
                mediaPlayer?.setDataSource(voiceOverPath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    mIsAudioPlaying = false
                    stopAudio()
                }
            } catch (e: IOException) {
                Log.e("Audio Exception", "prepare() failed" + e.message)
            }
        } else {
            mIsAudioPlaying = false
            stopAudio()
        }
    }

    private fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun openDrawerMenu() {
        if (!viewId.isNullOrEmpty()) {
            val resID = context.resources.getIdentifier(viewId, "id", context.packageName)
            val drawer = (context as Activity).findViewById<View>(resID) as DrawerLayout
            drawer.openDrawer(GravityCompat.START)
        }
    }

    init {
        this.tipWindow = PopupWindow(context)
        inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var layout = 0
        layout = when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM -> R.layout.tooltip_bottom_layout
            GydeTooltipPosition.DRAW_TOP -> R.layout.tooltip_top_layout
            GydeTooltipPosition.DRAW_LEFT -> R.layout.tooltip_left_layout
            GydeTooltipPosition.DRAW_RIGHT -> R.layout.tooltip_right_layout
        }
        contentView = inflater.inflate(layout, null)
        mTooltipTitle = contentView.findViewById<View>(R.id.tooltip_title) as TextView
        mTooltipDescription = contentView.findViewById<View>(R.id.tv_tooltip_content) as TextView
        mImageArrow = contentView.findViewById<View>(R.id.tooltip_nav_up) as ImageView
        mNextButton = contentView.findViewById<View>(R.id.next) as Button
        mImgPlayAudio = contentView.findViewById<View>(R.id.img_volume) as ImageView
        mImgClose = contentView.findViewById(R.id.img_close) as ImageView
        mNextButton.setBackgroundColor(Color.parseColor(Util.btnColor))
        setVolumeDrawable()
    }

    private fun setVolumeDrawable() {
        if (Util.isPlayVoiceOverEnabled) {
            mImgPlayAudio.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.gyde_ic_volume_off_24
                )
            )
        } else {
            mImgPlayAudio.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.gyde_ic_volume_up_24
                )
            )
        }
    }

    private fun setDescriptionText() {
        mTooltipTitle.text = String.format("%s", titleText)
        mTooltipDescription.text = String.format("%s", descriptionText)
        mNextButton.text = String.format("%s", buttonText)
    }
}