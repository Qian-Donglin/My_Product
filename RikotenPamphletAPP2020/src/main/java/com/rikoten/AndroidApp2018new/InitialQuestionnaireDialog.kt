package com.rikoten.AndroidApp2018new

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.rikoten.AndroidApp2018new.SharingMaterial.Algorithm.RandomizedStrGenerator
import com.rikoten.AndroidApp2018new.SharingMaterial.AppInfo
import com.rikoten.AndroidApp2018new.SharingMaterial.InitialOperationPhase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class InitialQuestionnaireDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        //ダイアログの外を押してもダイアログが消えないようにする（デフォルトは消える設定）
        this.setCancelable(false)

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_initial_questionnaire, null)

        builder.setView(view)

        val sp_gender = view.findViewById<Spinner>(R.id.initial_questionnaire_gender_sp)
        val sp_age = view.findViewById<Spinner>(R.id.initial_questionnaire_age_sp)
        val sp_job = view.findViewById<Spinner>(R.id.initial_questionnaire_job_sp)
        val sp_trigger = view.findViewById<Spinner>(R.id.initial_questionnaire_trigger_sp)
        val sp_how_many = view.findViewById<Spinner>(R.id.initial_questionnaire_how_many_sp)

        builder.setPositiveButton(getString(R.string.str_initial_questionnaire_explanation_8),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    //選択してくださいをから文字列に置換してから送信する。
                    val unSelectedChecker: (String) -> String = {
                        if (it == "選択してください")
                            ""
                        else
                            it
                    }

                    //現在時刻を取得

                    //アンケートの送信日時も一応データとして取っておく。（ダウンロード日時は知っておきたい）
                    val calendar = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
                    val date = sdf.format(calendar.time)

                    val answer = InitialQuestionnaireAnswerData(
                        gender = unSelectedChecker(sp_gender.selectedItem.toString()),
                        age = unSelectedChecker(sp_age.selectedItem.toString()),
                        job = unSelectedChecker(sp_job.selectedItem.toString()),
                        reason = unSelectedChecker(sp_trigger.selectedItem.toString()),
                        come = unSelectedChecker(sp_how_many.selectedItem.toString()),
                        os = "Android " + AppInfo.OS_Version,
                        timestamp = date
                    )

                    AppInfo.MLD_InitialOperationPhaseInfo.value = InitialOperationPhase.Usual
                    AppInfo.AnswerInitialQuestionnaire = true
                    //初めての使用でしかここにたどり着かないが、ここでUserにUserIDを付与する。
                    //非情に低い確率で衝突しうる。99.99%以上は大丈夫だろうが
                    AppInfo.UserName = RandomizedStrGenerator(AppInfo.UsernameLength)

                    answer.sendData()

                    AppInfo.writeUserName(context!!)

                    //アプリを再起動する。
                    restartApp(context!!)

                }

            }
        )

        return builder.create()
    }

}