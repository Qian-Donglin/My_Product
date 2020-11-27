package com.rikoten.AndroidApp2018new.Projects

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.rikoten.AndroidApp2018new.Projects.ProjectsList.ProjectsListFragment
import com.rikoten.AndroidApp2018new.R
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.DataFromFireBase
import com.rikoten.AndroidApp2018new.SharingMaterial.ProjectPicker
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.ProjectsProperty

/**
 * A simple [Fragment] subclass.
 */
class ModeChoosingProjectsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mode_choosing_projects, container, false)

        //6つのボタンによる企画の探し方の選択画面
        //ここからボタンのListenerの定義

        val buttonArray : MutableList<ImageButton> = mutableListOf()
        buttonArray.add(view.findViewById(R.id.all_type_choosing_bt))
        buttonArray.add(view.findViewById(R.id.genre_type_choosing_bt))
        buttonArray.add(view.findViewById(R.id.history_type_choosing_bt))
        buttonArray.add(view.findViewById(R.id.favorite_type_choosing_bt))
        buttonArray.add(view.findViewById(R.id.random_type_choosing_bt))
        buttonArray.add(view.findViewById(R.id.popular_type_choosing_bt))

        val listener = object : View.OnClickListener{
            override fun onClick(p0: View?) {
                val bundle = Bundle()
                var nxtFragment : Fragment? = null
                when(p0?.id){
                    R.id.all_type_choosing_bt -> {

                        nxtFragment = ProjectsListFragment()
                        nxtFragment.let{
                            //ここではすべての企画データを追加する
                            bundle.putInt("come by button", R.id.all_type_choosing_bt)
                            val send_list = mutableListOf<ProjectsProperty>()
                            for(v in DataFromFireBase.ProjectsData)
                                send_list.add(v)
                            bundle.putParcelableArrayList("showing projects", ArrayList<Parcelable>(send_list))
                            it.arguments = bundle
                        }

                    }
                    R.id.genre_type_choosing_bt -> {

                        nxtFragment = TagListFragment()
                        nxtFragment.let{
                            //すべてのタグを入れる。
                            bundle.putStringArrayList("tags", ArrayList(DataFromFireBase.ProjectsTags))
                            it.arguments = bundle
                        }

                    }
                    R.id.history_type_choosing_bt -> {
                        //TODO ヒストリー何もなかったら、「まだ何も見てません」とかを書く
                        nxtFragment = ProjectsListFragment()
                        nxtFragment.let{
                            bundle.putInt("come by button", R.id.history_type_choosing_bt)
                            bundle.putParcelableArrayList("showing projects",
                                ArrayList(
                                    ProjectPicker.pickProjectHistory(DataFromFireBase.ProjectsData)
                                )
                            )
                            it.arguments = bundle
                        }

                    }
                    R.id.favorite_type_choosing_bt -> {
                        //お気に入りのすべて
                        nxtFragment = ProjectsListFragment()
                        nxtFragment.let{
                            bundle.putInt("come by button", R.id.favorite_type_choosing_bt)
                            bundle.putParcelableArrayList("showing projects",
                                ArrayList(
                                    ProjectPicker.pickProjectFavorite(DataFromFireBase.ProjectsData)
                                )
                            )
                            it.arguments = bundle
                        }
                    }
                    R.id.random_type_choosing_bt -> {
                        nxtFragment = ProjectsListFragment()
                        nxtFragment.let{
                            bundle.putInt("come by button", R.id.random_type_choosing_bt)
                            bundle.putParcelableArrayList("showing projects",
                                ArrayList(
                                    ProjectPicker.pickProjectRandomized(DataFromFireBase.ProjectsData, AppInfo.RandomProjectsNumber)
                                )
                            )
                            it.arguments = bundle
                        }
                    }
                    R.id.popular_type_choosing_bt -> {
                        nxtFragment = ProjectsListFragment()
                        nxtFragment.let{
                            bundle.putInt("come by button", R.id.popular_type_choosing_bt)
                            bundle.putParcelableArrayList("showing projects",
                                ArrayList(
                                    ProjectPicker.pickProjectRandomized(DataFromFireBase.ProjectsData, AppInfo.PopularProjectsNumber)
                                )
                            )
                            it.arguments = bundle
                        }
                    }
                }
                if(p0?.id == R.id.all_type_choosing_bt) {
                    fragmentManager?.beginTransaction()
                        ?.replace(container?.id!!, nxtFragment!!)
                        ?.commit()
                }else {
                    fragmentManager?.beginTransaction()
                        ?.replace(container?.id!!, nxtFragment!!)
                        ?.commit()
                    }
            }
        }

        for(i in 0 until buttonArray.size){
            buttonArray[i].setOnClickListener(listener)
        }

        return view
    }

}
