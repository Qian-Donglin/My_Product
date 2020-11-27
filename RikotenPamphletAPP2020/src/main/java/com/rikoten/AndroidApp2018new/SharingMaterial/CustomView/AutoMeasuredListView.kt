package com.rikoten.AndroidApp2018new.SharingMaterial.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView

class AutoMeasuredListView(
    @get:JvmName("getContext_") private val context: Context,
    private val attrs : AttributeSet
    ) : ListView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var newH = 0
        val HMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if(HMode == MeasureSpec.EXACTLY){
            newH = measuredHeight
        }else {
            val listAdapter : ListAdapter = adapter
            if(listAdapter != null && !listAdapter.isEmpty){
                //adapterが存在すること
                //この時、Adapterに入ってる要素を全部見て、それらの高さの和をこのListViewの高さの和に再設定する。
                for(listPosition in 0 until listAdapter.count){
                    //その行のView or ViewGroupを取得
                    val listItem = listAdapter.getView(listPosition, null, this)
                    if(listItem is ViewGroup){
                        //ListViewに含まれている要素がまた入れ子状態になってるのなら
                        //子どものサイズに合わせたサイズに設定する。
                        //TODO 一応動くだろうがここの警告、余力があるのなら取りたい
                        listItem.setLayoutParams(LayoutParams(
                            LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        ))
                    }
                    listItem.measure(widthMeasureSpec, heightMeasureSpec)
                    newH += listItem.measuredHeight
                }
                newH += dividerHeight * listAdapter.count
                if((HMode == MeasureSpec.AT_MOST) && newH > heightSize){
                    //AT_MOSTならば超えてしまったらそこに抑える。
                    newH = heightSize
                }
            }
        }
        setMeasuredDimension(measuredWidth, newH)
    }

}