package com.rikoten.AndroidApp2018new.Info

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rikoten.AndroidApp2018new.MainActivity
import com.rikoten.AndroidApp2018new.R

/**
 * A simple [Fragment] subclass.
 */
class InformationListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_information_list, container, false)

        val detail_list_rv = view.findViewById<RecyclerView>(R.id.info_list_rv)
        //ListViewのように縦に並べる
        detail_list_rv.layoutManager = LinearLayoutManager(context)

        //別関数でリストの中身を代入する
        val detailData = makeDetailData()

        detail_list_rv.adapter =
            //container!!ではなくできるならばInfoFragmentと紐づけたxmlレイアウトの方がいいが・・・
            InfoDetailAdapter(detailData, activity!!, this, container!!)
        val decorator = DividerItemDecoration(context, LinearLayoutManager(context).orientation)
        detail_list_rv.addItemDecoration(decorator)

        return view
    }

    fun makeDetailData() : MutableList<MutableMap<String, Any>>{
        val rtn = mutableListOf<MutableMap<String, Any>>()

        var item : MutableMap<String, Any> = mutableMapOf(
            "text" to getString(R.string.str_information_list_fragment_official_urls),
            "isCategory" to true)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_rikoten66thlogo2,
            "text" to getString(R.string.str_information_list_fragment_official_web),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_twitter_logo,
            "text" to getString(R.string.str_information_list_fragment_official_twitter),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_facebook_logo,
            "text" to getString(R.string.str_information_list_fragment_official_fb),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.renrakukai_logo,
            "text" to getString(R.string.str_information_list_fragment_committee_web),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_rikoten_development,
            "text" to getString(R.string.str_information_list_fragment_official_it_twitter),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "text" to getString(R.string.str_information_list_fragment_about),
            "isCategory" to true)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_version_verification_24dp,
            "text" to getString(R.string.str_information_list_fragment_version),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_texticon,
            "text" to getString(R.string.str_information_list_fragment_terms),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_texticon,
            "text" to getString(R.string.str_information_list_fragment_policy),
            "isCategory" to false)
        rtn.add(item)

        item = mutableMapOf(
            "text" to getString(R.string.str_information_list_fragment_contact),
            "isCategory" to true)
        rtn.add(item)

        item = mutableMapOf(
            "icon" to R.drawable.ic_texticon,
            "text" to getString(R.string.str_information_list_fragment_form),
            "isCategory" to false)
        rtn.add(item)

        return rtn
    }

}
