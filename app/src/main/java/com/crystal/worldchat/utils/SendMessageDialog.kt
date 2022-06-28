package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.*
import com.crystal.worldchat.R

class SendMessageDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var cancelButton: ImageButton
    private lateinit var sendButton: Button
    private lateinit var listener: ReportClickedListener
    fun start(context: Context) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_send_message)           // 다이얼로그에 사용할 xml 파일 호출

        cancelButton = dialog.findViewById(R.id.cancel_image_button)
        sendButton = dialog.findViewById(R.id.send_message_button)


// OK Button을 클릭 시 itemEditText를 호출한 프래그먼트로 전달
        sendButton.setOnClickListener {

                listener.onOKClicked(true)
                dialog.dismiss()
        }

// Cancel Button을 클릭 시 dialog 닫음
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    fun setOnOKClickedListener(listener: (Boolean) -> Unit) {
        this.listener = object : ReportClickedListener{
            override fun onOKClicked(result: Boolean) {
                listener(result)
            }
        }
    }


    interface ReportClickedListener {
        fun onOKClicked(result: Boolean)
    }


}