package com.gyde.mylibrary.screens

import android.app.Dialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.gyde.library.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.R
import com.gyde.mylibrary.adapter.ViewPagerAdapter
import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.mylibrary.utils.Util
import kotlinx.android.synthetic.main.activity_gyde_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GydeHomeActivity : AppCompatActivity() {
    private var gydeApiKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gyde_home)

        getGydeAppKey()
        getWalkthroughListApiCall()
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

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

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

    private fun getWalkthroughListApiCall() {

        if (!isNetworkAvailable(this@GydeHomeActivity)) {
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
                            }
                        }
                    }

                    override fun onFailure(call: Call<WalkthroughsListResponse>, t: Throwable) {
                        progressBar_cyclic!!.visibility = View.GONE
                        Toast.makeText(this@GydeHomeActivity, "${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }
}