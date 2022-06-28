package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.crystal.worldchat.R

class RemoveBlockDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var removeButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var listener: CustomDialogClickedListener

    fun start() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_remove_block)           // 다이얼로그에 사용할 xml 파일 호출
        dialog.setCancelable(true)            // 다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지않도록 함

        removeButton = dialog.findViewById(R.id.remove_button)
        cancelButton = dialog.findViewById(R.id.cancel_button)

        removeButton.setOnClickListener {
            listener.onOKClicked()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    fun setOnOKClickedListener(listener: () -> Unit) {
        this.listener = object : CustomDialogClickedListener {
            override fun onOKClicked() {
                listener()
            }
        }
    }


    interface CustomDialogClickedListener {
        fun onOKClicked()
    }


}