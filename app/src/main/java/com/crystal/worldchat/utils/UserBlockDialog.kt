package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.*
import com.crystal.worldchat.R

class UserBlockDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var cancelButton: ImageButton
    private lateinit var userBlockButton: Button
    private lateinit var allBlockButton: Button
    private lateinit var listener: ReportClickedListener
    fun start(uid: String) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_user_block)           // 다이얼로그에 사용할 xml 파일 호출

        cancelButton = dialog.findViewById(R.id.cancel_image_button)
        userBlockButton = dialog.findViewById(R.id.user_block_button)
        allBlockButton = dialog.findViewById(R.id.all_block_button)


        userBlockButton.setOnClickListener {
                listener.onOKClicked(true, uid)
                dialog.dismiss()
        }

        allBlockButton.setOnClickListener {
            listener.onOKClicked(false, uid)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    fun setOnOKClickedListener(listener: (Boolean, String) -> Unit) {
        this.listener = object : ReportClickedListener{
            override fun onOKClicked(result: Boolean, uid: String) {
                listener(result, uid)
            }
        }
    }


    interface ReportClickedListener {
        fun onOKClicked(result: Boolean, uid: String)
    }


}