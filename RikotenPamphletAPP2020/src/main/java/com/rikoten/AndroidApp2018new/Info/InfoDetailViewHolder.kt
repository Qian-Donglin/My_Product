package com.rikoten.AndroidApp2018new.Info

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.*
import kotlinx.android.synthetic.main.item_infomation_list_rv.view.*

class InfoDetailViewHolder(itemView: View, isCategory: Int) : RecyclerView.ViewHolder(itemView) {

    private val CATEGORY_TYPE = 0
    private val NOT_CATEGORY_TYPE = 1

    var icon_iv: ImageView? = null
    var text_tv: TextView

    init {
        if (isCategory == CATEGORY_TYPE) {
            text_tv = itemView.findViewById(R.id.item_information_category_rv_tv)
        } else {
            icon_iv = itemView.findViewById(R.id.item_information_list_rv_iv)
            text_tv = itemView.findViewById(R.id.item_information_list_rv_tv)
        }
    }
}