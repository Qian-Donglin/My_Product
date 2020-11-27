package com.rikoten.AndroidApp2018new.SharingMaterial

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

//セーブしてからIntentを飛ばす
//onActivityResult()を呼びたいので、無理やりこっちで呼ぶ

fun startActivityWithSavingData_my_activity(activity : Activity, intent : Intent) {

    AppInfo.writeUserName(activity.applicationContext)
    AppInfo.writeLookedProjectsHistory(activity.applicationContext)
    AppInfo.writeFavoriteProjects(activity.applicationContext)
    activity.startActivityForResult(intent, 0)
}

fun startActivityWithSavingData_my_fragment(fragment : Fragment, intent : Intent) {

    AppInfo.writeUserName(fragment.context!!)
    AppInfo.writeLookedProjectsHistory(fragment.context!!)
    AppInfo.writeFavoriteProjects(fragment.context!!)
    fragment.startActivityForResult(intent, 0)
}