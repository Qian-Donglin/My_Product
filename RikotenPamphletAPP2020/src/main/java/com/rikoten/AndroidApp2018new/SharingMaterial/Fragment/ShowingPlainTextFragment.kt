package com.rikoten.AndroidApp2018new.SharingMaterial.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rikoten.AndroidApp2018new.R

/**
 * A simple [Fragment] subclass.
 */
class ShowingPlainTextFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_showing_plain_text, container, false)

        val target_tv = view.findViewById<TextView>(R.id.showing_plain_text_f_tv)
        if(arguments == null){
            //運用上ここの分岐はあり得ないはず。かならず文章をBundleで渡すため。
            target_tv.text="argumentsにデータが入ってないバグが起きています。"
        }
        else {
            target_tv.text = arguments?.getString("text")
        }

        return view
    }

}
