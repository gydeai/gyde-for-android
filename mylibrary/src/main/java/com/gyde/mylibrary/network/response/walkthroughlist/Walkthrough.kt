package com.gyde.mylibrary.network.response.walkthroughlist

data class Walkthrough(
    val firstDom: String,
    val flowId: String,
    val flowName: String,
    val order: Int,
    val type: String,
    val videoOnlyFlag: Boolean,
    val walkthroughVideoLink: String
)