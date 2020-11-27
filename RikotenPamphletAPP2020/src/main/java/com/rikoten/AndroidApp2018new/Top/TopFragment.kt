package com.rikoten.AndroidApp2018new.Top

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import com.rikoten.AndroidApp2018new.*
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.markushi.ui.CircleButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.OtherApp.LaunchOtherApp
import com.rikoten.AndroidApp2018new.SharingMaterial.findTagedStringValue
import com.google.firebase.storage.FirebaseStorage
import com.rikoten.AndroidApp2018new.SharingMaterial.startActivityWithSavingData_my_fragment
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_project_detail.view.*
import kotlinx.android.synthetic.main.item_infomation_list_rv.*
import technolifestyle.com.imageslider.FlipperLayout
import technolifestyle.com.imageslider.FlipperView

/**
 * A simple [Fragment] subclass.
 */
class TopFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ライフサイクル確認", "フラグメントでonCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_top, container, false)

        val image_flipper_fl = view.findViewById<FlipperLayout>(R.id.top_image_flipper_fl)

        //動的にサイズを16:9に合わせる。
        image_flipper_fl.layoutParams = ConstraintLayout.LayoutParams(AppInfo.ScreenWidthPX,
            (AppInfo.ScreenWidthPX.toDouble() * 9.0 / 16.0).toInt())

        for(i in 0 until DataFromFireBase.TopFlipperData.size){
            val flipper_view = FlipperView(activity?.baseContext!!)

            val targetData = DataFromFireBase.TopFlipperData[i]

            Glide.with(this)
                .asBitmap()
                .load(
                    DataFromFireBase.getFirebaseStorageReference(targetData.image_location!!)
                )
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(
                    object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                            //無理やり端末のサイズに合わせている。これいいのかな？TODO
                            var resized_resource = Bitmap.createScaledBitmap(resource,
                                AppInfo.ScreenWidthPX, AppInfo.ScreenWidthPX * 9 / 16,
                                true)

                            flipper_view.setImageScaleType(ImageView.ScaleType.FIT_CENTER)
                                .setImageBitmap(resized_resource, { imageView, image ->
                                    imageView.setImageBitmap(image)
                                })
                            image_flipper_fl.addFlipperView(flipper_view)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    }
                )

            //FlipperViewに対するListenerの設定。ちなみにこれは公式GitHubにないので、ここにしかない。
            flipper_view.setOnFlipperClickListener(
                object : FlipperView.OnFlipperClickListener{
                    override fun onFlipperClick(flipperView: FlipperView) {
                        var intent : Intent = Intent()
                        when(targetData.transition_type){
                            "ARYaguchi" -> {
                                //AR矢口くんアプリ起動
                                /*
                                //一旦切り離す
                                showNotificationDialogInFragment(this@TopFragment,
                                titleStr = "お知らせ",
                                messageStr = "大変申し訳ありませんが、現在AR矢口くん機能は調整中につき使用できません。近日のアップデートをお待ちください。")
                                */

                                launchARYaguchiWithCirtification()
                            }
                            "VirtualRikotenApp" -> {
                                //virtual理工展起動
                                launchVirtualRikoten()
                            }
                            "ProjectDetail" -> {
                                //詳細なプロジェクトの詳細に飛ばす

                            }
                            "website" -> {
                                //指定のリンクに飛ばす
                                val uriStr = targetData.destination
                                val uri = Uri.parse(uriStr)
                                intent = Intent(ACTION_VIEW, uri)
                                //startActivity(intent)
                                startActivityWithSavingData_my_fragment(this@TopFragment, intent)
                            }
                            else ->{
                                //一応例外処理
                            }
                        }
                    }
                }
            )

        }

        //4つ並んでいる丸ボタンのListener設定。
        //下にinner classとしてまとめている。
        //ウォークラリーボタン
        view.findViewById<CircleButton>(R.id.top_virtual_rikoten_bt).setOnClickListener(OnTop4ButtonClickListener())
        //AR矢口のボタン
        view.findViewById<CircleButton>(R.id.top_aryaguchi_bt).setOnClickListener(OnTop4ButtonClickListener())
        //今年の理工展のボタン
        //TODO ファーストリリースではオミット
        //view.findViewById<CircleButton>(R.id.top_rikoten_this_year_bt).setOnClickListener(OnTop4ButtonClickListener())
        //理工展No.1ボタン
        view.findViewById<CircleButton>(R.id.top_rikoten_this_year_bt).setOnClickListener(OnTop4ButtonClickListener())

        Log.d("設定したか", "newsは合計:" + DataFromFireBase.NewsData.size)
        val news_rv = view.findViewById<RecyclerView>(R.id.top_news_rv)
        news_rv.layoutManager = LinearLayoutManager(context)
        news_rv.adapter = NewsListAdapter(DataFromFireBase.NewsData, context!!, this, container)

        return view
    }

    inner class OnTop4ButtonClickListener : View.OnClickListener{
        override fun onClick(p0: View?) {
            when(p0?.id){
                R.id.top_virtual_rikoten_bt -> {
                    LaunchOtherApp.LaunchVirtualRikotenApp(this@TopFragment)
                }
                R.id.top_aryaguchi_bt -> {
                    launchARYaguchiWithCirtification()
                }

                R.id.top_rikoten_this_year_bt -> {
                    val intent = Intent(ACTION_VIEW,
                        if(AppInfo.AppUsedLanguage == "ja") Uri.parse("https://rikoten.com/about/")
                        else Uri.parse(("https://rikoten.com/about/en"))
                    )
                    //startActivity(intent)
                    startActivityWithSavingData_my_fragment(this@TopFragment, intent)
                }

            }
        }
    }

    //AR矢口くん起動関連の関数 Dialogによる確認機能付きのWrap関数
    private fun launchARYaguchiWithCirtification(){
        showNotificationDialogInFragment(this,
            titleStr = getString(R.string.str_top_aryaguchi_notification_title),
            messageStr = getString(R.string.str_top_aryaguchi_notification_content_ok),
            reaction = {
                LaunchOtherApp.LaunchARYaguchi(it)
            }
        )
    }

    //これはただのリネームである。
    private fun launchVirtualRikoten(){
        LaunchOtherApp.LaunchVirtualRikotenApp(this@TopFragment)
    }

}
