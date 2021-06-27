package com.gyde.mylibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.gyde.mylibrary.listener.HelpArticleListener
import com.gyde.mylibrary.network.response.walkthroughlist.HelpArticle
import com.gyde.mylibrary.R
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough

internal class HelpArticleAdapter(
    private var helpArticle: List<HelpArticle>,
    listeners: HelpArticleListener
) :
    RecyclerView.Adapter<HelpArticleAdapter.MyViewHolder>() {

    private var mListener: HelpArticleListener = listeners

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_title)
        var layoutHelpArticle: ConstraintLayout = view.findViewById(R.id.layout_help_article)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_help_article, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = String.format("%s", helpArticle[position].question)
        try {
            holder.layoutHelpArticle.setOnClickListener {
                mListener.onHelpArticleClicked(
                    helpArticle[position]
                )
            }
        } catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return helpArticle.size
    }

    fun updateData(response: List<HelpArticle>) {
        helpArticle = response
        notifyDataSetChanged()
    }

    fun filterList(filteredList: ArrayList<HelpArticle>) {
        helpArticle = filteredList
        notifyDataSetChanged()
    }
}