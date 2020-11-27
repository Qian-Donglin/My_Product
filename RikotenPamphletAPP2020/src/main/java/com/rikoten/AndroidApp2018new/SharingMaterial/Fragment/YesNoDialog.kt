package com.rikoten.AndroidApp2018new.SharingMaterial.Fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.rikoten.AndroidApp2018new.R
import java.io.Serializable


//Bundleにタイトルをタグ"Title"、内容の文章を"Message"とBundleでセットして渡して使う。
//なければこちらでデフォルトで設定された文章が表示される。
//タイトルはデフォルトでもいいが、文章の内容は絶対にセットをすること。

//以下のshowNotificationDialog()で簡単に表示させることができる。
//第1引数はactivity　基本的にAppCompatActivityを継承してActivityのクラスが作られるのでこれを渡す
//Fragment内で使うのならば、2つ目の関数(~InFragment())を使って、1つ目ではFragmentActivityを渡す
//第2引数はダイヤログのタイトルとなる。デフォルトのままならnull
//第3引数はダイヤログの内容となる。デフォルトのままならnullだが使い方が非想定なので内容だけはせめて設定すること
//第4引数はダイヤログを押した時の反応となる。デフォルト(null)では無反応。
//内部ではFragmentやラムダ式はSerializableにキャストしてから渡している。

//TODO 押した時のListenerも渡せるように直す。

fun showYesNoDialogInActivity(
    activity: AppCompatActivity,
    titleStr: String?,
    messageStr: String?,
    reaction_positive : ((Fragment) -> Unit)? = null,
    reaction_negative : ((Fragment) -> Unit)? = null,
    cancelable : Boolean = false
) : Unit {
    val dialogfragment = YesNoDialog()
    dialogfragment.let{
        val bundle = Bundle()
        if(titleStr != null)
            bundle.putString("Title", titleStr)
        if(messageStr != null)
            bundle.putString("Message", messageStr)
        if(reaction_positive != null)
            bundle.putSerializable("reaction_positive", reaction_positive as Serializable)
        if(reaction_negative != null)
            bundle.putSerializable("reaction_negative", reaction_negative as Serializable)
        bundle.putBoolean("Cancelable", cancelable)
        it.arguments = bundle
    }
    dialogfragment.show(activity.supportFragmentManager, "NotificationActivity")
}

fun showYesNoDialogInFragment(
    fragmentAbove: Fragment,
    titleStr: String?,
    messageStr: String?,
    reaction_positive : ((Fragment) -> Unit)? = null,
    reaction_negative : ((Fragment) -> Unit)? = null,
    cancelable: Boolean = false
) : Unit {

    val dialogfragment = YesNoDialog()
    dialogfragment.let{
        val bundle = Bundle()
        if(titleStr != null)
            bundle.putString("Title", titleStr)
        if(messageStr != null)
            bundle.putString("Message", messageStr)
        if(reaction_positive != null)
            bundle.putSerializable("reaction_positive", reaction_positive as Serializable)
        if(reaction_negative != null)
            bundle.putSerializable("reaction_negative", reaction_negative as Serializable)
        bundle.putBoolean("Cancelable", cancelable)
        it.arguments = bundle
    }
    dialogfragment.show(fragmentAbove.fragmentManager!!, "NotificationFragment")
}

class YesNoDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)

        this.setCancelable(arguments?.getBoolean("Cancelable")!!)

        val sv = requireActivity().layoutInflater.inflate(R.layout.dialog_content_view, null)
        val tv = sv.findViewById<TextView>(R.id.dialog_content_view_tv)
        tv.autoLinkMask = Linkify.WEB_URLS
        if(arguments?.getString("Message", "the data is empty.") != "the data is empty."){
            //builder.setMessage(arguments?.getString("Message"))
            tv.text = arguments?.getString("Message")
            builder.setView(sv)
        }
        else builder.setMessage("通知の内容が設定されていません")

        if(arguments?.getString("Title", "the data is empty.") != "the data is empty.") {
            builder.setTitle(arguments?.getString("Title"))
        }
        else builder.setTitle("確認")

        builder.setPositiveButton(getString(R.string.str_sharing_dialog_choosing_ok), DialogPositiveButtonClickListener())
        builder.setNegativeButton(R.string.str_sharing_dialog_choosing_ng, DialogNegativeButtonClickListener())

        return builder.create()
    }

    private inner class DialogPositiveButtonClickListener : DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {
            val callback = arguments?.getSerializable("reaction_positive") as ((Fragment) -> Unit)?
            if(callback != null)
                callback?.invoke(this@YesNoDialog)
        }
    }

    private inner class DialogNegativeButtonClickListener : DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {
            val callback = arguments?.getSerializable("reaction_negative") as ((Fragment) -> Unit)?
            if(callback != null)
                callback?.invoke(this@YesNoDialog)
        }
    }
}