package com.rikoten.AndroidApp2018new.SharingMaterial.DataType

import android.content.Context

data class NewsProperty (
    var title : String? = null,
    var detail : String? = null,
    var icon_location : String? = null,
    var timestamp : String? = null,
    var order : Int? = null
){
    constructor(mapDatabase : Map<String?, Any?>) : this(
        mapDatabase["title"] as String?,
        mapDatabase["detail"] as String?,
        mapDatabase["icon_location"] as String?,
        mapDatabase["timestamp"] as String?,
        mapDatabase["order"] as Int?
    ){
    }
}