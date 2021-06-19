package com.gyde.mylibrary.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.gyde.mylibrary.adapter.HelpArticleAdapter
import com.gyde.mylibrary.listener.HelpArticleListener
import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.utils.Util
import com.gyde.mylibrary.R
import kotlinx.android.synthetic.main.tab_layout_2.*

class HelpArticlesFragment : Fragment(), HelpArticleListener {
    private val helpArticleList = ArrayList<HelpArticle>()
    private lateinit var mAdapter: HelpArticleAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.tab_layout_2, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = HelpArticleAdapter(helpArticleList, this)
        val layoutManager = LinearLayoutManager(requireContext())
        recycler_help_article.layoutManager = layoutManager
        recycler_help_article.itemAnimator = DefaultItemAnimator()
        recycler_help_article.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        Log.e("HelpArticle", "OnResume")
        if (Util.helpArticle.isNotEmpty()) {
            mAdapter.updateData(Util.helpArticle)
        }
    }

    companion object {
        fun newInstance() = HelpArticlesFragment()
    }

    override fun onHelpArticleClicked(helpArticle: HelpArticle) {
        val helpArticleJson = Gson().toJson(helpArticle)
        startActivity(Intent(requireContext(), HelpArticleDetailsActivity::class.java).let {
            it.putExtra("helpArticleJson", helpArticleJson)
        })
    }
}