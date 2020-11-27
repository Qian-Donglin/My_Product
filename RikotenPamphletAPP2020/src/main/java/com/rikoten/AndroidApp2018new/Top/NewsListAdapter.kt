package com.rikoten.AndroidApp2018new.Top

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsItemViewHolder
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.NewsProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

class NewsListAdapter(
    private var givenData : List<NewsProperty>,
    private var _context : Context,
    private var calledFragment : Fragment,
    private val fragment_container: ViewGroup?
) : RecyclerView.Adapter<NewsListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        val view = LayoutInflater.from(_context).inflate(R.layout.item_top_news, parent, false)
        return NewsListViewHolder(view, calledFragment)
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        val target = givenData[position]
        Glide.with(calledFragment)
            .load(DataFromFireBase.getFirebaseStorageReference(target.icon_location!!))
            .into(holder.news_icon!!)

        holder.news_title?.text = target.title

        val announceDate = try {
            SimpleDateFormat("yyyy/MM/dd hh:mm").parse(givenData[position].timestamp)
        } catch (e: ParseException) {
            Log.d("firebaseLog", "お知らせのtimestampのフォーマットがyyyy/MM/dd hh:mmではないため、お知らせの日にちのパースに失敗しました。")
            null
        }
        val announceCalendar = if (announceDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = announceDate
                calendar
            } else
                null

        val announceDateStr = "${ announceCalendar?.get(Calendar.MONTH)?.plus(1) } 月 ${ announceCalendar?.get(Calendar.DAY_OF_MONTH) } 日"
        holder.news_time?.text = announceDateStr

        holder.NewsData = target

    }

    override fun getItemCount(): Int {
        return givenData.size
    }

}