package com.rikoten.AndroidApp2018new.Info

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.*
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.ShowingPlainTextFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInFragment

//渡す引数は、RecyclerViewを埋めるデータ
//参照しているcontext
//呼び出し元のFragment(を継承したクラス)
//画面遷移を伴うときに変更の描画場所となる親レイアウト
class InfoDetailAdapter(
    private val givenData: MutableList<MutableMap<String, Any>>,
    private val _context: Context,
    private val calledFragment: Fragment,
    private val fragment_container: ViewGroup
)
    : RecyclerView.Adapter<InfoDetailViewHolder>(){

    //ClickのListenerの内部でinner classとして定義。このクラスの最後にある。

    //viewType : Int の返ってくる値はこの2つに
    private val CATEGORY_TYPE = 0
    private val NOT_CATEGORY_TYPE = 1

    //getItemViewType()が呼び出されるたびに加算させる。
    //色々調べたけどいい機能が見つからなかった　後継者見つけてくれ
    private var view_cnt = 0

    override fun getItemViewType(position: Int): Int {
        return if(givenData[position]["isCategory"] as Boolean){
            return CATEGORY_TYPE
        }
        else return NOT_CATEGORY_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoDetailViewHolder {
        val layoutToInflate = if(viewType == CATEGORY_TYPE){
            R.layout.item_information_category_list_rv
        }
        else {
            R.layout.item_infomation_list_rv
        }
        val view = LayoutInflater.from(_context).inflate(layoutToInflate, parent, false)
        val position = view_cnt++
        //ここではこのようにview_cntを実装したがたぶん改善の余地がある(すでにある機能を使うとか)

        //カテゴリーでない場合に限ってListenerを設定
        if(viewType == NOT_CATEGORY_TYPE)
            view.setOnClickListener(InfoItemClickListener(givenData[position]["text"] as String))

        return InfoDetailViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: InfoDetailViewHolder, position: Int) {
        val targetData = givenData[position]
        if(targetData["isCategory"] as Boolean){
            holder.text_tv.text = targetData["text"] as String
        }
        else {
            holder.icon_iv?.setImageResource(targetData["icon"] as Int)
            holder.text_tv.text = targetData["text"] as String
        }
    }

    override fun getItemCount(): Int {
        return givenData.size
    }

    //各要素のクリックリスナー
    private inner class InfoItemClickListener(val selectedString: String) : View.OnClickListener{
        override fun onClick(p0: View?) {
            var uri : Uri
            var uriStr : String
            when(selectedString){
                calledFragment.getString(R.string.str_information_list_fragment_official_web) -> {
                    //理工展公式サイト
                    uriStr = "https://www.rikoten.com/"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                calledFragment.getString(R.string.str_information_list_fragment_official_twitter) -> {
                    //公式twitter
                    uriStr = "https://twitter.com/rikoten_waseda"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                calledFragment.getString(R.string.str_information_list_fragment_official_fb) -> {
                    //公式facebook
                    uriStr = "https://www.facebook.com/WasedaRikoten/"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                calledFragment.getString(R.string.str_information_list_fragment_committee_web) -> {
                    //理工展連絡会の公式ページ
                    uriStr = "https://circle.rikoten.com/"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                calledFragment.getString(R.string.str_information_list_fragment_official_it_twitter) -> {
                    //理工展開発班　Twitter
                    uriStr = "https://twitter.com/RikotenApp_pub"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                calledFragment.getString(R.string.str_information_list_fragment_version) -> {
                    //バージョン確認ダイヤログ
                    val latest_version : String = DataFromFireBase.AppLatestVersion
                    val this_app_version : String = AppInfo.AppVersion
                    //最新バージョンかどうか
                    val showing_message_by_version : String = if(latest_version == this_app_version)
                        _context.getString(R.string.str_version_verification_is_latest)
                    else
                        _context.getString(R.string.str_version_verification_is_not_latest)

                    //TODO 最新バージョンじゃなかったらIntentでも飛ばして更新してもらうように促す。
                    showNotificationDialogInFragment(calledFragment,
                        null,
                        "バージョン確認。\nお使いの理工展アプリのバージョンは${this_app_version}です。\n現在の最新版の理工展アプリのバージョンは${latest_version}です。\n"
                    + showing_message_by_version)

                }
                calledFragment.getString(R.string.str_information_list_fragment_terms) -> {
                    val transaction = calledFragment.fragmentManager?.beginTransaction()
                    val nxt_fragment = ShowingPlainTextFragment()
                    nxt_fragment.let {
                        val addData: Bundle = Bundle()
                        addData.putString(
                            "text",
                            calledFragment.getString(R.string.str_term_of_use)
                        )
                        it.arguments = addData
                    }
                    transaction?.replace(fragment_container.id, nxt_fragment)
                    //外周から真ん中へ遷移するアニメーションの設定
                    transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    transaction?.addToBackStack("")
                    transaction?.commit()
                }
                calledFragment.getString(R.string.str_information_list_fragment_policy) -> {
                    val transaction = calledFragment.fragmentManager?.beginTransaction()
                    val nxt_fragment = ShowingPlainTextFragment()
                    nxt_fragment.let {
                        val addData: Bundle = Bundle()
                        addData.putString(
                            "text",
                            calledFragment.getString(R.string.str_privacy_policy)
                        )
                        it.arguments = addData
                    }
                    transaction?.replace(fragment_container.id, nxt_fragment)
                    //外周から真ん中へ遷移するアニメーションの設定
                    transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    transaction?.addToBackStack("")
                    transaction?.commit()
                }
                calledFragment.getString(R.string.str_information_list_fragment_form) -> {
                    //Google Forumに飛ばす
                    uriStr = "https://docs.google.com/forms/d/e/1FAIpQLSdfC56nPatDpmjKHsyeuto3dsr6Fq3wiA_-1aRvRexf04Vhig/viewform"
                    uri = Uri.parse(uriStr)
                    val intent = Intent(ACTION_VIEW, uri)
                    //startActivity(_context, intent, Bundle())
                    startActivityWithSavingData_my_fragment(calledFragment, intent)
                }
                else -> {
                    //ここに来るというのは今までのどれにも該当しないから。
                    //Typoで条件に引っかからなかった可能性が高い
                    Toast.makeText(
                        _context,
                        "ボタンの名前とListener側のコードの不一致が原因のバグが起きてます。",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}