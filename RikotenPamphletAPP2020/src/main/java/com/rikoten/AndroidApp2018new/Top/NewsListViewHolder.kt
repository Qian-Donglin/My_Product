package com.rikoten.AndroidApp2018new.Top

import android.content.Context
import android.view.View
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.NewsProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInActivity
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInFragment
import kotlinx.android.synthetic.main.item_top_news.view.*

class NewsListViewHolder(val itemveiw : View, val calledFragment : Fragment) : RecyclerView.ViewHolder(itemveiw){
    var news_icon : ImageView? = null
    var news_title : TextView? = null
    var news_time : TextView? = null
    var NewsData : NewsProperty? = null
    init{
        news_icon = itemveiw.findViewById(R.id.item_top_news_icon_iv)
        news_title = itemveiw.findViewById(R.id.item_top_news_title_tv)
        news_time = itemveiw.findViewById(R.id.item_top_news_written_date_tv)
        itemveiw.setOnClickListener(OnItemClickListener())
    }

    inner class OnItemClickListener : View.OnClickListener{
        override fun onClick(p0: View?) {
            showNotificationDialogInFragment(calledFragment,
            titleStr = news_title?.text.toString(),
            messageStr = NewsData?.detail)
        }
    }

}