package com.rikoten.AndroidApp2018new.Projects.ProjectsList

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.*
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty

class ProjectsItemAdapter(
    private var givenData : ArrayList<ProjectsProperty>,
    private var _context : Context?,
    private var calledFragment : Fragment,
    private val fragment_container: ViewGroup?
    ) : RecyclerView.Adapter<ProjectsItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsItemViewHolder {
        val view = LayoutInflater.from(_context).inflate(R.layout.item_projects_in_list, parent, false)
        //view.setOnClickListener(ProjectItemClickListener(cnt_idx))
        val vh = ProjectsItemViewHolder(view, calledFragment, fragment_container)
        //ListenerはViewHolder内に置いた。理由はそこを見ればコメントアウトされている。
        return vh
    }

    override fun onBindViewHolder(holder: ProjectsItemViewHolder, position: Int) {
        val targetData = givenData[position]
        //ダミーのイメージ画像
        //Glideというライブラリを使用

        try {
            Glide.with(calledFragment)
                .asBitmap()
                .load(DataFromFireBase.getFirebaseStorageReference(givenData[position].icon!!))
                .into(holder.icon_iv!!)
        }

        catch(e : kotlin.Exception){
            Log.d("AppException", "icon画像がFirebaseのStorageの指定の位置にありません。代わりにデフォルト画像を挿入します。")
            holder.icon_iv!!.setImageResource(R.drawable.project_default_large_image)
        }

        //以下の2つのViewの横幅を設定する。盾幅はWRAP_CONTENTにする。
        //具体的な設定はAppInfo内で定められている。
        //これで設定しなおすと、どうやらViewのConstraintも崩れるので、設定しなおす。
        holder.group_tv?.text = targetData.group
        holder.title_tv?.text = targetData.title

        val groupParams = ConstraintLayout.LayoutParams(
            AppInfo.ProjectsItemListStringWidthPX,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        groupParams.leftMargin = 16
        groupParams.topMargin = 8
        groupParams.startToEnd = R.id.item_projects_list_rv_icon_iv
        groupParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID

        val titleParams = ConstraintLayout.LayoutParams(
            AppInfo.ProjectsItemListStringWidthPX,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        titleParams.topMargin = 16
        titleParams.bottomMargin = 16
        titleParams.leftMargin = 16
        titleParams.bottomToTop = R.id.item_projects_list_rv_description_tv
        titleParams.startToEnd = R.id.item_projects_list_rv_icon_iv
        titleParams.topToBottom = R.id.item_projects_list_rv_group_tv

        holder.title_tv?.layoutParams = titleParams
        holder.group_tv?.layoutParams = groupParams

        //リソースの制約により、2020年の英語版の企画概要は未翻訳である。
        //故に、ここでは、英語版の企画一覧画面の企画概要は、企画詳細で代替するという手法にする。
        if(AppInfo.AppUsedLanguage == "en"){
            holder?.description_tv?.text = targetData.description_detailed
        }
        else {
            holder.description_tv?.text = targetData.description
        }
        if(AppInfo.FavoriteProjects.contains(targetData.projectId)){
            //favlistに含まれている
            holder.fav_bt?.setImageResource(R.drawable.ic_fav_heart)
        }
        else{
            //favlistに含まれてない
            holder.fav_bt?.setImageResource(R.drawable.ic_not_fav_heart)
        }
        //画像を縮小
        holder.fav_bt?.scaleType = ImageView.ScaleType.CENTER_CROP
        //再利用されている？Viewに、それぞれの持つProjectsPropertyを持たせる
        //理由はViewHoldにある。
        holder.holding_project_data = givenData[position]
    }

    override fun getItemCount(): Int {
        return givenData.size
    }

    //プロジェクトのファボの星のImageViewのクリックリスナー
    private inner class ProjectItemFavoriteImageListener (val projectId : String?): View.OnClickListener{
        override fun onClick(p0: View?) {
            if(AppInfo.FavoriteProjects.contains(projectId)){
                //お気に入りに含まれているので、Favを外す。
                val bt = p0 as ImageView
                bt.setImageResource(R.drawable.ic_not_fav_heart)
                AppInfo.FavoriteProjects.remove(projectId)
                Log.d("assd", projectId + "押されたよ☆")
            }else {
                //お気に入りに含まれていないので、Favを入れる。
                val bt = p0 as ImageView
                bt.setImageResource(R.drawable.ic_fav_heart)
                AppInfo.FavoriteProjects.add(projectId!!)
            }
            p0?.scaleType = ImageView.ScaleType.CENTER_CROP

        }
    }
}