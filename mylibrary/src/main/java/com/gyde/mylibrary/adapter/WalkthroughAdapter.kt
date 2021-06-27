package com.gyde.mylibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gyde.mylibrary.listener.WalkthroughListeners
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.R

internal class WalkthroughAdapter(
    private var walkthroughList: List<Walkthrough>,
    listeners: WalkthroughListeners
) :
    RecyclerView.Adapter<WalkthroughAdapter.MyViewHolder>() {

    var mListener: WalkthroughListeners = listeners

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_title)
        var layoutGuideMe: LinearLayout = view.findViewById(R.id.layout_guide_me)
        var layoutPlayVideo: LinearLayout = view.findViewById(R.id.layout_play_video)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_walkthrough, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = String.format("%s", walkthroughList[position].flowName)
        holder.layoutGuideMe.setOnClickListener {
            mListener.onGuideMeClicked(walkthroughList[position].flowId)
        }
    }

    override fun getItemCount(): Int {
        return walkthroughList.size
    }

    fun updateData(response: List<Walkthrough>) {
        walkthroughList = response
        notifyDataSetChanged()
    }

    fun filterList(filteredList: ArrayList<Walkthrough>) {
        walkthroughList = filteredList
        notifyDataSetChanged()
    }

}