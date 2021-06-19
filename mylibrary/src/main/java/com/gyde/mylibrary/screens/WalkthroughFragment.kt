package com.gyde.mylibrary.screens

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import com.google.gson.Gson
import com.gyde.mylibrary.adapter.WalkthroughAdapter
import com.gyde.mylibrary.listener.WalkthroughListeners
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.response.walkthroughsteps.WalkthroughStepsResponse
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.library.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.R
import com.gyde.mylibrary.utils.*
import kotlinx.android.synthetic.main.tab_layout_1.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class WalkthroughFragment : Fragment(), WalkthroughListeners,
    CustomDialogGuideInformation.GuideInformationDialogListener,
    GydeTooltipWindow.TooTipClickListener {
    private val walkthroughList = ArrayList<Walkthrough>()
    private var walkthroughListNew = ArrayList<Walkthrough>()
    private lateinit var mAdapter: WalkthroughAdapter
    var activityList: List<Activity> = arrayListOf()
    var gydeApiKey: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.tab_layout_1, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = WalkthroughAdapter(walkthroughList, this)
        val layoutManager = LinearLayoutManager(requireContext())
        recycler_walkthrough_list.layoutManager = layoutManager
        recycler_walkthrough_list.itemAnimator = DefaultItemAnimator()
        recycler_walkthrough_list.adapter = mAdapter

        try {
            val ai: ApplicationInfo =
                requireContext().packageManager.getApplicationInfo(
                    requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
            val bundle = ai.metaData
            gydeApiKey = bundle.getString("GYDE_APP_ID") ?: ""

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        getWalkthroughListApiCall()
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

    private fun filter(text: String) {
        val filteredList: ArrayList<Walkthrough> = ArrayList()
        for (item in walkthroughListNew) {
            if (item.flowName.lowercase().contains(text.lowercase())) {
                filteredList.add(item)
            }
        }
        mAdapter.filterList(filteredList)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    private fun showDialog() {
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

    private fun getWalkthroughListApiCall() {

        if (!isNetworkAvailable(requireContext())) {
            progressBar_cyclic!!.visibility = View.GONE
            showDialog()
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
                                mAdapter.updateData(it.walkthroughs)
                                Util.helpArticle = it.helpArticles
                                walkthroughListNew = it.walkthroughs
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

    private fun setUpData() {
        val dummyJson = JsonUtils.getJsonDataFromAsset(
            requireContext(),
            "walkthroughSteps.json"
        )

        var response = Gson().fromJson(dummyJson, WalkthroughStepsResponse::class.java)
        Util.walkthroughSteps = response.steps
    }

    companion object {
        fun newInstance() = WalkthroughFragment()
    }

    override fun onGuideMeClicked(flowId: String) {
        getWalkthroughSteps(flowId)
    }

    private fun getWalkthroughSteps(flowId: String) {
        if (!isNetworkAvailable(requireContext())) {
            progressBar_cyclic!!.visibility = View.GONE
            Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show()
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
                                it.steps
                                CustomDialogGuideInformation(
                                    requireContext(),
                                    this@WalkthroughFragment,
                                    it.flowName,
                                    it.flowInitText,
                                    it.steps.size
                                ).show()
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

    }

    override fun onStartGuideClicked() {
        setUpData()
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
        Util.stepCounter += 1
        showToolTip(1000)
    }

    private val runningActivity: Activity
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

    private fun getActivityName(activity: Activity): String {
        val packageManager = activity.packageManager
        try {
            val info = packageManager.getActivityInfo(activity.componentName, 0)
            Log.e("app", "Activity name:" + info.name)
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
     * This function will open the drawer menu
     * from the activity
     */
    private fun openDrawerMenu() {
        Handler(Looper.getMainLooper()).postDelayed({
            val tipWindow = GydeTooltipWindow(
                runningActivity,
                GydeTooltipPosition.DRAW_BOTTOM,
                Util.walkthroughSteps[Util.stepCounter].viewId,
                Util.walkthroughSteps[Util.stepCounter].title,
                Util.walkthroughSteps[Util.stepCounter].content,
                if (Util.stepCounter == (Util.walkthroughSteps.size - 1)) {
                    "Done"
                } else {
                    "Next"
                },
                this
            )
            tipWindow.openDrawerMenu()
            Util.stepCounter += 1
        }, 500)
    }

    /**
     * This function will show the tooltip according the view id
     * @param delay Long : If new screen is opening then give some delay to open the new screen
     */
    private fun showToolTip(delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            val tipWindow = GydeTooltipWindow(
                runningActivity,
                GydeTooltipPosition.DRAW_TOP,
                Util.walkthroughSteps[Util.stepCounter].viewId,
                Util.walkthroughSteps[Util.stepCounter].title,
                Util.walkthroughSteps[Util.stepCounter].content,
                if (Util.stepCounter == (Util.walkthroughSteps.size - 1)) {
                    "Done"
                } else {
                    "Next"
                },
                this
            )
            tipWindow.showTooltip(Util.walkthroughSteps[Util.stepCounter + 1].stepDescription)
            Util.stepCounter += 1
        }, delay)
    }

    private fun navigateToNextScreen() {
        val activityToStart = Util.walkthroughSteps[Util.stepCounter].screenName
        try {
            val c = Class.forName(activityToStart)
            val intent = Intent(requireContext(), c)
            startActivity(intent)
        } catch (ignored: ClassNotFoundException) {
            ignored.printStackTrace()
        }
        if (Util.stepCounter < Util.walkthroughSteps.size) {
            Util.stepCounter += 1
        }
        try {
            if (Util.walkthroughSteps[Util.stepCounter].stepDescription == GydeStepDescription.SHOW_TOOLTIP.value) {
                showToolTip(1000)
            }
            if (Util.walkthroughSteps[Util.stepCounter+1].stepDescription == GydeStepDescription.OPEN_DRAWER_MENU.value) {
                openDrawerMenu()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }
}