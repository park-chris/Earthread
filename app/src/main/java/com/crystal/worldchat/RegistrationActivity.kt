package com.crystal.worldchat

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.crystal.worldchat.databinding.ActivityRegistrationBinding
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.users.UserViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)

        auth = Firebase.auth

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        setupEvents()

    }


    private fun setupEvents() {

        binding.signupButton.setOnClickListener {

            val email = binding.signupIdEdt.text.toString()
            val password = binding.signupPwEdt.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            task.isSuccessful
                            val user = Firebase.auth.currentUser
                            val userId = user?.uid

                            val newUser = User(email, null, null, userId)

                            userViewModel.addUser(newUser)
                            updateUI(user)

                            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    return@OnCompleteListener
                                }
                                val token = task.result
                                userViewModel.updateToken(userId!!, token )
                            })
                            moveToSettingNickname()
                        } else {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, getString(R.string.check_id_pw), Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun moveToSettingNickname() {
        val intentMain = Intent(this, SettingNicknameActivity::class.java)
        finish()
        startActivity(intentMain)
    }

    private fun updateUI(user: FirebaseUser?) {
    }
}