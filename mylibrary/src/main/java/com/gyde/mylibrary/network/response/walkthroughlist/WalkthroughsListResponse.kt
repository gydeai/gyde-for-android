package com.gyde.mylibrary.network.response.walkthroughlist

import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough

data class WalkthroughsListResponse(
    val adminName: String,
    val appName: String,
    val btnColor: String,
    val headerColor: String,
    val headerTextColor: String,
    val helpArticles: List<HelpArticle>,
    val helpArticlesTabText: String,
    val languageOptions: List<String>,
    val uiType: String,
    val walkthroughTabText: String,
    val walkthroughs: ArrayList<Walkthrough>,
    val welcomeGreeting: String
)