package com.rikoten.AndroidApp2018new.SharingMaterial

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.NewsProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TopFlipperItemProperty
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TimeTableEventProperty
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

//インスタンス化はせずに、データを保持するだけのクラス　データクラスにした方がよかったのかな・・・？
//アプリを起動した最初に *必ず* update()を実行すること。
//TODO 最初の全データのロードに時間がかかるらしいので、終わるまでローディングバーとか、くるくるとかを実装したい。
//TODO 取得終わったら、のリスナーの最後にfinish_cntを+1させるようにして、取得する全データの数になったらローディング終わらせるとか
//TODO 実を言うとTopFlipperのところで、Glideの読み込みをするスレッドを起動させながら、それの終わりを待たずしてCloud Firestoreからの読み込み終了だけを待つ状態になる。
//TODO 割と大事なので早急に改善したいが言うほど画像ないしver1.0リリース後でもよさそう。

abstract class DataFromFireBase {

    companion object{

        //Javaでいうとstatic修飾子がcompanion object
        private val StrDefaultValue = "default_str"

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        //Cloud Firestoreから取得するデータのリスナー数の総和
        val DownloadDataCategoryNumber : Int = 5
        //上記の現時点における値
        var DownloadDataCategoryCurrent : Int = 0
        //DownloadDataCategoryCurrentを監視するMutableLiveData<Int>
        var MLD_DownloadDataCategoryCurrent : MutableLiveData<Int> = MutableLiveData()
        //アプリの最新バージョン
        var AppLatestVersion : String = StrDefaultValue
        //Firebaseにあるプロジェクトのデータの配列
        var ProjectsData : MutableList<ProjectsProperty> = mutableListOf()
        //すべてのプロジェクトのタグ名の配列
        var ProjectsTags : List<String> = listOf()
        //すべてのTopFlipperのデータ
        var TopFlipperData : MutableList<TopFlipperItemProperty> = mutableListOf()
        //すべてのNewsのデータ
        var NewsData : MutableList<NewsProperty> = mutableListOf()
        //タイムテーブルでの表示するイベントのデータ
        //二次元配列でTimeTableEventData[i][j] := i(0-idx)日目のj個目の企画のデータ
        var TimeTableEventData : MutableList<MutableList<TimeTableEventProperty>> = mutableListOf()
        //理工展が始まっているか？
        var DoRikotenHeld : Boolean = false

        fun update(context : Context) : Unit {
            MLD_DownloadDataCategoryCurrent.value = 0//最初に0とセットする。

            //アプリの最新バージョン取得

            AppLatestVersion = ""
            db.collection("AppVersion").document("Android")
            .get()
                .addOnSuccessListener { document ->
                    if(document != null){
                        AppLatestVersion = document.data!!["version"] as String
                    }
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }
                .addOnFailureListener {exception ->
                    AppLatestVersion = "アプリのバージョン受け取り失敗"
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }


            ProjectsData.clear()
            //企画データを一斉取得
            db.collection("ProjectsData")
                .get()
                .addOnSuccessListener { result ->
                    val set_for_tags : MutableSet<String> = mutableSetOf()
                    for (document in result) {
                        if (document != null) {
                            //document.dataはMap<String、Any?>なので、これをもとに作ったコンバータでProjectsProperty型にして格納する。
                            //最初の6つは多言語対応されてるので、作ったコンバータを使用してString?で取得する。
                            //コンバータはこのクラスの後に書かれている。
                            //注意！　
                            val tags = findTagedStringArrayValue(document.data!!, "tags")
                            try {
                                ProjectsData.add(
                                    ProjectsProperty(
                                        //ドキュメントの名前を取得
                                        document.id as String?,
                                        findTagedStringValue(document.data!!, "title"),
                                        findTagedStringValue(document.data!!, "subtitle"),
                                        findTagedStringValue(document.data!!, "description"),
                                        findTagedStringValue(
                                            document.data!!,
                                            "description_detailed"
                                        ),
                                        findTagedStringValue(document.data!!, "group"),
                                        findTagedStringValue(document.data!!, "group_description"),
                                        document.data["icon"] as String?,
                                        document.data["large_image"] as String?,
                                        document.data["group_website"] as String?,
                                        document.data["twitter"] as String?,
                                        document.data["facebook"] as String?,
                                        document.data["instagram"] as String?,
                                        document.data["rikoten_web_link"] as String?,
                                        (document.data["virtual_rikoten_location"] as Long?)?.toInt(),
                                        tags,
                                        document.data["isLive"] as Boolean
                                    )
                                )
                            }
                            catch(e : java.lang.ClassCastException){
                                Log.e("AppException", "企画データのCloud Fire Storeからの読み込みでの型キャストでエラーが起きました。")
                                Log.e("AppException", "企画idは、" + document.id)
                            }
                            for(tagStr in tags!!) {
                                set_for_tags.add(tagStr)
                            }
                        }
                    }
                    //企画説明、企画詳細、グループ説明で改行がある場合、そのままだとescape sequenceとなるので、適宜replaceする。
                    for(project in ProjectsData){
                        project.description = project.description?.replace("\\n", "\n")
                        project.description_detailed = project.description_detailed?.replace("\\n", "\n")
                        project.group_description = project.group_description?.replace("\\n", "\n")
                    }
                    //重複要素を消すデータ構造setを利用している。
                    ProjectsTags = set_for_tags.toList()
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }
                .addOnFailureListener{exception ->
                    Log.d("FirebaseLog", "Firestoreのプロジェクトデータの取得error。")
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }

            TopFlipperData.clear()
            db.collection("TopFlipper")
                .get()
                .addOnSuccessListener {result ->
                    for(document in result){
                        TopFlipperData.add(
                            TopFlipperItemProperty(
                                context,
                                itemId = document.data["itemId"] as String?,
                                transition_type = document.data["transition_type"] as String?,
                                destination = document.data["destination"] as String?,
                                order = (document.data["order"] as Long).toInt(),
                                image_location = document.data["image_location"] as String?
                            )
                        )
                    }
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }
                .addOnFailureListener{exception ->
                    Log.d("FirebaseLog", "FirestoreのTopFlipperのデータの取得error。")
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }

            //お知らせデータの取得
            NewsData.clear()
            db.collection("News").document("Android")
                .collection("NewsList")
                .get()
                .addOnSuccessListener {result ->
                    for(document in result){
                        NewsData.add(
                            NewsProperty(
                                title = findTagedStringValue(document.data, "title"),
                                detail = findTagedStringValue(document.data, "detail"),
                                icon_location = document.data["icon_location"] as String,
                                timestamp = document.data["timestamp"] as String,
                                order = (document.data["order"] as Long).toInt()
                            )
                        )
                    }
                    //NewsDataの\nを置換
                    for(news in NewsData){
                        news.detail = news.detail?.replace("\\n", "\n")
                    }
                    Log.d("追跡", "お知らせ取得終了")
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }
                .addOnFailureListener{
                    Log.d("FirebaseLog", "FirestoreのNewsListのデータの取得error。")
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }
            //お知らせデータをorderが大きい順にソート
            NewsData.sortedByDescending { it.order }

            DoRikotenHeld = false
            db.collection("Rikoten").document("isHeld")
                .get()
                .addOnSuccessListener {document ->
                    DoRikotenHeld = document.data!!["isHeld"] as Boolean
                    DownloadDataCategoryCurrent++
                    MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                }

            /*
            //タイムテーブルのデータを取得
            TimeTableEventData.clear()
            val target_days = arrayOf<String>("day1", "day2")
            for(i in 0 until 2)TimeTableEventData.add(mutableListOf())
            for(day in target_days){
                val ref = db.collection("TimeTableData").document("Android")
                    .collection(day).get()
                    .addOnSuccessListener { result ->
                        //何日目か
                        val day_cnt = day.substring(3 until day.length).toInt() - 1//1-idxから0-idxに直す
                        for(document in result){
                            TimeTableEventData[day_cnt].add(
                                TimeTableEventProperty(
                                    project_name = findTagedStringValue(document.data!!, "project_name") ?: "",
                                    group_name = findTagedStringValue(document.data!!, "group_name") ?: "",
                                    start_timestamp = document.data["start_timestamp"] as String,
                                    end_timestamp = document.data["end_timestamp"] as String,
                                    linked_project_name = document.data["linked_project_name"] as String,
                                    lane_location_num = (document.data["lane_location_num"] as Long ?: 0).toInt()
                                )
                            )
                        }
                        DownloadDataCategoryCurrent++
                        MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                    }
                    .addOnFailureListener {
                        Log.d("FirebaseLog", "Firestoreの基本的のデータの取得error。")
                        DownloadDataCategoryCurrent++
                        MLD_DownloadDataCategoryCurrent.value = DownloadDataCategoryCurrent
                    }
                
            }

             */

        }
        //String型の「Storage内のデータの場所」から、Firebase Storageへのリファレンスを返す。
        public fun getFirebaseStorageReference(str : String) : StorageReference{
            return storage.reference.child(str)
        }

    }
}

//Document直下のMap<String!, Any?>と取得したい項目の名前を入力することで、
//存在するのならその項目の多言語データが含まれているStringを返す
//含まれてないならnullが返る。
fun findTagedStringValue(datalist : Map<String, Any?>, tagstr : String) : String? {
    for ((k, v) in datalist) {
        //kに_がない場合は、そもそも多言語対応のキーではないのでcontinueする
        if (!k.contains('_')) continue
        //firestoreのキーとマッチングするために、与えられた使用言語、tagstrのデータから想定されるキーと比較して、
        //それと一致したのならそのタグの値が求めようとしているfirestore側の格納されたデータである。

        var matchingKey: String
            matchingKey = tagstr + "_" + AppInfo.AppUsedLanguage

        if (k == matchingKey) {
            return v as String
        }
    }

    //マッチするタグが見つからなかった
    return null
}

//さきほどのArray<String>版
fun findTagedStringArrayValue(datalist : Map<String, Any?>, tagstr : String) : List<String>? {
    for ((k, v) in datalist) {
        //kに_がない場合は、そもそも多言語対応のキーではないのでcontinueする
        if (!k.contains('_')) continue
        //firestoreのキーとマッチングするために、与えられた使用言語、tagstrのデータから想定されるキーと比較して、
        //それと一致したのならそのタグの値が求めようとしているfirestore側の格納されたデータである。

        var matchingKey: String
        matchingKey = tagstr + "_" + AppInfo.AppUsedLanguage

        if (k == matchingKey) {
            return v as List<String>
        }
    }
    //マッチするタグが見つからなかった
    return null
}