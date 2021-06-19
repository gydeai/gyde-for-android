package com.gyde.library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.gyde.library.adapter.ViewPagerAdapter
import com.gyde.library.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.library.screens.HelpArticlesFragment
import com.gyde.library.screens.WalkthroughFragment
import com.gyde.library.utils.JsonUtils
import kotlinx.android.synthetic.main.activity_gyde_home.*

class GydeHomeActivity : AppCompatActivity() {
    private var dummyJson: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gyde_home)

        setUpTitle()
        setUpViewPager()
        setUpTabLayout()
    }

    private fun setUpTitle() {
        dummyJson = JsonUtils.getJsonDataFromAsset(
            this@GydeHomeActivity,
            "walkthrough.json"
        )

        val response = Gson().fromJson(dummyJson, WalkthroughsListResponse::class.java)
        tv_greeting.text = String.format("%s", response.welcomeGreeting)
        tv_company_name.text = String.format("%s", response.appName)
    }

    private fun setUpTabLayout() {
        val response = Gson().fromJson(dummyJson, WalkthroughsListResponse::class.java)
        tabLayout.addTab(tabLayout.newTab().setText(response.walkthroughTabText))
        tabLayout.addTab(tabLayout.newTab().setText(response.helpArticlesTabText))

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
}