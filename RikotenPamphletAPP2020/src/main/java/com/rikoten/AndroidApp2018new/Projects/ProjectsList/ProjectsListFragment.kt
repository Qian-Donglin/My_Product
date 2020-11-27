package com.rikoten.AndroidApp2018new.Projects.ProjectsList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.*
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty

class ProjectsListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_projects_list, container, false)

        //上の説明文の設定。
        setUpperExplain(view)

        val show_projects = arguments?.getParcelableArrayList<ProjectsProperty>("showing projects")

        val projects_list_catalog_rv = view.findViewById<RecyclerView>(R.id.projects_list_list_rv)

        projects_list_catalog_rv.adapter = ProjectsItemAdapter(
            ArrayList(show_projects),
            context,
            this@ProjectsListFragment,
            container)
        projects_list_catalog_rv.layoutManager = LinearLayoutManager(context)

        return view
    }

    fun setUpperExplain(view : View){
        val explain_tv = view.findViewById<TextView>(R.id.projects_list_upper_2_tv)
        //6つのボタンからこの一覧ページに飛ぶが、どれから来ているのかは下のように取得
        //中身は押されてる6つのボタンのうちの1つのR値を採用した
        val come_by_id = arguments?.getInt("come by button")

        when(come_by_id){
            R.id.all_type_choosing_bt ->{
                explain_tv.setText(R.string.str_project_list_explain_all)
            }
            R.id.genre_type_choosing_bt ->{
                //キーのcome by tagとcome by tagsは違うので気を付けること
                val selectedTag = arguments?.getString("come by tag")
                val showStr = getString(R.string.str_project_list_explain_genre) + selectedTag
                explain_tv.text = showStr
            }
            R.id.history_type_choosing_bt -> {
                //企画の履歴
                explain_tv.setText(R.string.str_project_list_explain_history)
            }
            R.id.favorite_type_choosing_bt -> {
                //これは単に説明としては「お気に入りに企画一覧」を表すだけ
                explain_tv.setText(R.string.str_project_list_explain_favorite)
            }
            R.id.random_type_choosing_bt -> {
                //これは単に説明として「ランダムで表示」を出すだけ
                explain_tv.setText(R.string.str_project_list_explain_random)
            }
            R.id.popular_type_choosing_bt -> {
                explain_tv.setText(getString(R.string.str_project_list_explain_popular))
            }
        }

    }

}
