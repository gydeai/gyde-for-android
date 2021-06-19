package com.gyde.mylibrary.listener

import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle

interface HelpArticleListener {
    fun onHelpArticleClicked(helpArticle: HelpArticle)
}