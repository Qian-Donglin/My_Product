package com.rikoten.AndroidApp2018new

import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.google.firebase.firestore.FirebaseFirestore

//初期アンケートの回答結果　このまま送信できる。
data class InitialQuestionnaireAnswerData(
    var age : String,
    var come : String,
    var gender : String,
    var job : String,
    var os : String,
    var reason : String,
    var timestamp : String
){
    fun sendData(){
        val db = FirebaseFirestore.getInstance()
        val viewRef = db.collection("UserData").document(AppInfo.UserName)
        viewRef.set(this)
    }
}