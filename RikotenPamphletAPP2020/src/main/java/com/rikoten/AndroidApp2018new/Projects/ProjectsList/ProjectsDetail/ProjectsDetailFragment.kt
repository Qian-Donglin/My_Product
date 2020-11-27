package com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsDetail

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsListFragment
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import com.rikoten.AndroidApp2018new.SharingMaterial.ProjectPicker
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showYesNoDialogInFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.OtherApp.LaunchOtherApp
import com.rikoten.AndroidApp2018new.SharingMaterial.startActivityWithSavingData_my_fragment

//ProjectsDetailFragmentのViewを上から順に操作していく
class ProjectsDetailFragment : Fragment() {

    var sentData : ProjectsProperty? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_project_detail, container, false)
        sentData = arguments?.getParcelable<ProjectsProperty>("Project Property")

        //large imageの設定 Glideというライブラリを使用
        //読み込み終えたら、ProgressBarを見えなくする。
        val large_image_iv = view.findViewById<ImageView>(R.id.project_detail_large_image_iv)
        try {
            Glide.with(this)
                .asBitmap()
                .load(DataFromFireBase.getFirebaseStorageReference(sentData?.large_image!!))
                .listener(object : RequestListener<Bitmap> {
                    val progressbar =
                        view.findViewById<ProgressBar>(R.id.project_detail_progress_bar_pb)


                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //ここを呼ばれると、データの取得が失敗したことになる。
                        //というわけでデフォルト画像に差し替える。
                        large_image_iv.setImageResource(R.drawable.project_default_large_image)
                        progressbar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //ProgressBarを消す
                        progressbar.visibility = View.GONE
                        return false
                    }
                })
                .into(large_image_iv)
        }
        catch(e : kotlin.Exception){
            Log.d("AppException", "large_image画像がFirebaseのStorageの指定の位置にありません。代わりにデフォルト画像を挿入します。")
            large_image_iv.setImageResource(R.drawable.project_default_large_image)
        }

        //titleの設定
        view.findViewById<TextView>(R.id.project_detail_title_tv)
            .text = sentData?.title

        //subtitleの設定
        view.findViewById<TextView>(R.id.project_detail_sub_title_tv)
            .text = sentData?.subtitle

        //groupの設定
        view.findViewById<TextView>(R.id.project_detail_group_name_tv)
            .text = sentData?.group

        val ic_live_event = view.findViewById<ImageView>(R.id.project_detail_ic_live_event_iv)
        if(sentData?.is_live_event!!){
            //ライブ配信イベントなら
            //参加方法のダイヤログを出す。
            showNotificationDialogInFragment(this,
            titleStr = getString(R.string.str_general_interface_verification_title),
            messageStr = getString(R.string.str_project_detail_notification_how_to_attend_live_event))
        }
        else {
            //そうでないなら
            ic_live_event.visibility = View.GONE
        }

        //タグのListViewの設定
        val tags_lv = view.findViewById<ListView>(R.id.project_detail_tag_list_lv)
        val tag_adapter_data : MutableList<MutableMap<String, String>> = mutableListOf()
        for(tagStr in sentData?.tags!!) {
            tag_adapter_data.add(mutableMapOf("tag name" to tagStr))
        }
        tags_lv.adapter = SimpleAdapter(
            context,
            tag_adapter_data,
            R.layout.item_tag_row_small,
            arrayOf("tag name"),
            intArrayOf(R.id.item_tag_row_tag_name_small_tv)
        )
        //押されたらそのタグを含む企画の一覧へ飛ぶようなリスナーを設定
        tags_lv.setOnItemClickListener(onTagListClickListener(sentData?.tags!!, container!!))

        //Twitterでシェアのボタン
        view.findViewById<ImageButton>(R.id.project_detail_share_tweet_bt)
            .setOnClickListener(onButtonClickListener())

        view.findViewById<ImageButton>(R.id.project_detail_share_line_bt)
            .setOnClickListener(onButtonClickListener())

        //理工展のWebページ上の企画紹介ページへのボタン
        view.findViewById<ImageButton>(R.id.project_detail_open_rikoten_web_description_bt)
            .setOnClickListener(onButtonClickListener())

        //バーチャル理工展を起動してその展示前に送るボタン
        view.findViewById<ImageButton>(R.id.project_detail_open_virtual_rikoten_bt)
            .setOnClickListener(onButtonClickListener())

        //企画概要の設定
        //2020年のリソースの制限上、英語版では企画概要の欄を消すことになっている
        if(AppInfo.AppUsedLanguage == "en"){
            //サブタイトルも消す
            view.findViewById<TextView>(R.id.project_detail_sub_title_tv)
                .visibility = View.GONE

            //企画概要のあのタイトルと中身2つのvisiblityを見えなくしてスペースも詰める
            view.findViewById<TextView>(R.id.project_detail_projects_description_title_tv)
                .visibility = View.GONE
            view.findViewById<TextView>(R.id.project_detail_projects_description_tv)
                .visibility = View.GONE
            //TODO 実はここ、上の区切り線にあたる黒塗りViewは消してないけど、別に消さなくても意外にバレないもんだった(区切り線がちょっと太いだけ)
            //余裕があったら消す。
        }
        else {
            view.findViewById<TextView>(R.id.project_detail_projects_description_tv)
                .text = sentData?.description
        }

        //企画詳細の設定
        view.findViewById<TextView>(R.id.project_detail_project_description_detailed_tv)
            .text = sentData?.description_detailed

        //団体紹介の設定
        view.findViewById<TextView>(R.id.project_detail_group_description_tv)
            .text = sentData?.group_description

        //団体のウェブページの設定
        if(sentData?.group_website.isNullOrEmpty()){
            //団体のウェブページがない
            //画像を灰色のWebマークに、リスナーは設定せずに押しても反応がないように
            view.findViewById<ImageButton>(R.id.project_detail_group_web_link_bt)
                .setImageResource(R.drawable.ic_web_disable_button_in_detail)
        }
        else {
            //団体のウェブページがある
            //画像を明るい色のWebマーク(のまま)に　リスナーを設定する
            val target_bt = view.findViewById<ImageButton>(R.id.project_detail_group_web_link_bt)
            target_bt.setImageResource(R.drawable.ic_web_button_in_detail)
            target_bt.setOnClickListener(onButtonClickListener())

            //さらに、ライブ配信団体ならば、ライブ配信のアイコンにもリスナーをつける
            if(sentData?.is_live_event!!){
                ic_live_event.setOnClickListener(onButtonClickListener())
            }
        }

        //団体のTwitterの設定
        if(sentData?.twitter.isNullOrEmpty()){
            //団体のTwitterアカウントがない
            //画像を灰色のTwitterマークに、リスナーは設定せずに押しても反応がないように
            view.findViewById<ImageButton>(R.id.project_detail_group_twitter_bt)
                .setImageResource(R.drawable.ic_disable_twitter_logo)
        }
        else {
            //団体のTwitterアカウントがある
            //画像を明るい色のTwitterマーク(のまま)に　リスナーを設定する
            val target_bt = view.findViewById<ImageButton>(R.id.project_detail_group_twitter_bt)
            target_bt.setImageResource(R.drawable.ic_twitter_logo)
            target_bt.setOnClickListener(onButtonClickListener())
        }

        //団体のFacebookの設定
        if(sentData?.facebook.isNullOrEmpty()){
            //団体のFacebookアカウントがない
            //画像を灰色のFacebookマークに、リスナーは設定せずに押しても反応がないように
            view.findViewById<ImageButton>(R.id.project_detail_group_facebook_bt)
                .setImageResource(R.drawable.ic_disable_facebook_logo)
        }
        else {
            //団体のFacebookアカウントがある
            //画像を明るい色のFacebookマーク(のまま)に　リスナーを設定する
            val target_bt = view.findViewById<ImageButton>(R.id.project_detail_group_facebook_bt)
            target_bt.setImageResource(R.drawable.ic_facebook_logo)
            target_bt.setOnClickListener(onButtonClickListener())
        }

        //団体のInstagramの設定
        if(sentData?.instagram.isNullOrEmpty()){
            //団体のFacebookアカウントがない
            //画像を灰色のFacebookマークに、リスナーは設定せずに押しても反応がないように
            view.findViewById<ImageButton>(R.id.project_detail_group_instagram_bt)
                .setImageResource(R.drawable.ic_disable_instagram_logo)
        }
        else {
            //団体のFacebookアカウントがある
            //画像を明るい色のFacebookマーク(のまま)に　リスナーを設定する
            val target_bt = view.findViewById<ImageButton>(R.id.project_detail_group_instagram_bt)
            target_bt.setImageResource(R.drawable.ic_instagram_logo)
            target_bt.setOnClickListener(onButtonClickListener())
        }

        return view
    }

    private inner class onButtonClickListener : View.OnClickListener{
        override fun onClick(p0: View?) {
            val shareSentence = getString(R.string.str_project_detail_share_front) +
                    sentData?.title +
                    getString(R.string.str_project_detail_share_back)
            var uriStr = ""
            var intent : Intent
            when(p0?.id){
                R.id.project_detail_share_tweet_bt ->{
                    //Twitterでシェア
                    uriStr = "https://twitter.com/share?text=" + shareSentence
                }
                R.id.project_detail_share_line_bt -> {
                    //LINEでシェア
                    uriStr = "https://line.me/R/msg/text/?" + shareSentence
                }
                R.id.project_detail_open_rikoten_web_description_bt -> {
                    //理工展Webでの紹介ページを開く
                    uriStr = ""

                    if(DataFromFireBase.DoRikotenHeld) {
                        //理工展時間内限定でWebへ飛ぶ
                        uriStr = sentData?.rikoten_web_link ?: ""
                    }
                    else {
                        //理工展はまだ始まってないので、ダイヤログを出す。
                        showNotificationDialogInFragment(this@ProjectsDetailFragment,
                        titleStr = getString(R.string.str_general_interface_verification_title),
                        messageStr = getString(R.string.str_project_detail_notification_message_web_out_of_rikoten_span)
                        )
                    }
                }
                R.id.project_detail_open_virtual_rikoten_bt -> {

                    if(!DataFromFireBase.DoRikotenHeld) {
                        //理工展時間外なら
                        showYesNoDialogInFragment(
                            this@ProjectsDetailFragment,
                            titleStr = getString(R.string.str_general_interface_verification_title),
                            messageStr = getString(R.string.str_project_detail_verification_message_out_of_rikoten_span),
                            reaction_positive = {
                                LaunchOtherApp.LaunchVirtualRikotenApp(this@ProjectsDetailFragment, sentData?.virtual_rikoten_location)
                            },
                            reaction_negative = {}
                        )

                    }
                    else {
                        LaunchOtherApp.LaunchVirtualRikotenApp(this@ProjectsDetailFragment, sentData?.virtual_rikoten_location)
                    }
                }
                R.id.project_detail_group_web_link_bt ->{
                    //団体のWebリンクを開く
                    uriStr = sentData?.group_website!!
                }
                R.id.project_detail_group_twitter_bt -> {
                    //団体のTwitterリンクを開く
                    uriStr = "https://twitter.com/" + sentData?.twitter!!
                }
                R.id.project_detail_group_facebook_bt -> {
                    //団体のFacebookリンクを開く
                    uriStr = sentData?.facebook!!
                }
                R.id.project_detail_group_instagram_bt -> {
                    //団体のInstagramのリンクを開く
                    uriStr = "https://www.instagram.com/" + sentData?.instagram!!
                }
                R.id.project_detail_ic_live_event_iv -> {
                    //ライブ配信団体限定で、WebのURLと同じところに飛ばす。
                    if(DataFromFireBase.DoRikotenHeld) {
                        //理工展時間内限定でWebへ飛ぶ
                        uriStr = sentData?.rikoten_web_link ?: ""
                    }
                    else {
                        //理工展はまだ始まってないので、ダイヤログを出す。
                        showNotificationDialogInFragment(this@ProjectsDetailFragment,
                            titleStr = getString(R.string.str_general_interface_verification_title),
                            messageStr = getString(R.string.str_project_detail_notification_message_web_out_of_rikoten_span)
                        )
                    }
                }
            }
            if(uriStr != "") {
                intent = Intent(ACTION_VIEW, Uri.parse(uriStr))
                //startActivity(intent, Bundle())
                startActivityWithSavingData_my_fragment(this@ProjectsDetailFragment, intent)
            }
        }
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
                    ArrayList(ProjectPicker.pickProjectOneCondition(
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
