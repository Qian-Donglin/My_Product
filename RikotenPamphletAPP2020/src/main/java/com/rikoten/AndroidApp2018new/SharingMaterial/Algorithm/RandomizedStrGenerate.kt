package com.rikoten.AndroidApp2018new.SharingMaterial.Algorithm

import android.util.Log
import java.util.*

fun RandomizedStrGenerator(length : Int, random : Random = Random()) : String{
    var ret : String = ""
    for(i in 0 until length){
        val rint = random.nextInt(26 + 26 + 10)
        if(rint < 10){
            ret += '0' + rint
        }
        else if(rint < 10 + 26){
            ret += 'a' + (rint - 10)
        }
        else {
            ret += 'A' + (rint - 10 - 26)
        }
        Log.d("追跡", rint.toString() + " " + ret)
    }
    return ret
}