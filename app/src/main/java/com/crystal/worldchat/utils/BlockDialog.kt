package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.User

class BlockDialog(context: Context) {

    private val dialog = Dialog(context)
    private lateinit var profileImage : ImageView
    private lateinit var nicknameTextView: TextView
    private lateinit var sendMessageTextView: TextView
    private lateinit var blockTextView: TextView
    private lateinit var closeImageButton: ImageButton
    private lateinit var listener: CustomDialogClickedListener

    fun start(context: Context, user: User) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_block)           // 다이얼로그에 사용할 xml 파일 호출
        dialog.setCancelable(true)            // 다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지않도록 함


        profileImage = dialog.findViewById(R.id.profile_img)
        nicknameTextView = dialog.findViewById(R.id.nickname_text)
        sendMessageTextView = dialog.findViewById(R.id.send_message_text)
        blockTextView = dialog.findViewById(R.id.block_text)
        closeImageButton = dialog.findViewById(R.id.close_image_button)


        if (user.profileImageUrl != null) {
            Glide.with(context).load(user.profileImageUrl).circleCrop().into(profileImage)
        }else {
            profileImage.setImageResource(R.drawable.user)
        }
        nicknameTextView.text = user.name

// OK Button을 클릭 시 itemEditText를 호출한 프래그먼트로 전달
        sendMessageTextView.setOnClickListener {
            listener.onOKClicked(true, user.uid!!)
            dialog.dismiss()
        }

// Cancel Button을 클릭 시 dialog 닫음
        blockTextView.setOnClickListener {
            listener.onOKClicked(false, user.uid!!)
            dialog.dismiss()
        }

        closeImageButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    fun setOnOKClickedListener(listener: (Boolean, String) -> Unit) {
        this.listener = object : CustomDialogClickedListener {
            override fun onOKClicked(result: Boolean, uid: String) {
                listener(result, uid)
            }
        }
    }


    interface CustomDialogClickedListener {
        fun onOKClicked(result: Boolean, uid: String)
    }


}