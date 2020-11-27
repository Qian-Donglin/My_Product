package com.rikoten.AndroidApp2018new.SharingMaterial.Algorithm

import java.util.*

//与えられたListのDataから、無作為にNum個をランダムな順番で取り出す。
//条件を満たせないようなものに関しては、nullを返す
fun <T>RandomizedMultiSelection(Data : List<T>, Num : Int, random : Random = Random()) : List<T>?{
    if(Num < 0 || Num > Data.size)return null
    val ret : MutableList<T> = mutableListOf()
    val Datacpy : MutableList<T> = Data.toMutableList()

    for(i in 0 until Num){
        val rndIdx = random.nextInt(Datacpy.size)
        ret.add(Datacpy[rndIdx])
        if(rndIdx != Datacpy.size - 1){
            Datacpy[rndIdx] = Datacpy[Datacpy.size - 1]
        }
        Datacpy.removeAt(rndIdx)
    }
    return ret
}