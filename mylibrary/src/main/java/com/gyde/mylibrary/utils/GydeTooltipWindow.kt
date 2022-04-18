package com.gyde.mylibrary.utils

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.gyde.mylibrary.R
import com.gyde.mylibrary.screens.OnKeyboardVisibilityListener
// import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
// import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.registerEventListener
// import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.io.IOException
import java.lang.Exception

internal class GydeTooltipWindow(
    private var context: Context,
    private var toolTipPosition: GydeTooltipPosition,
    private val viewId: String?,
    private val titleText: String?,
    private val descriptionText: String?,
    private val buttonText: String?,
    private val nextClickListener: ToolTipClickListener?,
    private val voiceOverPath: String?,
    private val viewIdInt: Int?
) : OnKeyboardVisibilityListener {

    interface ToolTipClickListener {
        fun nextButtonClicked(context: Context)
    }

    private lateinit var view: View
    private var contentView: View
    private var mTooltipTitle: TextView
    private var mTooltipDescription: TextView
    private var mImgPlayAudio: ImageView
    private var mImgClose: ImageView
    private var mNextButton: Button
    private var mImageArrow: TextView
    private var tipWindow: PopupWindow? = null
    private var inflater: LayoutInflater
    private var mIsAudioPlaying: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var mIsRotated = false

    companion object {
        private const val MSG_DISMISS_TOOLTIP = 5000
    }

    init {
        this.tipWindow = PopupWindow(context)
        inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: Int = when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM,
            GydeTooltipPosition.DRAW_BOTTOM_LEFT,
            GydeTooltipPosition.DRAW_BOTTOM_CENTER,
            GydeTooltipPosition.DRAW_BOTTOM_RIGHT -> R.layout.tooltip_bottom_layout

            GydeTooltipPosition.DRAW_TOP,
            GydeTooltipPosition.DRAW_TOP_LEFT,
            GydeTooltipPosition.DRAW_TOP_RIGHT,
            GydeTooltipPosition.DRAW_TOP_CENTER -> R.layout.tooltip_top_layout

            GydeTooltipPosition.DRAW_LEFT -> R.layout.tooltip_left_layout
            GydeTooltipPosition.DRAW_RIGHT -> R.layout.tooltip_right_layout
        }
        contentView = inflater.inflate(layout, null)
        mTooltipTitle = contentView.findViewById<View>(R.id.tooltip_title) as TextView
        mTooltipDescription = contentView.findViewById<View>(R.id.tv_tooltip_content) as TextView
        mImageArrow = contentView.findViewById<View>(R.id.tooltip_nav_up) as TextView
        mNextButton = contentView.findViewById<View>(R.id.next) as Button
        mImgPlayAudio = contentView.findViewById<View>(R.id.img_volume) as ImageView
        mImgClose = contentView.findViewById(R.id.img_close) as ImageView
        mNextButton.setBackgroundColor(Color.parseColor(Util.btnColor))
        setVolumeDrawable()
    }

    private fun setKeyboardVisibilityListener(
        onKeyboardVisibilityListener: OnKeyboardVisibilityListener,
        nextStepDescription: Int,
        tooltipPositionY: Int
    ) {
        val parentView =
            (contentView.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                private var alreadyOpen = false
                private val defaultKeyboardHeightDP = 100
                private val EstimatedKeyboardDP =
                    defaultKeyboardHeightDP + 48
                private val rect: Rect = Rect()
                override fun onGlobalLayout() {
                    val estimatedKeyboardHeight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        EstimatedKeyboardDP.toFloat(),
                        parentView.resources.displayMetrics
                    )
                        .toInt()
                    parentView.getWindowVisibleDisplayFrame(rect)
                    val heightDiff: Int = parentView.rootView.height - (rect.bottom - rect.top)
                    val isShown = heightDiff >= estimatedKeyboardHeight
                    if (isShown == alreadyOpen) {
                        Log.d("Keyboard state", "Ignoring global layout change...")
                        return
                    }
                    alreadyOpen = isShown
                    onKeyboardVisibilityListener.onVisibilityChanged(
                        isShown,
                        nextStepDescription,
                        tooltipPositionY
                    )
                }
            })
    }

    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DISMISS_TOOLTIP -> if (tipWindow != null && tipWindow!!.isShowing) tipWindow!!.dismiss()
            }
        }
    }

    /**
     * Show tooltip according the anchor point
     * @param nextStepDescription Int : requires to guess Next button or done button
     */
    fun showTooltip(nextStepDescription: Int) {
        if (!viewId.isNullOrEmpty()) {
            try {
                val resID = context.resources.getIdentifier(viewId, "id", context.packageName)
                view = (context as Activity).findViewById(resID) as View
            } catch (ex: Exception) {
                ex.stackTrace
                Log.d("resID", "Resource id not found")
                return
            }
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
            else -> {
                val layoutParams = LinearLayout.LayoutParams(height, height)
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 10, 1)
                mImageArrow.layoutParams = layoutParams
            }
        }

        tipWindow?.isOutsideTouchable = false
        tipWindow?.isTouchable = true
        tipWindow?.isFocusable = false
        tipWindow?.setBackgroundDrawable(null)
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

//        var positionX = 0
//        var positionY = 0
        when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM,
            GydeTooltipPosition.DRAW_BOTTOM_CENTER -> {
                Util.tooltipPositionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10
            }
            GydeTooltipPosition.DRAW_BOTTOM_LEFT -> {
                Util.tooltipPositionX = anchorRect.left
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.START
                    it.marginStart = 40
                }
            }
            GydeTooltipPosition.DRAW_BOTTOM_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right - contentViewWidth
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.END
                    it.marginEnd = 40
                }
            }

            GydeTooltipPosition.DRAW_TOP,
            GydeTooltipPosition.DRAW_TOP_CENTER -> {
                Util.tooltipPositionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                Util.tooltipPositionY = anchorRect.top - contentViewHeight
            }

            GydeTooltipPosition.DRAW_TOP_LEFT -> {
                Util.tooltipPositionX = anchorRect.left
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.START
                    it.marginStart = 40
                }
            }

            GydeTooltipPosition.DRAW_TOP_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right - contentViewWidth
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.END
                    it.marginEnd = 40
                }
            }

            GydeTooltipPosition.DRAW_LEFT -> {
                DRAW_RIGHT@ Util.tooltipPositionX = anchorRect.left - contentViewWidth - 30
                Util.tooltipPositionY = anchorRect.top
            }
            GydeTooltipPosition.DRAW_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right
                Util.tooltipPositionY = anchorRect.top
            }
        }
        tipWindow?.showAtLocation(
            view, Gravity.NO_GRAVITY, Util.tooltipPositionX,
            Util.tooltipPositionY
        )
        setDescriptionText()
        setVolumeDrawable()
        playAudio(voiceOverPath ?: "")
        initListeners(nextStepDescription, Util.tooltipPositionY)
    }

    /**
     * Show single tooltip from client input.
     * This will be triggered as per user input
     * On done button click it will only close the tooltip
     */
    fun showTooltipFromClientInput() {
        if (viewIdInt != null) {
            val resID = viewIdInt
            view = (context as Activity).findViewById(resID) as View
        }
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            25f,
            context.resources.displayMetrics
        ).toInt()
        when (GydeTooltipArrowPosition.ARROW_DEFAULT_CENTER) {
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
            else -> {
                val layoutParams = LinearLayout.LayoutParams(height, height)
                layoutParams.gravity = Gravity.CENTER
                layoutParams.setMargins(0, 0, 10, 1)
                mImageArrow.layoutParams = layoutParams
            }
        }

        tipWindow?.isOutsideTouchable = false
        tipWindow?.isTouchable = true
        tipWindow?.isFocusable = false
        tipWindow?.setBackgroundDrawable(null)
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

//        var positionX = 0
//        var positionY = 0
        when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM,
            GydeTooltipPosition.DRAW_BOTTOM_CENTER -> {
                Util.tooltipPositionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10
            }
            GydeTooltipPosition.DRAW_BOTTOM_LEFT -> {
                Util.tooltipPositionX = anchorRect.left
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.START
                    it.marginStart = 40
                }
            }
            GydeTooltipPosition.DRAW_BOTTOM_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right - contentViewWidth
                Util.tooltipPositionY = anchorRect.bottom - anchorRect.height() / 2 + 10

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.END
                    it.marginEnd = 40
                }
            }

            GydeTooltipPosition.DRAW_TOP,
            GydeTooltipPosition.DRAW_TOP_CENTER -> {
                Util.tooltipPositionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                Util.tooltipPositionY = anchorRect.top - contentViewHeight
            }

            GydeTooltipPosition.DRAW_TOP_LEFT -> {
                Util.tooltipPositionX = anchorRect.left
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.START
                    it.marginStart = 40
                }
            }

            GydeTooltipPosition.DRAW_TOP_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right - contentViewWidth
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.END
                    it.marginEnd = 40
                }
            }

            GydeTooltipPosition.DRAW_LEFT -> {
                DRAW_RIGHT@ Util.tooltipPositionX = anchorRect.left - contentViewWidth - 30
                Util.tooltipPositionY = anchorRect.top
            }
            GydeTooltipPosition.DRAW_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right
                Util.tooltipPositionY = anchorRect.top
            }
        }
        tipWindow?.showAtLocation(
            view, Gravity.NO_GRAVITY, Util.tooltipPositionX,
            Util.tooltipPositionY
        )
        setDescriptionText()
        setVolumeDrawable()
        playAudio(voiceOverPath ?: "")
        mImgPlayAudio.visibility = View.INVISIBLE
        mImgClose.setOnClickListener {
            tipWindow?.dismiss()
            Util.walkthroughSteps.clear()
            Util.stepCounter = 0
        }
        mNextButton.setOnClickListener {
            tipWindow?.dismiss()
            nextClickListener?.nextButtonClicked(context)
            unregisterKeyBoardEventListener()
            hideKeyboard((context as Activity))
        }
    }

    /**
     * Initialize all click listeners of tooltip.
     * @param nextStepDescription Int
     * @param tooltipPositionY Int
     */
    private fun initListeners(
        nextStepDescription: Int,
        tooltipPositionY: Int
    ) {
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

        mNextButton.setOnClickListener {
            tipWindow?.dismiss()
            nextClickListener?.nextButtonClicked(context)
            if (nextStepDescription == GydeStepDescription.OPEN_NEW_SCREEN.value) {
                (context as Activity).finish()
            }
            unregisterKeyBoardEventListener()
            hideKeyboard((context as Activity))
        }

        registerKeyBoardEventListener(nextStepDescription, tooltipPositionY)
//        setKeyboardVisibilityListener(this, nextStepDescription, tooltipPositionY)
        disableViewClickListener(nextStepDescription)
    }

    /**
     * Disable click listener if the next step is screen navigation
     * @param nextStepDescription Int
     */
    private fun disableViewClickListener(nextStepDescription: Int) {
        if (nextStepDescription == GydeStepDescription.OPEN_NEW_SCREEN.value) {
            view.isClickable = false
        }
    }

    private fun registerKeyBoardEventListener(
        nextStepDescription: Int,
        positionY: Int
    ) {
        try {

//            (context as Activity).let {
//                KeyboardVisibilityEvent.setEventListener(
//                    it
//                ) { isOpen ->
//                    if (isOpen) {
//                        Log.d("keyboard ", "Keyboard is opened")
//                        getWindowDimension(positionY, nextStepDescription)
//                    } else {
//                        Log.d("keyboard ", "Keyboard is closed")
//                        getWindowDimension(positionY, nextStepDescription)
//                    }
//                }
//            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getWindowDimension(positionY: Int, nextStepDescription: Int) {
        val outMetrics = DisplayMetrics()
        val currentDisplay: Display?

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            currentDisplay = (context as Activity).display
            currentDisplay?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            currentDisplay = (context as Activity).windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            currentDisplay.getMetrics(outMetrics)
        }

        val maxX: Int = outMetrics.widthPixels
        val maxY: Int = outMetrics.heightPixels
        Log.e("abc", "maxX : $maxX ::: maxY : $maxY")

        if (positionY > (maxY / 2) && !mIsRotated) {
            onKeyBoardOpenRotateToolTip(nextStepDescription)
        }
    }

    /**
     * This method will revert the bottom tooltip to top tooltip
     * only when keyboard is opened and keyboard is wrapping by tooltip.
     * @param nextStepDescription Int : the next button action is dependent on this status
     */
    private fun onKeyBoardOpenRotateToolTip(nextStepDescription: Int) {
        tipWindow?.dismiss()
        mIsRotated = true

        val layout = R.layout.tooltip_top_layout
        contentView = inflater.inflate(layout, null)
        mTooltipTitle = contentView.findViewById<View>(R.id.tooltip_title) as TextView
        mTooltipDescription = contentView.findViewById<View>(R.id.tv_tooltip_content) as TextView
        mImageArrow = contentView.findViewById<View>(R.id.tooltip_nav_up) as TextView
        mNextButton = contentView.findViewById<View>(R.id.next) as Button
        mImgPlayAudio = contentView.findViewById<View>(R.id.img_volume) as ImageView
        mImgClose = contentView.findViewById(R.id.img_close) as ImageView
        mNextButton.setBackgroundColor(Color.parseColor(Util.btnColor))
        setVolumeDrawable()

        tipWindow?.isOutsideTouchable = false
        tipWindow?.isTouchable = true
        tipWindow?.isFocusable = false
        tipWindow?.setBackgroundDrawable(null)
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

//        var positionX = 0
//        var positionY = 0
        when (toolTipPosition) {
            GydeTooltipPosition.DRAW_BOTTOM,
            GydeTooltipPosition.DRAW_BOTTOM_CENTER -> {
                Util.tooltipPositionX = anchorRect.centerX() - (contentViewWidth - contentViewWidth / 2)
                Util.tooltipPositionY = anchorRect.top - contentViewHeight
            }

            GydeTooltipPosition.DRAW_BOTTOM_LEFT -> {
                Util.tooltipPositionX = anchorRect.left
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.START
                    it.marginStart = 40
                }
            }

            GydeTooltipPosition.DRAW_BOTTOM_RIGHT -> {
                Util.tooltipPositionX = anchorRect.right - contentViewWidth
                Util.tooltipPositionY = anchorRect.top - contentViewHeight

                (mImageArrow.layoutParams as LinearLayout.LayoutParams).let {
                    it.gravity = Gravity.END
                    it.marginEnd = 40
                }
            }

            else -> {
                // not required...
            }
        }
        tipWindow?.showAtLocation(
            view, Gravity.NO_GRAVITY, Util.tooltipPositionX,
            Util.tooltipPositionY
        )
        setDescriptionText()
        setVolumeDrawable()

        initListeners(nextStepDescription, Util.tooltipPositionY)
        showEditTextFocus()
        unregisterKeyBoardEventListener()
    }

    private fun showEditTextFocus() {
        if (view is EditText || view is AppCompatEditText) {
            view.requestFocus()
        }
    }

    /**
     * Unregister the keyboard open listener
     */
    private fun unregisterKeyBoardEventListener() {
//        val unRegistrar = registerEventListener(
//            (context as Activity),
//            KeyboardVisibilityEventListener {
//                // some code depending on keyboard visibility status
//            }
//        )
//
//        unRegistrar.unregister()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Play audio voice over path provided from json.
     * It plays the URL string
     * @param voiceOverPath String : URL string received from json from playing sound
     */
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

    private fun setVolumeDrawable() {
        if (Util.isPlayVoiceOverEnabled) {
            mImgPlayAudio.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.gyde_ic_volume_up_24
                )
            )
        } else {
            mImgPlayAudio.setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.gyde_ic_volume_off_24
                )
            )
        }
    }

    private fun setDescriptionText() {
        mTooltipTitle.text = String.format("%s", titleText)
        mTooltipTitle.setTextColor(Color.parseColor(Util.btnColor))
        mTooltipDescription.text = String.format("%s", descriptionText)
        mNextButton.text = String.format("%s", buttonText)
    }

    override fun onVisibilityChanged(
        visible: Boolean,
        nextStepDescription: Int,
        tooltipPositionY: Int
    ) {
        if (visible) {
            Log.d("keyboard tooltip window", "Keyboard is opened")
            getWindowDimension(tooltipPositionY, nextStepDescription)
        } else {
            Log.d("keyboard tooltip window", "Keyboard is closed")
            getWindowDimension(tooltipPositionY, nextStepDescription)
        }
    }
}
