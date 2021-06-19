package com.gyde.library.network.response.walkthroughlist

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