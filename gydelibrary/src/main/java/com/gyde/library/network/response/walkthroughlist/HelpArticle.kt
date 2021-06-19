package com.gyde.library.network.response.walkthroughlist

data class HelpArticle(
    val category: String,
    val draftMode: Int,
    val language: String,
    val order: Int,
    val queId: String,
    val question: String,
    val urlForMobileWebView: String,
    val response: List<Response>,
    val role: String,
    val timeStamp: Long,
    val type: String,
    val url: String
)