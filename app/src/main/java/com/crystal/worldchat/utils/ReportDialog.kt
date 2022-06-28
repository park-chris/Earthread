package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.crystal.worldchat.R

class ReportDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var reportButton: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var cancelButton: TextView
    private lateinit var listener: ReportClickedListener
    private var selectedContent: String = ""

    fun start(context: Context) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_report)           // 다이얼로그에 사용할 xml 파일 호출

        reportButton = dialog.findViewById(R.id.report_text)
        cancelButton = dialog.findViewById(R.id.cancel_text)
        radioGroup = dialog.findViewById(R.id.radio_group)


        radioGroup.setOnCheckedChangeListener { group, selectedId ->
            when(selectedId) {
                R.id.child_abuse_radio_button -> selectedContent = context.resources.getString(R.string.child_abuse_report)
                R.id.commercial_radio_button -> selectedContent = context.resources.getString(R.string.commercial_report)
                R.id.misinformation_radio_button -> selectedContent = context.resources.getString(R.string.misinformation_report)
                R.id.pornography_radio_button -> selectedContent = context.resources.getString(R.string.pornography_report)
                R.id.suicide_radio_button -> selectedContent = context.resources.getString(R.string.suicide_report)
                R.id.terror_radio_button -> selectedContent = context.resources.getString(R.string.terror_report)
                R.id.torment_radio_button -> selectedContent = context.resources.getString(R.string.torment_report)
                R.id.violence_radio_button -> selectedContent = context.resources.getString(R.string.violence_report)
            }
        }

// OK Button을 클릭 시 itemEditText를 호출한 프래그먼트로 전달
        reportButton.setOnClickListener {

            if (selectedContent != "") {
                listener.onOKClicked(selectedContent)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "신고 사유를 선택하세요.", Toast.LENGTH_SHORT).show()
            }

        }

// Cancel Button을 클릭 시 dialog 닫음
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object : ReportClickedListener{
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }


    interface ReportClickedListener {
        fun onOKClicked(content: String)
    }


}