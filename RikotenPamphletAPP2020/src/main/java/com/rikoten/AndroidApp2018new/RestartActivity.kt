//https://qiita.com/Shiozawa/items/85f078ed57aed46f6b69　ここを実質的に大いにパクった(中身は理解してる)

package com.rikoten.AndroidApp2018new

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process

class RestartActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. メインプロセスを Kill する
        val intent = intent
        val mainPid = intent.getIntExtra(EXTRA_MAIN_PID, -1)
        Process.killProcess(mainPid)

        // 2. MainActivity を再起動する
        val context = applicationContext
        val restartIntent = Intent(Intent.ACTION_MAIN)
        restartIntent.setClassName(context.packageName, FirstWatingActivity::class.java.name)
        restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(restartIntent)

        // 3. RestartActivity を終了する
        finish()
        Process.killProcess(Process.myPid())
    }

    companion object {
        //プロセスID
        const val EXTRA_MAIN_PID = "RestartActivity.main_pid"
        fun createIntent(context: Context): Intent {
            val intent = Intent()
            intent.setClassName(context.packageName, RestartActivity::class.java.name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // メインプロセスの PID を Intent に保存しておく
            intent.putExtra(EXTRA_MAIN_PID, Process.myPid())
            return intent
        }
    }
}

fun restartApp(context: Context) {
    val intent = RestartActivity.createIntent(context)
    // RestartActivity を起動（AndroidManifest.xml での宣言により別プロセスで起動する
    context.startActivity(intent)
}