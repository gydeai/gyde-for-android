package com.gyde.mylibrary.screens

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.gyde.mylibrary.R
import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.utils.Util
import kotlinx.android.synthetic.main.activity_help_article_details.*

internal class HelpArticleDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_article_details)

        getIntentData()
        img_back.setOnClickListener {
            this@HelpArticleDetailsActivity.finish()
        }
        img_back.setColorFilter(Color.parseColor(Util.headerTextColor))
        tv_title.setTextColor(Color.parseColor(Util.headerTextColor))
        layout_welcome.setBackgroundColor(Color.parseColor(Util.headerColor))
    }

    private fun getIntentData() {
        val helpArticleJson = intent.getStringExtra("helpArticleJson")
        val helpArticle = Gson().fromJson(helpArticleJson, HelpArticle::class.java)

        tv_title.text = helpArticle.question
        web_view.loadUrl(helpArticle.urlForMobileWebView)
    }
}