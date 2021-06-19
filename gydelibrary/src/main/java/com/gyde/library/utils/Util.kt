package com.gyde.library.utils

import com.gyde.library.network.response.walkthroughlist.HelpArticle
import com.gyde.library.network.response.walkthroughsteps.Step

class Util {
    companion object{
        lateinit var helpArticle: List<HelpArticle>
        lateinit var walkthroughSteps: List<Step>
        var stepCounter = 0
    }
}