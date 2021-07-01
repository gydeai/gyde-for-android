package com.gyde.mylibrary.utils

import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.network.response.walkthroughsteps.Step

class Util {
    companion object{
        lateinit var helpArticle: List<HelpArticle>
        lateinit var walkthroughList: List<Walkthrough>
        var walkthroughSteps = mutableListOf<Step>()
        var stepCounter = 0
        var isPlayVoiceOverEnabled = false
        var headerColor = ""
        var headerTextColor = ""
        var btnColor = ""
        var deepLinkData: String = ""
        var isDeepLink = false
    }
}