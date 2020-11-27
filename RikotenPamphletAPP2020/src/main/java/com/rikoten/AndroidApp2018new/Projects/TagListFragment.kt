package com.rikoten.AndroidApp2018new.Projects

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsListFragment
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.CustomView.AutoMeasuredListView
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import com.rikoten.AndroidApp2018new.SharingMaterial.ProjectPicker

class TagListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //ここは企画を表すListView(AutoMeasuredしたもの)を入れただけのFragmenｔ。

        val view = inflater.inflate(R.layout.fragment_tag_list, container, false)

        val tags = arguments?.getStringArrayList("tags")
        val alltag_lv = view.findViewById<ListView>(R.id.tag_list_all_tags_lv)
        val tag_adapter_data : MutableList<MutableMap<String, String>> = mutableListOf()
        //最初
        for(tagStr in tags!!) {
            tag_adapter_data.add(mutableMapOf("tag name" to tagStr))
        }

        alltag_lv.adapter = SimpleAdapter(
            context,
            tag_adapter_data,
            R.layout.item_tag_row,
            arrayOf("tag name"),
            intArrayOf(R.id.item_tag_row_tag_name_tv)
        )
        //押されたらそのタグを含む企画の一覧へ飛ぶようなリスナーを設定
        alltag_lv.setOnItemClickListener(onTagListClickListener(tags!!, container!!))

        return view
    }

    private inner class onTagListClickListener(val tags : List<String>, val container : ViewGroup) : AdapterView.OnItemClickListener{
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val tagStr = tags[p2]
            val nxtFragment = ProjectsListFragment()
            nxtFragment.let{
                val bundle = Bundle()
                bundle.putInt("come by button", R.id.genre_type_choosing_bt)
                bundle.putString("come by tag", tagStr)
                bundle.putParcelableArrayList("showing projects",
                    ArrayList(
                        ProjectPicker.pickProjectOneCondition(
                        DataFromFireBase.ProjectsData,
                        tagStr))
                )
                it.arguments = bundle
            }
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(container?.id!!, nxtFragment)
            transaction?.addToBackStack("")
            transaction?.commit()
        }
    }

}
