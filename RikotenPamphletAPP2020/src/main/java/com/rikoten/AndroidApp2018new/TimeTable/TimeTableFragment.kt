package com.rikoten.AndroidApp2018new.TimeTable

import com.rikoten.AndroidApp2018new.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase

/**
 * A simple [Fragment] subclass.
 */
class TimeTableFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_time_table, container, false)

        /*
        val time_table_viewpager2 = view.findViewById<ViewPager2>(R.id.time_table_vp)
        time_table_viewpager2.adapter = TimeTableViewPagerAdapter(this)
        */

        //タブと連結する。
        val tablayout = view.findViewById<TabLayout>(R.id.time_table_tab_tbl)

        tablayout.visibility = View.GONE

        /*
        TabLayoutMediator(tablayout, time_table_viewpager2) {tab, position ->
            if(position == 0){
                //1日目
                tab.text = getString(R.string.str_time_table_tab_day1)
            }
            else if(position == 1){
                //2日目
                tab.text = getString(R.string.str_time_table_tab_day2)
            }
            else {
                //例外処理　ここは来ないと思うが・・・
                tab.text = ""
            }
        }.attach()
        */
        return view
    }

    /*
    private inner class TimeTableViewPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment){
        override fun createFragment(position: Int): Fragment {
            val ret_fragment = TimeTableInViewPagerFragment()

            ret_fragment.let{
                val bundle = Bundle()
                //position(0-idx)日目の企画データを送る
                bundle.putParcelableArrayList("time_table_data", ArrayList(DataFromFireBase.TimeTableEventData[position]))
            }

            return ret_fragment
        }

        override fun getItemCount(): Int {
            return AppInfo.RikotenHeldingSpanDays
        }
    }
     */

}
