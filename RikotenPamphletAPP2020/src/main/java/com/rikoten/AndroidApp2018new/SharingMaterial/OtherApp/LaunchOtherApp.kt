package com.rikoten.AndroidApp2018new.SharingMaterial.OtherApp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showYesNoDialogInFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.StringToUriConverter
import com.rikoten.AndroidApp2018new.SharingMaterial.startActivityWithSavingData_my_fragment
import java.lang.NullPointerException


class LaunchOtherApp{
    companion object {

        //AR矢口くんを起動
        fun LaunchARYaguchi(calledFragment : Fragment){
            try{
                val pm = calledFragment.context?.packageManager
                val intent = Intent(pm?.getLaunchIntentForPackage("com.rikoten.ARYaguchi02"))
                //startActivity(calledFragment.context!!, intent, Bundle())
                startActivityWithSavingData_my_fragment(calledFragment, intent)
            }
            /*
            catch (e : ActivityNotFoundException){
                //Noの時はなのも反応しないように
                //おそらく、外筒アプリがないときはこれではなく下のNullPointerExceptionが出ると思われる。
                showYesNoDialogInFragment(calledFragment,
                    titleStr = calledFragment.getString(R.string.str_top_aryaguchi_notification_title),
                    messageStr = calledFragment.getString(R.string.str_top_aryaguchi_notification_content_ng),
                    reaction_positive = {
                        val uri: Uri = Uri.parse("https://play.google.com/store/apps/details?id=com.rikoten.ARYaguchi02")
                        val intentToOpenURL = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(it.context!!, intentToOpenURL, Bundle())
                    }
                )
            }
            */
            catch (e : NullPointerException){

                //TODO アプリがないときはNullPointerExceptionが出るけどこの措置で本当にいいんだろうか
                //パッケージ名を含んだ完全修飾名に相当する起動する外部アプリがなければ、
                //PackageManager?.getLaunchIntentForPackage("com.rikoten.ARYaguchi02")は""の中が見つからなければ、
                //Intent(別のIntent)でのIntentのコンストラクタにnullを渡してしまい、nullPointerExceptionとなるため、
                //このようにcatchしている。
                //Noの時はなのも反応しないように
                showYesNoDialogInFragment(calledFragment,
                    titleStr = calledFragment.getString(R.string.str_top_aryaguchi_notification_title),
                    messageStr = calledFragment.getString(R.string.str_top_aryaguchi_notification_content_ng),
                    reaction_positive = {
                        val uri: Uri = Uri.parse("https://play.google.com/store/apps/details?id=com.rikoten.ARYaguchi02")
                        val intentToOpenURL = Intent(Intent.ACTION_VIEW, uri)
                        //startActivity(it.context!!, intentToOpenURL, Bundle())
                        startActivityWithSavingData_my_fragment(calledFragment, intentToOpenURL)
                    }
                )
            }

        }

        //virtual理工展Appを起動
        fun LaunchVirtualRikotenApp(calledFragment : Fragment, location : Int? = null){
            try {
                var intent: Intent = Intent()
                if (location == null) {
                    val pm = calledFragment.context?.packageManager
                    intent =
                        Intent(pm?.getLaunchIntentForPackage("com.RikotenRenrakukai.VirtualRikoten"))
                } else {
                    intent = Intent(
                        ACTION_VIEW, StringToUriConverter(
                            "unitydl://mylink?ClassroomScene?" + location.toString()
                        )
                    )
                    intent.setPackage("com.RikotenRenrakukai.VirtualRikoten")
                }
                //startActivity(calledFragment.context!!, intent, Bundle())
                startActivityWithSavingData_my_fragment(calledFragment, intent)
            }
            catch (e : NullPointerException){

                showYesNoDialogInFragment(calledFragment,
                    titleStr = calledFragment.getString(R.string.str_top_virtual_rikoten_notification_title),
                    messageStr = calledFragment.getString(R.string.str_top_virtual_rikoten_notification_content_ng),
                    reaction_positive = {
                        val uri: Uri = Uri.parse("https://play.google.com/store/apps/details?id=com.RikotenRenrakukai.VirtualRikoten")
                        val intentToOpenURL = Intent(Intent.ACTION_VIEW, uri)
                        //startActivity(it.context!!, intentToOpenURL, Bundle())
                        startActivityWithSavingData_my_fragment(calledFragment, intentToOpenURL)
                    }
                )
            }

        }

    }
}