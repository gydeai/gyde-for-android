package com.gyde.library.listener

import com.gyde.library.network.response.walkthroughlist.HelpArticle

interface HelpArticleListener {
    fun onHelpArticleClicked(helpArticle: HelpArticle)
}