package com.gyde.mylibrary.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.gyde.mylibrary.R
import com.gyde.mylibrary.listener.WalkthroughListeners
import com.gyde.mylibrary.network.response.walkthroughlist.Walkthrough
import com.gyde.mylibrary.utils.Util

internal class WalkthroughAdapter(
    private var walkthroughList: List<Walkthrough>,
    listeners: WalkthroughListeners
) :
    RecyclerView.Adapter<WalkthroughAdapter.MyViewHolder>() {

    private var mListener: WalkthroughListeners = listeners

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_title)
        var layoutGuideMe: LinearLayout = view.findViewById(R.id.layout_guide_me)

        var imgGuideMe: ImageView = view.findViewById(R.id.img_guide_me)
        var imgPlayVideo: ImageView = view.findViewById(R.id.img_play_video)
        var tvLblGuideMe: AppCompatTextView = view.findViewById(R.id.tv_lbl_guide_me)
        var tvLblPlayVideo: AppCompatTextView = view.findViewById(R.id.tv_lbl_play_video)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_walkthrough, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.imgGuideMe.setColorFilter(Color.parseColor(Util.headerColor))
        holder.imgPlayVideo.setColorFilter(Color.parseColor(Util.headerColor))
        holder.tvLblGuideMe.setTextColor(Color.parseColor(Util.btnColor))
        holder.tvLblPlayVideo.setTextColor(Color.parseColor(Util.headerColor))

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