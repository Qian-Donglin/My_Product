package com.rikoten.AndroidApp2018new

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import kotlinx.android.synthetic.main.activity_first_wating.*

class FirstWatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_wating)

        DataFromFireBase.MLD_DownloadDataCategoryCurrent.value = 0
        //進捗バーの観測をするリスナーを設定する。
        DataFromFireBase.MLD_DownloadDataCategoryCurrent.observe(this, Observer{response ->
            //進展でなんぶんのなにみたいなのを記述する。
            /*
            val progress_tv = findViewById<TextView>(R.id.first_waiting_progress_tv)
            progress_tv.text = response.toString() + "/" + DataFromFireBase.DownloadDataCategoryNumber.toString()
            */
            //Androidの下のBNの3つ目を押すのを繰り返すとonCreate()だけがやたらと呼ばれていくので、手元に届いてるデータの数は、
            //ピッタリではなく以上で判定。
            if(response >= DataFromFireBase.DownloadDataCategoryNumber){
                //すべてのデータのロードが終わったら。
                /*
                val trans_bt = findViewById<Button>(R.id.first_waiting_start_activity_bt)
                trans_bt.isClickable = true
                trans_bt.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        //次のActivityに遷移する前に自分自身を消す。
                        this@FirstWatingActivity.finish()
                    }
                })
                */

                //メインスレッドを1.5sスリープさせる。　Cloud Fire storeとのロード時間とも合わせた時間を待ってもらうことになる。
                //TODO ロード時間と2sのうち長い方に合わせるやつを時間があったら書きたい
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                //次のActivityに遷移する前に自分自身を消す。
                this@FirstWatingActivity.finish()
            }
        })

        //AppInfoの要素のセットアップ
        AppInfo.setupOnCreate(applicationContext, this)

    }

    override fun onStart() {
        super.onStart()

        AppInfo.setupOnStart(applicationContext, this)

        Log.d("追跡", "アンケート:" + AppInfo.AnswerInitialQuestionnaire)
        Log.d("追跡", "利用規約:" + AppInfo.TOS_Permission)
        Log.d("追跡", "PP:" + AppInfo.PP_Permission)

        DataFromFireBase.update(applicationContext)

    }

}