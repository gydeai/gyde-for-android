package com.gyde.mylibrary.screens

import android.app.Dialog
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.gyde.mylibrary.R
import com.gyde.mylibrary.adapter.ViewPagerAdapter
import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.mylibrary.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.utils.NetworkUtils
import com.gyde.mylibrary.utils.Util
import kotlinx.android.synthetic.main.activity_gyde_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class GydeHomeActivity : AppCompatActivity() {
    private var gydeApiKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gyde_home)
        supportActionBar?.hide()
        getIntentData()
        getGydeAppKey()
        getWalkthroughListApiCall()
        initializeListeners()

    }

    private fun initializeListeners() {
        img_menu.setOnClickListener {
            val menu = PopupMenu(this@GydeHomeActivity, it)
            for (s in Util.OPTIONS_MENU) {
                menu.menu.add(s)
            }
            menu.show()
        }
    }

    private fun getIntentData() {
        Util.deepLinkData = try {
            intent.getStringExtra("GYDE_DEEP_LINK") ?: ""
        } catch (ex: java.lang.Exception) {
            ""
        }
        Util.isDeepLink = Util.deepLinkData.isNotEmpty()
    }

    /**
     * Get Gyde app key from manifest
     */
    private fun getGydeAppKey() {
        try {
            val ai: ApplicationInfo =
                this@GydeHomeActivity.packageManager.getApplicationInfo(
                    this.packageName,
                    PackageManager.GET_META_DATA
                )
            val bundle = ai.metaData
            gydeApiKey = bundle.getString("GYDE_APP_ID") ?: ""
            Log.e("Key", "Api Key : $gydeApiKey")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun setUpTitle(welcomeGreeting: String, appName: String) {
        tv_greeting.text = String.format("%s", welcomeGreeting)
        tv_company_name.text = String.format("%s", appName)
    }

    private fun setUpTabLayout(walkthroughTabText: String, helpArticlesTabText: String) {
        tabLayout.addTab(tabLayout.newTab().setText(walkthroughTabText))
        tabLayout.addTab(tabLayout.newTab().setText(helpArticlesTabText))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pager.setCurrentItem(tab!!.position, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //this will not require
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //this will not require
            }

        })
    }

    private fun setUpViewPager() {
        val fragmentList = arrayListOf(
            WalkthroughFragment.newInstance(),
            HelpArticlesFragment.newInstance()
        )
        pager.adapter = ViewPagerAdapter(this, fragmentList)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }

    /**
     * Show internet connectivity dialog if network not available
     */
    private fun showDialog() {
        val dialog = Dialog(this@GydeHomeActivity)
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
     * Get all walkthrough list api
     * in this api we will get walkthrough list and help article list
     */
    private fun getWalkthroughListApiCall() {

        if (!NetworkUtils.isNetworkAvailable(this@GydeHomeActivity)) {
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
                        progressBar_cyclic!!.visibility = View.GONE
                        if (response.isSuccessful) {
                            response.body()?.let {
                                if (!it.walkthroughs.isNullOrEmpty()) {
                                    Util.walkthroughList = it.walkthroughs
                                    setUpTitle(it.welcomeGreeting, it.appName)
                                    setUpViewPager()
                                    setUpTabLayout(it.walkthroughTabText, it.helpArticlesTabText)
                                }
                                if (!it.helpArticles.isNullOrEmpty()) {
                                    Util.helpArticle = it.helpArticles
                                }
                                setBackgroundColor(it.headerColor, it.headerTextColor, it.btnColor)
                            }
                        }
                    }

                    override fun onFailure(call: Call<WalkthroughsListResponse>, t: Throwable) {
                        progressBar_cyclic!!.visibility = View.GONE
                        Toast.makeText(
                            this@GydeHomeActivity, "${t.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }
    }

    /**
     * Set background color of activity
     * @param headerColor String : This is header background color
     * @param headerTextColor String : This is text color of header
     * @param btnColor String : This color will be used for all over the app for buttons
     */
    private fun setBackgroundColor(headerColor: String, headerTextColor: String, btnColor: String) {
        Util.headerColor = headerColor
        Util.headerTextColor = headerTextColor
        Util.btnColor = btnColor

        layout_welcome.setBackgroundColor(Color.parseColor(headerColor))
        tv_greeting.setTextColor(Color.parseColor(headerTextColor))
        img_menu.setColorFilter(Color.parseColor(headerTextColor))
    }
}