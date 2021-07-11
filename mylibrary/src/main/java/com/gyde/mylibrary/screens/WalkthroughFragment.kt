package com.gyde.mylibrary.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyde.mylibrary.R
import com.gyde.mylibrary.adapter.WalkthroughAdapter
import com.gyde.mylibrary.listener.WalkthroughListeners
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.response.walkthroughsteps.WalkthroughStepsResponse
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.mylibrary.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.utils.*
import kotlinx.android.synthetic.main.tab_layout_1.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

internal class WalkthroughFragment :
    Fragment(),
    WalkthroughListeners,
    CustomDialogGuideInformation.GuideInformationDialogListener,
    GydeTooltipWindow.TooTipClickListener {
    private lateinit var mAdapter: WalkthroughAdapter
    private var gydeApiKey: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.tab_layout_1, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = WalkthroughAdapter(Util.walkthroughList, this)
        val layoutManager = LinearLayoutManager(requireContext())
        recycler_walkthrough_list.layoutManager = layoutManager
        recycler_walkthrough_list.itemAnimator = DefaultItemAnimator()
        recycler_walkthrough_list.adapter = mAdapter

        getAppIdFromManifest()
        showWalkthroughList()
        initListeners()
    }

    private fun initListeners() {
        layout_branding.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://gyde.ai")
            startActivity(intent)
        }

        edt_search.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
    }

    private fun getAppIdFromManifest() {
        try {
            val applicationInfo: ApplicationInfo =
                requireContext().packageManager.getApplicationInfo(
                    requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
            val bundle = applicationInfo.metaData
            gydeApiKey = bundle.getString("GYDE_APP_ID") ?: ""
            Util.appId = gydeApiKey
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showWalkthroughList() {
        if (Util.walkthroughList.isNotEmpty()) {
            var filteredWalkthrough = mutableListOf<Walkthrough>()
            for (item in Util.walkthroughList) {
                if (item.language == Util.selectedLanguage) {
                    filteredWalkthrough.add(item)
                }
            }

            mAdapter.updateData(filteredWalkthrough)
        } else {
            getWalkthroughListApiCall()
        }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<Walkthrough> = ArrayList()
        for (item in Util.walkthroughList) {
            if (item.flowName.lowercase()
                    .contains(text.lowercase()) && item.language.equals(Util.selectedLanguage, true)
            ) {
                filteredList.add(item)
            }
        }
        mAdapter.filterList(filteredList)
    }

    private fun showInternetConnectivityDialog() {
        val dialog = Dialog(requireContext())
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

    /**
     * Get walkthrough list from server
     */
    private fun getWalkthroughListApiCall() {

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            progressBar_cyclic!!.visibility = View.GONE
            showInternetConnectivityDialog()
        } else {
            progressBar_cyclic!!.visibility = View.VISIBLE
            val request = ServiceBuilder.buildService(WalkthroughListInterface::class.java)

            request.getWalkthroughList(gydeApiKey)
                .enqueue(object : Callback<WalkthroughsListResponse> {
                    override fun onResponse(
                        call: Call<WalkthroughsListResponse>,
                        response: Response<WalkthroughsListResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                Util.helpArticle = it.helpArticles
                                Util.walkthroughList = it.walkthroughs
                                showWalkthroughList()
                            }
                        }
                        progressBar_cyclic!!.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<WalkthroughsListResponse>, t: Throwable) {
                        progressBar_cyclic!!.visibility = View.GONE
                        Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.isDeepLink) {
            getWalkthroughSteps(Util.deepLinkData)
            Util.isDeepLink = false
        }
    }

    internal fun updateLanguageSelection(selectedLanguage: String) {
        var newLanguageList = mutableListOf<Walkthrough>()
        for (item in Util.walkthroughList) {
            if (item.language.equals(selectedLanguage, true)) {
                newLanguageList.add(item)
            }
        }
        mAdapter.updateData(newLanguageList)
    }

    companion object {
        fun newInstance() = WalkthroughFragment()
    }

    override fun onGuideMeClicked(flowId: String) {
        getWalkthroughSteps(flowId)
    }

    /**
     * Get walkthrough steps by flowId
     * @param flowId String : Selected flowId by user
     */
    private fun getWalkthroughSteps(flowId: String) {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            progressBar_cyclic!!.visibility = View.GONE
            showInternetConnectivityDialog()
        } else {
            progressBar_cyclic!!.visibility = View.VISIBLE
            val request = ServiceBuilder.buildService(WalkthroughListInterface::class.java)

            request.getWalkthroughSteps(gydeApiKey, flowId)
                .enqueue(object : Callback<WalkthroughStepsResponse> {
                    override fun onResponse(
                        call: Call<WalkthroughStepsResponse>,
                        response: Response<WalkthroughStepsResponse>
                    ) {
                        progressBar_cyclic!!.visibility = View.GONE
                        if (response.isSuccessful) {
                            response.body()?.let {

                                Util.walkthroughSteps.clear()
                                Util.stepCounter = 0
                                Util.isPlayVoiceOverEnabled = false

                                Util.walkthroughSteps = it.steps.toMutableList()
                                CustomDialogGuideInformation(
                                    requireContext(),
                                    this@WalkthroughFragment,
                                    it.flowName,
                                    it.flowInitText,
                                    it.steps.size
                                ).show()

                                saveLog(flowId)
                            }
                        }
                        progressBar_cyclic!!.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<WalkthroughStepsResponse>, t: Throwable) {
                        progressBar_cyclic!!.visibility = View.GONE
                        Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onPlayVideoClicked() {
        // this will be added once play video integrated on web portal
    }

    override fun onStartGuideClicked() {
        navigateToFirstScreen()
    }

    private fun navigateToFirstScreen() {
        val activityToStart = Util.walkthroughSteps[0].screenName
        try {
            val c = Class.forName(activityToStart)
            val intent = Intent(requireContext(), c)
            startActivity(intent)
        } catch (ignored: ClassNotFoundException) {
            ignored.printStackTrace()
        }
        incrementCounter()
        showToolTip(1000)
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

    override fun nextButtonClicked() {
        if (Util.stepCounter < Util.walkthroughSteps.size) {
            when (Util.walkthroughSteps[Util.stepCounter].stepDescription) {
                GydeStepDescription.SHOW_TOOLTIP.value -> showToolTip(0)
                GydeStepDescription.OPEN_NEW_SCREEN.value -> navigateToNextScreen()
                GydeStepDescription.OPEN_DRAWER_MENU.value -> openDrawerMenu()
            }
        } else {
            Util.stepCounter = 0
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
                    this,
                    Util.walkthroughSteps[Util.stepCounter].voiceOverPath
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

    /**
     * Navigate to next screen as per the walkthrough steps
     */
    private fun navigateToNextScreen() {
        val activityToStart = Util.walkthroughSteps[Util.stepCounter].screenName
        try {
            val c = Class.forName(activityToStart)
            val intent = Intent(requireContext(), c)
            startActivity(intent)
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
     * This function will open the drawer menu
     * from the activity
     */
    private fun openDrawerMenu() {
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
                    this,
                    Util.walkthroughSteps[Util.stepCounter].voiceOverPath
                )
                tipWindow.openDrawerMenu()
                incrementCounter()
            },
            500
        )
    }

    /**
     * Save user log to server when user starts any walkthrough.
     * @param flowId String
     */
    private fun saveLog(flowId: String) {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showInternetConnectivityDialog()
        } else {
            val request = ServiceBuilder.buildService(WalkthroughListInterface::class.java)

            request.saveUserLog(
                appId = gydeApiKey,
                flowID = flowId,
                timestamp = System.currentTimeMillis(),
                type = "runFlowLog",
                uuid = Util.getUuid(requireContext()),
                url = "",
                source = "android",
                activityName = getFirstActivityName()
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.e("log", "" + response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getFirstActivityName(): String {
        for (item in Util.walkthroughSteps) {
            if (item.stepDescription == GydeStepDescription.OPEN_NEW_SCREEN.value) {
                return item.screenName
            }
        }
        return ""
    }
}