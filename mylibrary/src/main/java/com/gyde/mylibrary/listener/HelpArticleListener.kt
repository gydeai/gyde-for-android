package com.gyde.mylibrary.listener

import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle

internal interface HelpArticleListener {
    fun onHelpArticleClicked(helpArticle: HelpArticle)
}