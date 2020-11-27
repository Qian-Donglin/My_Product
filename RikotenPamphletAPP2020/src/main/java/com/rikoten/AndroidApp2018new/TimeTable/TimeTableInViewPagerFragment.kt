package com.rikoten.AndroidApp2018new.TimeTable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TimeTableEventProperty

class TimeTableInViewPagerFragment : Fragment() {

    //0-idxの日目
    val day by lazy{
        arguments?.getInt("day") ?: 0
    }
    val sentEventData by lazy{
        arguments?.getParcelableArrayList<TimeTableEventProperty>("time_table_data")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_time_table_in_view_pager, container, false)

        val time_table = view.findViewById<RecyclerView>(R.id.time_table_in_view_pager_time_table_rv)



        return view
    }

}