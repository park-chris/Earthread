package com.crystal.worldchat

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.crystal.worldchat.databinding.ActivitySettingNicknameBinding
import com.crystal.worldchat.users.UserViewModel
import com.crystal.worldchat.utils.SelectImageDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

private lateinit var auth: FirebaseAuth

class SettingNicknameActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private val existedName = arrayListOf<String>()

    lateinit var binding: ActivitySettingNicknameBinding

    private val userViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data    // 이미지 경로 원본
                Glide.with(this).load(imageUri).circleCrop().into(binding.profileImg)

            } else {
                Toast.makeText(this, "이미지를 불러오질 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_nickname)
        auth = Firebase.auth

        userViewModel.getUsers().observe(
            this, Observer { users ->
                users?.let {
                    if (users.isNotEmpty()) {
                        for (user in users) {
                            if (user.name != null) {
                                existedName.add(user.name!!)
                            }
                        }
                    }

                }
            }
        )

        setupEvents()

    }

    private fun setupEvents() {

        Glide.with(this).load(R.drawable.user).circleCrop().into(binding.profileImg)



        binding.profileImg.setOnClickListener {

            val dialog = SelectImageDialog(this)
            dialog.setOnOKClickedListener { string ->
                if (string == "basic") {
                    Glide.with(this).load(R.drawable.user).circleCrop().into(binding.profileImg)
                    imageUri = null
                }
                if (string == "select") {

                    val permissionListener = object : PermissionListener {
                        override fun onPermissionGranted() {
                            val intentImage = Intent(Intent.ACTION_PICK)
                            intentImage.type = "image/*"
                            getContent.launch(intentImage)
                        }

                        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                            Toast.makeText(this@SettingNicknameActivity, "저장소 접근 권한을 거부하셨습니다. 이미지를 업로드하기 위해서는 해당 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                        }

                    }

                    TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check()

                }
            }
            dialog.start()

        }

        binding.saveButton.setOnClickListener {
            val name = binding.profileEdt.text.toString()

            if (name.isNotEmpty()) {
                if (imageUri != null) {
                    userViewModel.updateStorageUserImage(imageUri!!)
                }
                if (validation(name)) {
                    userViewModel.updateUser("name", name)
                    moveToMainActivity()
                } else {
                    Toast.makeText(this, "유효하지 않은 닉네임입니다.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "닉네임 입력하여주십시오.", Toast.LENGTH_SHORT).show()
            }

        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.profileEdt.text.toString() != "") {
                    if (validation(binding.profileEdt.text.toString())) {
                        binding.validationText.text = "유효한 닉네임입니다."
                        binding.validationText.setTextColor(
                            ContextCompat.getColor(this@SettingNicknameActivity,R.color.white)
                        )
                    } else {
                        binding.validationText.text = "유효하지 않은 닉네임입니다."
                        binding.validationText.setTextColor(
                            ContextCompat.getColor(this@SettingNicknameActivity,R.color.peach)
                        )
                    }
                }
            }
        }

        binding.profileEdt.addTextChangedListener(textWatcher)
    }

    private fun validation(name: String): Boolean {
        if (existedName.isNotEmpty()){
            var duplicate = false
            for (existedName in existedName) {
                if (existedName == name) {
                    duplicate = true
                }
            }
            return !duplicate
        } else {
            return true
        }
    }

    private fun moveToMainActivity() {
        val intentMain = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intentMain)
    }


}