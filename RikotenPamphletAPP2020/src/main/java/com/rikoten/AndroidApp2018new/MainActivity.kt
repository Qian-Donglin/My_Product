package com.rikoten.AndroidApp2018new

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.rikoten.AndroidApp2018new.Info.InformationListFragment
import com.rikoten.AndroidApp2018new.Projects.ModeChoosingProjectsFragment
import com.rikoten.AndroidApp2018new.TimeTable.TimeTableFragment
import com.rikoten.AndroidApp2018new.Top.TopFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.*
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInActivity
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showNotificationDialogInFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showYesNoDialogInActivity
import com.rikoten.AndroidApp2018new.SharingMaterial.Fragment.showYesNoDialogInFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rikoten.AndroidApp2018new.SharingMaterial.InitialOperationPhase

//TODO 戻るボタンを押してもToolbarには反映されないバグを修正
//TODO URI経由のIntent起動が頻出なので関数にまとめる。

//Phase関連はAppConstants.ktに定義されている。
//今回のPhase関連はMainActivityから起動する。

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //BNV関連のセット
        setBNVData()
        switchInitialPhase()
    }

    override fun onResume(){
        super.onResume()
    }

    private fun setBNVData() : Unit{

        val BNV = findViewById<BottomNavigationView>(R.id.main_bnv)
        val toolbar = findViewById<Toolbar>(R.id.main_top_title_tb)

        //最初の画面はTopから始まり、その際上のツールバーのタイトルもTopとする。
        supportFragmentManager.beginTransaction().
        replace(R.id.main_draw_frameLayout, TopFragment())
            .commit()
        toolbar.setTitle(getString(R.string.str_bnv_title_Top))
        //上のサポートバーで戻るボタンを出す
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        BNV.setOnNavigationItemSelectedListener(
            object : BottomNavigationView.OnNavigationItemSelectedListener{
                private var selectedFragment : Fragment? = null
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when(item.itemId){
                        R.id.bnv_top -> {
                            selectedFragment = TopFragment()
                            toolbar.setTitle(getString(R.string.str_bnv_title_Top))
                        }
                        R.id.bnv_project -> {
                            selectedFragment = ModeChoosingProjectsFragment()
                            toolbar.setTitle(getString(R.string.str_bnv_title_Projects))
                        }
                        R.id.bnv_time_table -> {
                            selectedFragment = TimeTableFragment()
                            toolbar.setTitle(getString(R.string.str_bnv_title_Time_Table))
                        }
                        R.id.bnv_info -> {
                            selectedFragment = InformationListFragment()
                            toolbar.setTitle(getString(R.string.str_bnv_title_Info))
                        }
                        else -> {
                            selectedFragment = TopFragment()
                            toolbar.setTitle(getString(R.string.str_bnv_title_Top))
                        }
                    }
                    //selectedFragmentは必ずtrueになる
                    //上のwhen文で必ずなにかしらを代入するので。

                    if(item.itemId == R.id.bnv_time_table) {
                        if(DataFromFireBase.DoRikotenHeld){
                            //理工展時間内なら、URIを飛ばす。
                            val uriStr = "https://rikoten.com/timetable/"
                            val uri = Uri.parse(uriStr)
                            val intent = Intent(ACTION_VIEW, uri)
                            //startActivity(intent)
                            startActivityWithSavingData_my_activity(this@MainActivity, intent)
                        }
                        else {
                            //時間外なら、できない旨のダイヤログを出す。
                            showNotificationDialogInActivity(this@MainActivity,
                                titleStr = getString(R.string.str_general_interface_verification_title),
                                messageStr = getString(R.string.str_time_table_ng_notification_detail),
                                reaction = {})
                        }

                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_draw_frameLayout, selectedFragment!!)
                        .commit()
                    setActionBar(toolbar)
                    return true
                }
            }
        )
    }

    //初回アンケート、利用規約などの同意のフェーズの切り替え
    private fun switchInitialPhase(){
        when(AppInfo.MLD_InitialOperationPhaseInfo.value){
            InitialOperationPhase.TOS -> {
                //利用規約に同意してもらう
                showYesNoDialogInActivity(this,
                    titleStr = getString(R.string.str_initial_TOS_title),
                    messageStr = getString(R.string.str_term_of_use),
                    reaction_positive = {
                        AppInfo.TOS_Permission = true
                        //プライバシーポリシーの同意に移る
                        AppInfo.MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.PP
                        //随時ファイルに現時点での許可状況を書き込む。
                        AppInfo.writeUserName(applicationContext)
                        this.switchInitialPhase()
                    },
                    reaction_negative = {
                        showNotificationDialogInFragment(it,
                            titleStr = getString(R.string.str_initial_TOS_title),
                            messageStr = getString(R.string.str_initial_TOS_content_ng),
                            reaction = {
                                this@MainActivity.finish()
                            }
                        )
                    }
                )

            }

            InitialOperationPhase.PP -> {
                //プライバシーポリシーに同意してもらう
                showYesNoDialogInActivity(this,
                    titleStr = getString(R.string.str_initial_PP_title),
                    messageStr = getString(R.string.str_privacy_policy),
                    reaction_positive = {
                        AppInfo.PP_Permission = true
                        //プライバシーポリシーの同意に移る
                        AppInfo.MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.FirstQuestionnaire
                        //随時ファイルに現時点での許可状況を書き込む。
                        AppInfo.writeUserName(applicationContext)
                        this.switchInitialPhase()
                    },
                    reaction_negative = {
                        showNotificationDialogInFragment(it,
                            titleStr = getString(R.string.str_initial_PP_title),
                            messageStr = getString(R.string.str_initial_PP_content_ng),
                            reaction = {
                                this@MainActivity.finish()
                            }
                        )
                    }
                )
            }

            InitialOperationPhase.FirstQuestionnaire -> {
                val questionnaire = InitialQuestionnaireDialog()
                questionnaire.show(supportFragmentManager, "InitialQuestionnaireDialog")
            }

            InitialOperationPhase.Usual -> {

            }

            //それ以外の時(Usual、あと現状ではUpdateRequestも)はなにもしない
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppInfo.setupOnCreate(applicationContext, this)
        AppInfo.setupOnStart(applicationContext, this)
        DataFromFireBase.update(applicationContext)
        Log.d("追跡", "Actvityから戻った")
        Thread.sleep(2000)
    }

    override fun onDestroy() {
        AppInfo.writeFavoriteProjects(applicationContext)
        AppInfo.writeLookedProjectsHistory(applicationContext)
        super.onDestroy()
    }

}
