package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.crystal.worldchat.R

class SelectImageDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var basicImageText: TextView
    private lateinit var selectImageText: TextView
    private lateinit var listener: ReportClickedListener

    fun start() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_select_image)           // 다이얼로그에 사용할 xml 파일 호출

        basicImageText = dialog.findViewById(R.id.basic_image_text)
        selectImageText = dialog.findViewById(R.id.select_image_text)


// OK Button을 클릭 시 itemEditText를 호출한 프래그먼트로 전달
        basicImageText.setOnClickListener {
                listener.onOKClicked("basic")
                dialog.dismiss()
        }

// Cancel Button을 클릭 시 dialog 닫음
        selectImageText.setOnClickListener {
            listener.onOKClicked("select")
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