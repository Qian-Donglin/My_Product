package com.rikoten.AndroidApp2018new.SharingMaterial.DataType

import android.os.Parcel
import android.os.Parcelable

//定義はGitHubにある構造説明/Firestoreの構造.xmlのProjectsのコレクション内のDocumentに準拠
//基本的には9個の引数を渡すことになるが、Map<String, String>で渡してもよい。
//iconとlarge_imageでは、中身では

data class ProjectsProperty(
    var projectId : String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var description: String? = null,
    var description_detailed: String? = null,
    var group: String? = null,
    var group_description: String? = null,
    var icon: String? = null,
    var large_image: String? = null,
    var group_website: String? = null,
    //twitterは@を除いたアカウントID
    var twitter: String? = null,
    //facebookやinstagramはいずれもURL
    var facebook: String? = null,
    var instagram: String? = null,
    //理工展のWebでのこの企画のサイト
    var rikoten_web_link: String? = null,
    //バーチャル理工展での教室番号　定義方法はバーチャル理工展側に聞いてほしい
    var virtual_rikoten_location: Int? = null,
    //タグ。 TODO 1つ目のタグは重要タグでそれで分類する予定。タグの複合検索も実装したい
    var tags: List<String>? = null,
    var is_live_event : Boolean = false
) : Parcelable
{

    //セカンダリコンストラクタ　Mapの形からでも代入できるように
    constructor(mapDatabase: Map<String, Any?>) : this(
        mapDatabase["project id"] as String,
        mapDatabase["title"] as String,
        mapDatabase["subtitle"] as String,
        mapDatabase["description"] as String,
        mapDatabase["description_detailed"] as String,
        mapDatabase["group"] as String,
        mapDatabase["group_description"] as String,
        mapDatabase["icon"] as String,
        mapDatabase["large_image"] as String,
        mapDatabase["group_website"] as String,
        mapDatabase["twitter"] as String,
        mapDatabase["facebook"] as String,
        mapDatabase["instagram"] as String,
        mapDatabase["rikoten_web_link"] as String,
        mapDatabase["virtual_rikoten_location"] as Int,
        mapDatabase["tags"] as MutableList<String>,
        mapDatabase["is_live_event"] as Boolean
    ){
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(projectId)
        p0?.writeString(title)
        p0?.writeString(subtitle)
        p0?.writeString(description)
        p0?.writeString(description_detailed)
        p0?.writeString(group)
        p0?.writeString(group_description)
        p0?.writeString(icon)
        p0?.writeString(large_image)
        p0?.writeString(group_website)
        p0?.writeString(twitter)
        p0?.writeString(facebook)
        p0?.writeString(instagram)
        p0?.writeString(rikoten_web_link)
        p0?.writeInt(virtual_rikoten_location ?: 0)
        p0?.writeList(tags ?: mutableListOf<String>("nodata"))
        //booleanでなんかバグったので緊急措置
        p0?.writeInt(if(is_live_event) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR : Parcelable.Creator<ProjectsProperty> =
            object : Parcelable.Creator<ProjectsProperty>{
                override fun createFromParcel(p0: Parcel?): ProjectsProperty {
                    val tmpList : MutableList<String> = mutableListOf()
                    return ProjectsProperty(
                        p0?.readString(),//project id
                        p0?.readString(),//title
                        p0?.readString(),//subtitle
                        p0?.readString(),//description
                        p0?.readString(),//description_detail
                        p0?.readString(),//group
                        p0?.readString(),//group_description
                        p0?.readString(),//icon
                        p0?.readString(),//large_image
                        p0?.readString(),//group_website
                        p0?.readString(),//twitter
                        p0?.readString(),//facebook
                        p0?.readString(),//instagram
                        p0?.readString(),//rikoten_web_link
                        p0?.readInt(),//virtual_rikoten_location
                        { tmpList : MutableList<String> ->
                            p0?.readList(tmpList, List::class.java.classLoader);tmpList}(tmpList), //tags
                        if(p0?.readInt()!! == 1) true else false
                    )
                }

                override fun newArray(p0: Int): Array<ProjectsProperty> {
                    return Array<ProjectsProperty>(p0, { ProjectsProperty() })
                }
            }
    }

}