package com.gyde.mylibrary.screens

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.gyde.mylibrary.R
import com.gyde.mylibrary.adapter.HelpArticleAdapter
import com.gyde.mylibrary.listener.HelpArticleListener
import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.network.retrofit.ServiceBuilder
import com.gyde.mylibrary.network.retrofit.WalkthroughListInterface
import com.gyde.mylibrary.utils.NetworkUtils
import com.gyde.mylibrary.utils.Util
import kotlinx.android.synthetic.main.tab_layout_2.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class HelpArticlesFragment : Fragment(), HelpArticleListener {
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

        initListeners()
    }

    private fun initListeners() {
        edt_search_article.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })

        layout_branding.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://gyde.ai")
            startActivity(intent)
        }
    }

    internal fun updateLanguageSelection(selectedLanguage: String) {
        val newLanguageList = mutableListOf<HelpArticle>()
        for (item in Util.helpArticle) {
            if (item.language.equals(selectedLanguage, true)) {
                newLanguageList.add(item)
            }
        }
        try {
            mAdapter.updateData(newLanguageList)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<HelpArticle> = ArrayList()
        for (item in Util.helpArticle) {
            if (item.question.lowercase()
                    .contains(text.lowercase()) && item.language.equals(Util.selectedLanguage, true)
            ) {
                filteredList.add(item)
            }
        }
        mAdapter.filterList(filteredList)
    }

    override fun onResume() {
        super.onResume()
        Log.e("HelpArticle", "OnResume")
        updateLanguageSelection(Util.selectedLanguage)
    }

    companion object {
        fun newInstance() = HelpArticlesFragment()
    }

    override fun onHelpArticleClicked(helpArticle: HelpArticle) {
        try {
            saveLog(helpArticle.queId)
            val helpArticleJson = Gson().toJson(helpArticle)
            startActivity(
                Intent(requireActivity(), HelpArticleDetailsActivity::class.java).putExtra(
                    "helpArticleJson",
                    helpArticleJson
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showInternetConnectivityDialog() {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_internet_not_available)
        val yesBtn: TextView = dialog.findViewById(R.id.tvOk)
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun saveLog(flowId: String) {
        val activityName = ""
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showInternetConnectivityDialog()
        } else {
            val request = ServiceBuilder.buildService(WalkthroughListInterface::class.java)

            request.saveUserLog(
                appId = Util.appId,
                flowID = flowId,
                timestamp = System.currentTimeMillis(),
                type = "runFaqLog",
                uuid = Util.getUuid(requireContext()),
                url = "",
                source = "android",
                activityName = activityName
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.e("log", "" + response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}