package com.rikoten.AndroidApp2018new.SharingMaterial

import android.app.Activity
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.rpc.context.AttributeContext
import java.io.*
import java.util.*

/*
UserName.txtの中身は3行によってからなる。
利用規約に同意したかという真偽値
プライバシーポリシーに同意したかという真偽値
初回アンケートに答えたかという真偽値
*/

/*初回アンケート、利用規約、プライバシーポリシー、アップデート要求をenumとして定義して、MutableLiveDataに入れて使うことにする。
初期処理フェーズ
段階としては、
利用規約の同意(TOS)
    ↓
プライバシーポリシーの同意
    ↓
(新バージョンの有無確認 TODO これはファーストリリースでは実装しない)
    ↓
初回アンケート
    ↓
平常時              

*/
enum class InitialOperationPhase{
    TOS,                    //Terms Of Service 利用規約
    PP,                     //Privacy Policy　プライバシーポリシー
    FirstQuestionnaire,     //初回アンケート
    UpdateRequest,          //新バージョンへの更新要求
    Usual                   //平常時
}

//アプリのバージョン情報などの定数の保存場所
abstract class AppInfo {
    companion object {

        //Firebase認証
        private lateinit var Auth_Firebase: FirebaseAuth

        //現在のアプリのバージョン
        val AppVersion: String = "3.2.0"

        //アプリの使用してる言語
        val AppUsedLanguage: String = java.util.Locale.getDefault().language

        //アプリの用意している言語一覧　今のところ使う予定なし
        val AppPreparedLanguage: Array<String> = arrayOf("ja", "en")

        //アプリのフェーズについて。このフェーズによって利用規約の同意を求める、などの操作を行う。
        var MLD_InitialOperationPhaseInfo : MutableLiveData<InitialOperationPhase> = MutableLiveData()

        //利用規約、プライバシーポリシーの同意状況および初回アンケートを答えたかどうか
        //ここ二ある値を使うときは、確認状況のファイルが未作成のとき、つまり初回利用時。
        var TOS_Permission : Boolean = false
        var PP_Permission : Boolean = false
        var AnswerInitialQuestionnaire : Boolean = false
        var UserName : String = ""
        val UserNameFileName : String = "UserName.txt"
        val UsernameLength : Int = 30

        //理工展開催期間
        val RikotenHeldingSpanDays = 2

        //お気に入り情報
        var FavoriteProjects: MutableList<String> = mutableListOf()
        val FavoriteProjectsDataFileName: String = "favorite_projects.txt"

        //前にどれを見たのかのヒストリー情報
        val ProjectHistoryStoredNumber = 20//ヒストリーは最大で20件まで記憶
        var LookedHistoryProjects: Queue<String> = LinkedList<String>()
        val LookedHistoryProjectsDataFileName: String = "looked_history_projects.txt"

        //「ランダムで見る」で出てくるプロジェクトの数
        val RandomProjectsNumber = 3

        //「人気の企画を見る」で出てくるプロジェクト数
        val PopularProjectsNumber = 10

        //端末の画面サイズ setup()で取得する。
        var ScreenWidthPX: Int = 0
        var ScreenHeightPX: Int = 0
        //企画一覧リスト画面での、団体名と企画名の横幅の長さの定数 setup()内で定義

        var ProjectsItemListStringWidthPX = 0

        val OS_Version : String = Build.VERSION.RELEASE ?: "Cannot get version data"

        val FirstWaitingMiliSecond = 2000

        //最初にこれをonCreate()を実行すること
        fun setupOnCreate(context: Context, activity: Activity) {
            Log.d("追跡", "読む")
            //各種Flag情報を読み込む
            readUserName(context)
            //お気に入りの情報を読み込む
            readFavoriteProjects(context)
            //ヒストリー情報を読み込む
            readLookedProjectsHistory(context)

            //画面の画素の密度を取得する。(pxとdpの変換に使うため)
            px_dp_Convert.coefficient = context.resources.displayMetrics.density.toDouble()

            //画面サイズ情報の記述
            //TODO ここ非推奨のものを使ってる。時間があったら直したい。
            val size = Point().also {
                (context.getSystemService(WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay.apply { getSize(it) }
            }

            ScreenWidthPX = size.x
            ScreenHeightPX = size.y

            /*
            それぞれ、
            icon左のmargin
            iconの横の長さ
            icon右のmargin
            fav_icon左のmargin
            fav_iconの横の長さ
            fav_icon右のmargin
            に対応している。
            単位は *dp*
              */
            //一度px->dpにしてから、dp->pxにまたしている
            ProjectsItemListStringWidthPX =
                px_dp_Convert.convert_dp_to_px(
                px_dp_Convert.convert_px_to_dp(ScreenWidthPX) -
                        (12 + 70 + 16 + 32 + 50 + 16)
                )
            //Cloud Fire storeのデータ取得のための匿名アカウントの設定をする。
            Auth_Firebase = FirebaseAuth.getInstance()

        }

        //onStart()でのセットアップ。他のすべての関数よりも優先して呼び出すこと。
        fun setupOnStart(context: Context, activity: Activity){
            val currentUser = Auth_Firebase.currentUser
            Auth_Firebase.signInAnonymously()
                .addOnCompleteListener(activity,
                    object : OnCompleteListener<AuthResult?> {
                    override fun onComplete(p0: Task<AuthResult?>) {
                        if (p0.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("", "signInAnonymously:success")
                            val user: FirebaseUser? = Auth_Firebase.getCurrentUser()
                            //Log.d("MAinActivity", user.toString())
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w("MAinActivity", "signInAnonymously:failure", p0.exception)
                        }
                    }
                })
            //TODO ここでは強制的に待たせているけど、もちろん本来ならばマルチスレッドでjoinを待つ方がいい。
            //ファーストリリースに間に合わなかった。これから絶対に改善したい。
            Thread.sleep(AppInfo.FirstWaitingMiliSecond.toLong())
        }

        //プライバシーポリシー、利用規約の同意情報、アンケートに回答済みか、ユーザーIDの読み込み。
        //TODO ユーザーIDは非常に低確率で衝突するかも　非常に低確率だが
        fun readUserName(context: Context) {
            LookedHistoryProjects.clear()
            try {
                val file = File(context.filesDir, UserNameFileName)
                //ファイルが作られてない場合は作成してifを素通りする。作られてる場合はif文の中を実行する。
                if (!file.createNewFile()) {
                    val br = BufferedReader(FileReader(file))
                    var bufStr: String? = br.readLine()
                    var cnt = 0
                    while (bufStr != null) {
                        Log.d("追跡", "ファイルk行目あるよ")
                        if(cnt == 0){
                            TOS_Permission = bufStr.toBoolean()
                        }
                        if(cnt == 1) {
                            PP_Permission = bufStr.toBoolean()
                        }
                        if(cnt == 2) {
                            AnswerInitialQuestionnaire = bufStr.toBoolean()
                        }
                        if(cnt == 3){
                            UserName = bufStr.toString()
                        }
                        bufStr = br.readLine()
                        cnt++
                    }
                    br.close()
                }
                //読み込みが終えたら、そのUserName.txtから読み込んだ、もしくは存在せずにデフォルトのままのデータから判断する。

                if(!TOS_Permission){
                    MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.TOS
                }
                else if(!PP_Permission){
                    MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.PP
                }
                else if(!AnswerInitialQuestionnaire){
                    MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.FirstQuestionnaire
                }
                else {
                    MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.Usual
                }
            } catch (e: IOException) {
                Log.e("AppFileError", "利用規約やプライバシーポリシーの同意情報の読み込み中にIOExceptionが発生しました。")
            } catch (e: FileNotFoundException) {
                Log.e("AppFileError", "利用規約やプライバシーポリシーの同意情報を読み込む際に、読み込み対象のターゲットのファイルが存在しない。")
            }
        }

        fun writeUserName(context: Context) {
            try {
                val file = File(context.filesDir, UserNameFileName)
                val bw = BufferedWriter(FileWriter(file))
                //すでにある各種許可情報を全部捨てて、新しい情報を上書きする。
                bw.write(TOS_Permission.toString())
                Log.d("writeLog", TOS_Permission.toString())
                bw.newLine()
                bw.write(PP_Permission.toString())
                Log.d("writeLog", PP_Permission.toString())
                bw.newLine()
                bw.write(AnswerInitialQuestionnaire.toString())
                Log.d("writeLog", AnswerInitialQuestionnaire.toString())
                bw.newLine()
                bw.write(UserName)
                Log.d("writeLog", UserName)
                bw.newLine()

                bw.close()
            } catch (e: IOException) {
                Log.e("AppFileError", "利用規約やプライバシーポリシーの同意情報の書き込み中にIOExceptionが発生しました。")
            }
        }

        fun readFavoriteProjects(context: Context) {
            //今アプリ内にあるすべてのお気に入り情報を削除して、端末に保存してる情報を準拠する。
            FavoriteProjects.clear()
            try {
                val file = File(context.filesDir, FavoriteProjectsDataFileName)
                if (!file.createNewFile()) {
                    //ファイルが作られてない場合は作成してifを素通りする。作られてる場合はif文の中を実行する。
                    val br = BufferedReader(FileReader(file))
                    var bufStr: String? = br.readLine()
                    while (bufStr != null) {
                        Log.d("abcdefg", "read:" + bufStr)
                        FavoriteProjects.add(bufStr)
                        bufStr = br.readLine()
                    }
                    br.close()
                }
            } catch (e: IOException) {
                Log.e("AppFileError", "お気に入り情報の読み込み中にIOExceptionが発生しました。")
            } catch (e: FileNotFoundException) {
                Log.e("AppFileError", "お気に入り情報を読み込む際に、読み込み対象のターゲットのファイルが存在しない。")
            }
        }

        fun writeFavoriteProjects(context: Context) {
            try {
                val file = File(context.filesDir, FavoriteProjectsDataFileName)
                val bw = BufferedWriter(FileWriter(file))
                //すでにあるお気に入り情報を全部捨てて、新しい情報を上書きする。
                for (projectId in FavoriteProjects) {
                    bw.write(projectId)
                    bw.newLine()
                }
                bw.close()
            } catch (e: IOException) {
                Log.e("AppFileError", "お気に入り情報の書き込み中にIOExceptionが発生しました。")
            }
        }

        fun readLookedProjectsHistory(context: Context) {
            LookedHistoryProjects.clear()
            try {
                val file = File(context.filesDir, LookedHistoryProjectsDataFileName)
                //ファイルが作られてない場合は作成してifを素通りする。作られてる場合はif文の中を実行する。
                if (!file.createNewFile()) {
                    Log.d("追跡", "企画履歴を読みだしています")
                    val br = BufferedReader(FileReader(file))
                    var bufStr: String? = br.readLine()
                    while (bufStr != null) {
                        Log.d("追跡", "企画名は" + bufStr)
                        LookedHistoryProjects.add(bufStr)
                        bufStr = br.readLine()
                    }
                    br.close()
                }
            } catch (e: IOException) {
                Log.e("AppFileError", "プロジェクトヒストリーの読み込み中にIOExceptionが発生しました。")
            } catch (e: FileNotFoundException) {
                Log.e("AppFileError", "プロジェクトヒストリーを読み込む際に、読み込み対象のターゲットのファイルが存在しない。")
            }
        }

        fun writeLookedProjectsHistory(context: Context) {
            Log.d("追跡", "書く")
            try {
                val file = File(context.filesDir, LookedHistoryProjectsDataFileName)
                val bw = BufferedWriter(FileWriter(file))
                //すでにあるお気に入り情報を全部捨てて、新しい情報を上書きする。
                Log.d("追跡", "履歴を書いています")
                while (LookedHistoryProjects.size > 0) {
                    bw.write(LookedHistoryProjects.peek())
                    Log.d("追跡", LookedHistoryProjects.peek())
                    bw.newLine()
                    LookedHistoryProjects.remove()
                }
                bw.close()
            } catch (e: IOException) {
                Log.e("AppFileError", "プロジェクトヒストリーの書き込み中にIOExceptionが発生しました。")
            }
        }

    }
}

//pxとdpの変換
class px_dp_Convert{
    companion object {
        var coefficient: Double = 0.0

        //px -> dpの変換
        fun convert_px_to_dp(px : Int) : Int{
            return (px.toDouble() / coefficient).toInt()
        }

        //dp -> pxの変換
        fun convert_dp_to_px(dp : Int) : Int{
            return (dp.toDouble() * coefficient).toInt()
        }
    }
}

//返ってくるデータで処理をするIntentのユニークなIDの保存場所
abstract class IntentID {
    companion object {

    }
}

//端末の様々な機能の許可を取る際に必要なユニークなIDの保存場所
abstract class PermissionID {
    companion object {

    }
}

