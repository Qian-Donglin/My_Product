package com.rikoten.AndroidApp2018new.SharingMaterial

import android.net.Uri

fun StringToUriConverter(uriStr : String) : Uri{
    return Uri.parse(uriStr)
}