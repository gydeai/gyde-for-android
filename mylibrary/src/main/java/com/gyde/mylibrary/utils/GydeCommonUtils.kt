package com.gyde.mylibrary.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.gyde.mylibrary.R
import com.gyde.mylibrary.network.response.walkthroughsteps.WalkthroughStepsResponse
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.mylibrary.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.screens.CustomDialogGuideInformation
import kotlinx.android.synthetic.main.activity_gyde_home.*
import kotlinx.android.synthetic.main.tab_layout_1.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object GydeInternalCommonUtils :
    CustomDialogGuideInformation.GuideInformationDialogListener,
    GydeTooltipWindow.ToolTipClickListener {

    private var alertDialog: AlertDialog? = null

    /**
     * Get Gyde app key specified in Main application manifest file
     * This will be required for authentication from server side.
     * @param activity Activity : current activity
     * @param packageName String : current activity package name
     * @return String : returns gyde api key in String type.
     */
    fun getGydeAppKey(activity: Activity, packageName: String): String {
        return try {
            val ai: ApplicationInfo =
                activity.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            val bundle = ai.metaData
            bundle.getString(Util.keyGydeAppId) ?: ""
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    private fun showProgressDialog(context: Context) {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = context.getString(R.string.loading)
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        val typeface: Typeface? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.resources.getFont(R.font.proxima_nova)
        } else {
            ResourcesCompat.getFont(context, R.font.proxima_nova)
        }
        tvText.typeface = typeface
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        alertDialog = builder.create()
        alertDialog?.show()
        val window: Window? = alertDialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(alertDialog?.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            alertDialog?.window?.attributes = layoutParams
        }
    }

    private fun hideProgressBar() {
        if (alertDialog != null) {
            alertDialog?.dismiss()
            alertDialog = null
        }
    }

    /**
     * Show internet connectivity dialog if network not available
     */
    private fun showInternetConnectivityDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_internet_not_available)
        val yesBtn: TextView = dialog.findViewById(R.id.tvOk)
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun getWalkthroughStepsFromId(context: Context, flowId: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            hideProgressBar()
            showInternetConnectivityDialog(context)
            return
        }

        val request = ServiceBuilder.buildService(WalkthroughListInterface::class.java)
        showProgressDialog(context)
        request.getWalkthroughSteps(getGydeAppKey(context as Activity, context.packageName), flowId)
            .enqueue(object : Callback<WalkthroughStepsResponse> {
                override fun onResponse(
                    call: Call<WalkthroughStepsResponse>,
                    response: Response<WalkthroughStepsResponse>
                ) {
                    hideProgressBar()
                    if (response.isSuccessful) {
                        response.body()?.let {

                            Util.walkthroughSteps.clear()
                            Util.stepCounter = 0
//                            Util.isPlayVoiceOverEnabled = false

                            Util.walkthroughSteps = it.steps.toMutableList()
                            CustomDialogGuideInformation(
                                context,
                                this@GydeInternalCommonUtils,
                                it.flowName,
                                it.flowInitText,
                                it.steps.size
                            ).show()

//                            saveLog(flowId)
                        }
                    }
                }

                override fun onFailure(call: Call<WalkthroughStepsResponse>, t: Throwable) {
                    hideProgressBar()
                    Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onStartGuideClicked(context: Context) {
        navigateToFirstScreen(context)
    }

    private fun navigateToFirstScreen(context: Context) {
        val activityToStart = Util.walkthroughSteps[0].screenName
        try {
            val className = Class.forName(activityToStart)
            val intent = Intent(context, className)
            context.startActivity(intent)
        } catch (ignored: ClassNotFoundException) {
            ignored.printStackTrace()
        }
        incrementCounter()
        showToolTip(1000)
    }

    /**
     * This method will increment the step counter.
     * and save it to Util.stepCounter
     */
    private fun incrementCounter() {
        if (Util.stepCounter < Util.walkthroughSteps.size) {
            Util.stepCounter += 1
        }
    }

    /**
     * Get current tooltip position like top or bottom
     * left and right will be implemented later on
     * @return GydeTooltipPosition
     */
    private fun getToolTipPosition(): GydeTooltipPosition {
        return when (Util.walkthroughSteps[Util.stepCounter].placement) {
            "top" -> GydeTooltipPosition.DRAW_TOP
            "bottom" -> GydeTooltipPosition.DRAW_BOTTOM
            "bottom left" -> GydeTooltipPosition.DRAW_BOTTOM_LEFT
            "bottom right" -> GydeTooltipPosition.DRAW_BOTTOM_RIGHT
            "bottom center" -> GydeTooltipPosition.DRAW_BOTTOM_CENTER
            "top left" -> GydeTooltipPosition.DRAW_TOP_LEFT
            "top right" -> GydeTooltipPosition.DRAW_TOP_RIGHT
            "top center" -> GydeTooltipPosition.DRAW_TOP_CENTER
            else -> GydeTooltipPosition.DRAW_BOTTOM_CENTER
        }
    }

    /**
     * This function will show the tooltip according the view id
     * @param delay Long : If new screen is opening then give some delay to open the new screen
     */
    private fun showToolTip(delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val tipWindow = GydeTooltipWindow(
                    runningActivity,
                    getToolTipPosition(),
                    Util.walkthroughSteps[Util.stepCounter].viewId,
                    Util.walkthroughSteps[Util.stepCounter].title,
                    Util.walkthroughSteps[Util.stepCounter].content,
                    if (Util.stepCounter == (Util.walkthroughSteps.size - 1)) {
                        "Done"
                    } else {
                        "Next"
                    },
                    this@GydeInternalCommonUtils,
                    Util.walkthroughSteps[Util.stepCounter].voiceOverPath,
                    null
                )
                tipWindow.showTooltip(
                    if (Util.stepCounter < Util.walkthroughSteps.size) {
                        if (Util.stepCounter == (Util.walkthroughSteps.size - 1)) {
                            3
                        } else {
                            Util.walkthroughSteps[Util.stepCounter + 1].stepDescription
                        }
                    } else {
                        2
                    }
                )
                incrementCounter()
            },
            delay
        )
    }

    private val runningActivity: Activity
        @SuppressLint("PrivateApi")
        get() {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val activityThread = activityThreadClass.getMethod("currentActivityThread")
                    .invoke(null)
                val activitiesField = activityThreadClass.getDeclaredField("mActivities")
                activitiesField.isAccessible = true
                val activities = activitiesField[activityThread] as ArrayMap<*, *>
                for (activityRecord in activities.values) {
                    val activityRecordClass: Class<*> = activityRecord.javaClass
                    val pausedField = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField = activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        getActivityName(activityField[activityRecord] as Activity)
                        return activityField[activityRecord] as Activity
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            throw RuntimeException("Didn't find the running activity")
        }

    /**
     * Get current opened activity name.
     * @param activity Activity : current opened activity instance
     * @return String : returns the activity class name.
     */
    private fun getActivityName(activity: Activity): String {
        val packageManager = activity.packageManager
        try {
            val info = packageManager.getActivityInfo(activity.componentName, 0)
            return info.name
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun nextButtonClicked(context: Context) {
        if (Util.stepCounter < Util.walkthroughSteps.size) {
            when (Util.walkthroughSteps[Util.stepCounter].stepDescription) {
                GydeStepDescription.SHOW_TOOLTIP.value -> showToolTip(0)
                GydeStepDescription.OPEN_NEW_SCREEN.value -> navigateToNextScreen(context)
//                GydeStepDescription.OPEN_DRAWER_MENU.value -> openDrawerMenu()
            }
        } else {
            Util.stepCounter = 0
        }
    }

    /**
     * Navigate to next screen as per the walkthrough steps
     */
    private fun navigateToNextScreen(context: Context) {
        val activityToStart = Util.walkthroughSteps[Util.stepCounter].screenName
        try {
            val c = Class.forName(activityToStart)
            val intent = Intent(context, c)
            context.startActivity(intent)
        } catch (ignored: ClassNotFoundException) {
            ignored.printStackTrace()
        }
        incrementCounter()
        try {
            if (Util.walkthroughSteps[Util.stepCounter].stepDescription == GydeStepDescription.SHOW_TOOLTIP.value) {
                showToolTip(1000)
            }
            if (Util.walkthroughSteps[Util.stepCounter + 1].stepDescription == GydeStepDescription.OPEN_DRAWER_MENU.value) {
                openDrawerMenu()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun openDrawerMenu() {
        // TODO: Open drawer functionality will be added
    }
}
