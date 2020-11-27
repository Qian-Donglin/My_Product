package com.rikoten.AndroidApp2018new.SharingMaterial.DataType

import android.os.Parcel
import android.os.Parcelable
/*
TimeTableでは、TimeTableEventPropertyがCloud Fire Storeから送られてくる。
ここで、別のTimeTablePeriodクラスを作成する。これは基底クラスであり、これ単体での使用は基本的に考えない。
このTimeTablePeriodクラスを継承して、TimeTableEventPropertyクラスとTimeTableBlankPeriodクラスを作る。
ParseEveneData内のfillWithSpacer()は、TimeTableEventPropertyのListを受け取って、すべてのステージ(列)でのイベントの空白部分も埋める関数である。
具体的には、内部的には入っているイベントなら前者、空白イベントなら後者を渡して、
そのように作られた配列を見て判断して、GroupieのRecycelrViewのItemクラスの割り当てをする。
*/

sealed class TimeTablePeriod (
    open var start_timestamp: String = "",
    open var end_timestamp: String = "",
    open var lane_location_num : Int = 1 //1-idx　何列目に配置するか
)

data class TimeTableBlankPeriod(
    override var start_timestamp: String = "",
    override var end_timestamp: String = "",
    override var lane_location_num: Int = 1
) : TimeTablePeriod(start_timestamp, end_timestamp, lane_location_num)

data class TimeTableEventProperty (
    var project_name : String = "",
    var group_name : String = "",
    var linked_project_name : String = "",
    override var start_timestamp: String = "",
    override var end_timestamp: String = "",
    override var lane_location_num : Int = 1//1-idx　何列目に配置するか

) : TimeTablePeriod(start_timestamp, end_timestamp, lane_location_num), Parcelable{
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(project_name)
        p0?.writeString(group_name)
        p0?.writeString(start_timestamp)
        p0?.writeString(end_timestamp)
        p0?.writeString(linked_project_name)
        p0?.writeInt(lane_location_num)
    }

    companion object{
        val CREATOR : Parcelable.Creator<TimeTableEventProperty> =
            object : Parcelable.Creator<TimeTableEventProperty>{
                override fun createFromParcel(p0: Parcel?): TimeTableEventProperty {
                    val ret = TimeTableEventProperty(
                        project_name = p0?.readString() ?: "",
                        group_name = p0?.readString() ?: "",
                        start_timestamp = p0?.readString() ?: "",
                        end_timestamp = p0?.readString() ?: "",
                        linked_project_name = p0?.readString() ?: "",
                        lane_location_num = p0?.readInt() ?: 1
                    )
                    return ret
                }

                override fun newArray(p0: Int): Array<TimeTableEventProperty> {
                    return Array<TimeTableEventProperty>(p0, { TimeTableEventProperty() })
                }
            }
    }
}
