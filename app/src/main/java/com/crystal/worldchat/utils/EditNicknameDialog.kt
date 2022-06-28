package com.crystal.worldchat.utils

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.crystal.worldchat.R

class EditNicknameDialog(
    context: Context,
    private val existedNames: ArrayList<String>
) {

    private val dialog = Dialog(context)
    private lateinit var cancelButton: TextView
    private lateinit var okButton: TextView
    private lateinit var validationText: TextView
    private lateinit var nicknameEditText: EditText
    private lateinit var listener: ReportClickedListener

    fun start(context: Context) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)    // 타이틀바 제거
        dialog.setContentView(R.layout.dialog_edit_nickname)           // 다이얼로그에 사용할 xml 파일 호출

        cancelButton = dialog.findViewById(R.id.cancel_text)
        okButton = dialog.findViewById(R.id.ok_text)
        nicknameEditText = dialog.findViewById(R.id.nickname_edit_text)
        validationText = dialog.findViewById(R.id.validation_text)


// OK Button을 클릭 시 itemEditText를 호출한 프래그먼트로 전달
        okButton.setOnClickListener {
            if (nicknameEditText.text.isNotEmpty()) {
                if (validation(nicknameEditText.text.toString())) {
                    listener.onOKClicked(nicknameEditText.text.toString())
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "유효하지 않은 닉네임입니다.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, "변경하실 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
            }

        }

// Cancel Button을 클릭 시 dialog 닫음
        cancelButton.setOnClickListener {
            dialog.dismiss()

        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (nicknameEditText.text.toString() != "") {
                    if (validation(nicknameEditText.text.toString())) {
                        validationText.text = "유효한 닉네임입니다."
                        validationText.setTextColor(
                            ContextCompat.getColor(context,R.color.white)
                        )
                    } else {
                        validationText.text = "유효하지 않은 닉네임입니다."
                        validationText.setTextColor(
                            ContextCompat.getColor(context,R.color.peach)
                        )
                    }
                }
            }
        }

        nicknameEditText.addTextChangedListener(textWatcher)

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

    private fun validation(name: String): Boolean {
        if (existedNames.isNotEmpty()){
            var duplicate = false
            for (existedName in existedNames) {
                if (existedName == name) {
                    duplicate = true
                }
            }
            return !duplicate
        } else {
            return true
        }
    }


}