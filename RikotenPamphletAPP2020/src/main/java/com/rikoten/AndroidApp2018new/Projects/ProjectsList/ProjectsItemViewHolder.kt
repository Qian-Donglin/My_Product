package com.rikoten.AndroidApp2018new.Projects.ProjectsList

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsDetail.ProjectsDetailFragment
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProjectsItemViewHolder(itemview : View, val calledFragment : Fragment?, val fragment_container : ViewGroup?) : RecyclerView.ViewHolder(itemview){
    var container : View? = null
    var icon_iv : ImageView? = null
    var title_tv : TextView? = null
    var group_tv : TextView? = null
    var description_tv : TextView? = null
    var fav_bt : ImageButton? = null
    var holding_project_data : ProjectsProperty? = null//最初はnull埋めされているが、使うときは絶対にデータが入っていると保証できる。
    //holding_project_dataはProjectItemAdapterのonBindViewHolder()が呼ばれるときに、データが入るようにする。
    //View自体は長いRecyclerViewの場合全体の要素数に比べて、少ない生成数にとどまり、その回数分だけonCreateViewHolder()が生成される。
    //しかし、その確保したViewの中身のデータだけを差し替えて、Viewの実態は流用するのがRecyclerViewのわけで、
    //ProjectItemAdapter#"onBindViewHolder()が毎回、Viewの中身を設定しなおしている。
    //なので、そのたびに新しい(画面外にあって、不要なViewでリサイクルされる)itemを表示するときに、holding_project_dataを保持させて、
    //ViewHolder内で各Viewに対して、そのholding_project_dataを利用してOnClickListenerを作ればいい。
    //なお、そのためにここの内部でOnClickListenerを実装する。
    init{
        container = itemview
        icon_iv = itemview.findViewById(R.id.item_projects_list_rv_icon_iv)
        title_tv = itemview.findViewById(R.id.item_projects_list_rv_title_tv)
        group_tv = itemview.findViewById(R.id.item_projects_list_rv_group_tv)
        description_tv = itemview.findViewById(R.id.item_projects_list_rv_description_tv)
        fav_bt = itemView.findViewById(R.id.item_projects_list_rv_fav_iv)

        //企画詳細画面への遷移のOnClickListenerの設定
        icon_iv?.setOnClickListener(ProjectItemClickListener())
        title_tv?.setOnClickListener(ProjectItemClickListener())
        group_tv?.setOnClickListener(ProjectItemClickListener())
        description_tv?.setOnClickListener(ProjectItemClickListener())
        //Favボタン関連のOnClickListenerの設定
        fav_bt?.setOnClickListener(ProjectItemFavoriteImageListener())
    }

    //Cloud Fire Storeにどれを何回見たかを追跡させる
    fun addProjectViewCount(){
        val db = FirebaseFirestore.getInstance()
        //このキーの値を取得して+1してupdateする。
        val DataKey = "view_" + holding_project_data?.projectId
        var docRef = db.collection("UserData").document(AppInfo.UserName)
        docRef.update(DataKey, FieldValue.increment(1))

        docRef = db.collection("ProjectsData").document(holding_project_data?.projectId!!)
        docRef.update("view", FieldValue.increment(1))
        docRef.update("popularity", FieldValue.increment(1))
    }

    //プロジェクトのCardViewのクリックリスナー
    private inner class ProjectItemClickListener() : View.OnClickListener{
        override fun onClick(p0: View?) {

            val nxtFragment = ProjectsDetailFragment()
            nxtFragment.let{
                val bundle = Bundle()
                bundle.putParcelable("Project Property", holding_project_data)
                it.arguments = bundle
            }

            //新しく追加したイベントは、プロジェクトヒストリーのQueueの末尾に追加
            //すでにあるのなら、そのプロジェクトを最新のところへ持っていく
            if(AppInfo.LookedHistoryProjects.contains(holding_project_data?.projectId)){
                AppInfo.LookedHistoryProjects.remove(holding_project_data?.projectId)
            }else {
                if (AppInfo.LookedHistoryProjects.size == AppInfo.ProjectHistoryStoredNumber) {
                    //上限に達したら一番古いものを1つ捨てる
                    AppInfo.LookedHistoryProjects.remove()
                }
            }
            AppInfo.LookedHistoryProjects.add(holding_project_data?.projectId)

            addProjectViewCount()

            val transaction = calledFragment?.fragmentManager?.beginTransaction()
            transaction?.replace(fragment_container?.id!!, nxtFragment)
            transaction?.addToBackStack("")
            transaction?.commit()
        }
    }

    //プロジェクトのファボの星のImageViewのクリックリスナー
    private inner class ProjectItemFavoriteImageListener (): View.OnClickListener{
        override fun onClick(p0: View?) {
            if(AppInfo.FavoriteProjects.contains(holding_project_data?.projectId)){
                //お気に入りに含まれているので、Favを外す。
                val bt = p0 as ImageView
                bt.setImageResource(R.drawable.ic_not_fav_heart)
                AppInfo.FavoriteProjects.remove(holding_project_data?.projectId)

                //Cloud Fire storeに企画のfav数を送信
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("ProjectsData").document(holding_project_data?.projectId!!)
                docRef.update("fav", FieldValue.increment(-1))
                docRef.update("popularity", FieldValue.increment(-3))

            }else {
                //お気に入りに含まれていないので、Favを入れる。
                val bt = p0 as ImageView
                bt.setImageResource(R.drawable.ic_fav_heart)
                AppInfo.FavoriteProjects.add(holding_project_data?.projectId!!)

                //Cloud Fire storeに企画のfav数を送信
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("ProjectsData").document(holding_project_data?.projectId!!)
                docRef.update("fav", FieldValue.increment(1))
                docRef.update("popularity", FieldValue.increment(3))
            }
            p0?.scaleType = ImageView.ScaleType.CENTER_CROP

        }
    }

}