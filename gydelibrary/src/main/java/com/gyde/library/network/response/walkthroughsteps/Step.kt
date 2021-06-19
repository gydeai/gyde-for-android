package com.gyde.library.network.response.walkthroughsteps

data class Step(
    val content: String,
    val delay: Int,
    val domSelector: String,
    val frame: String,
    val gyClickFlag: Boolean,
    val gyHoverFlag: Boolean,
    val placement: String,
    val screenName: String,
    val stepUrl: String,
    val title: String,
    val stepDescription: Int,
    val viewId: String,
    val voiceOverPath: String,
    val width: Int
)