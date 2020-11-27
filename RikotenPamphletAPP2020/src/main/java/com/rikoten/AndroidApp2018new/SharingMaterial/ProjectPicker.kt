package com.rikoten.AndroidApp2018new.SharingMaterial

import android.util.Log
import com.rikoten.AndroidApp2018new.SharingMaterial.Algorithm.RandomizedMultiSelection
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty
import com.google.api.ProjectProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.NullPointerException
import java.util.*

class ProjectPicker {
    companion object{
        //変数で与えたList<ProjectsProperty>から、指定した1つタグのみを含む物を抽出し、
        //それをList<ProjectProperty>で返す。
        fun pickProjectOneCondition(projectdata : List<ProjectsProperty>, containTag : String) : List<ProjectsProperty>{
            val ret : MutableList<ProjectsProperty> = mutableListOf()

            for(project in projectdata){
                var iscontain : Boolean = false
                for(tags in project.tags!!){
                    if(tags == containTag) {
                        iscontain = true
                    }
                }
                if(iscontain) {
                    ret.add(project)
                }
            }
            return ret
        }

        fun pickProjectManyConditions(projectdata : List<ProjectsProperty>, containTag : List<String>) : List<ProjectsProperty>{
            val ret : MutableList<ProjectsProperty> = mutableListOf()
            for(project in projectdata){
                var contain_cnt = 0
                //2重ループになってるけど、実用的には計算量的にも大丈夫
                //タグの数をn、企画数をmとしてとしてたかだかO(n^2*m)なので
                for(mustTag in containTag){
                    var iscontain = false
                    for(project_tag in project.tags!!){
                        if(mustTag == project_tag)iscontain = true
                    }
                    if(iscontain)contain_cnt++
                }
                if(contain_cnt == containTag.size)
                    ret.add(project)
            }
            return ret
        }

        //直近の最大20件のヒストリーにある企画をすべて選択
        fun pickProjectHistory(projectdata: List<ProjectsProperty>) : List<ProjectsProperty>{
            val ret : MutableList<ProjectsProperty> = mutableListOf()
            //Queueの中身を見るために、一旦LookedProjectHistory複製する。
            val targetQueue : Queue<String> = LinkedList<String>(AppInfo.LookedHistoryProjects)
            var storedCnt = AppInfo.ProjectHistoryStoredNumber
            while(targetQueue.size > 0 && storedCnt > 0){
                val targetProject = targetQueue.peek()
                for(project in projectdata){
                    if(project.projectId == targetProject)
                        ret.add(project)
                }
                targetQueue.remove()
                storedCnt--
            }
            //Queueの順番なので、古いのが先に来ている　それを逆転する。
            ret.reverse()
            return ret
        }

        //お気に入りの企画をすべて選択
        fun pickProjectFavorite(projectdata : List<ProjectsProperty>) : List<ProjectsProperty>{
            val ret : MutableList<ProjectsProperty> = mutableListOf()
            for(project in projectdata){
                if(AppInfo.FavoriteProjects.contains(project.projectId))
                    ret.add(project)
            }
            return ret
        }

        //ランダム3件の企画を選択、　3件という定数はAppInfoクラスで定義されている
        fun pickProjectRandomized(projectdata : List<ProjectsProperty>, num : Int) : List<ProjectsProperty>{
            val ret = RandomizedMultiSelection(projectdata, num)
            if(ret == null){
                throw NullPointerException("ランダムでプロジェクトを見るで、ランダムで複数選択で正当な個数選択できなかった。")
            }
            return ret
        }

        //Cloud　Fire Storeからトップ10のデータを取ってくる。
        fun pickProjectsPopular(projectdata: List<ProjectsProperty>, num : Int) : List<ProjectsProperty>{
            val ret : MutableList<ProjectsProperty> = mutableListOf()
            val db = FirebaseFirestore.getInstance()
            //降順に10個取得
            db.collection("ProjectsData")
                .orderBy("popularity", Query.Direction.DESCENDING)
                .limit(num.toLong())
                .get()
                .addOnSuccessListener {
                    for(result in it){
                        val projectId = result.id
                        //projectIdである企画をDataFromFirebase.ProjectsDataから線形探索で探す。
                        for(searching_project in DataFromFireBase.ProjectsData){
                            if(searching_project.projectId == projectId){
                                ret.add(searching_project)
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener{
                    Log.e("AppException", "CloudFireStoreのProjectsDataからの人気企画をpopularityの降順で" + num +
                            "個取得する際に、何かしらの例外が起きた。")
                }
            return ret
        }
    }
}