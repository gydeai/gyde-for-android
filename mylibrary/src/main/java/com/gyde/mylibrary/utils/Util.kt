package com.gyde.mylibrary.utils

import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.network.response.walkthroughsteps.Step

class Util {
    companion object{
        lateinit var helpArticle: List<HelpArticle>
        lateinit var walkthroughSteps: List<Step>
        var stepCounter = 0
    }
}