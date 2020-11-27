package com.rikoten.AndroidApp2018new.SharingMaterial.DataType

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase

data class TopFlipperItemProperty(
    var context : Context,
    var itemId : String? = null,
    var transition_type : String? = null,
    var destination : String? = null,
    var order : Int = -1,
    var image_location : String? = null

){
    init{

        Glide.with(context)
            .asBitmap()
            .load(
                DataFromFireBase.getFirebaseStorageReference(image_location!!)
            )
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .preload()

    }

    constructor(context : Context, mapDatabase : Map<String?, Any?>) : this(
        context,
        mapDatabase["itemId"] as String?,
        mapDatabase["transition_type"] as String?,
        mapDatabase["destination"] as String?,
        mapDatabase["order"] as Int,
        mapDatabase["image_location"] as String?
    ){
    }
}