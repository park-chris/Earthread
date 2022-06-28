package com.crystal.worldchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.crystal.worldchat.databinding.ActivityLoginBinding
import com.crystal.worldchat.users.UserViewModel
import com.crystal.worldchat.utils.ProgressDialog
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

private const val REQUEST_GOOGLE_CODE = 9009

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding

    private lateinit var googleSignInClient : GoogleSignInClient

    private val userViewModel by lazy {
        ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth

        setupEvents()

    }

    private fun setupEvents() {

        binding.loginButton.setOnClickListener {
            val email = binding.loginIdEdt.text.toString()
            val pw = binding.loginPwEdt.text.toString()

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, getString(R.string.check_id_pw), Toast.LENGTH_SHORT).show()
            } else {
                signIn(email, pw)
            }
        }

        binding.registrationButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        binding.googleLoginButton.setOnClickListener {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            googleLogin()
        }
    }

    private fun googleLogin() {
        val signInClient = googleSignInClient?.signInIntent
        startActivityForResult(signInClient, REQUEST_GOOGLE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GOOGLE_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                    userViewModel.getUserResponse(auth.currentUser!!.uid).observe(this) { user ->
                        if (user.name == null) {
                            moveToSettingNickname()
                        } else {
                            moveToMainActivity()
                        }

                    }
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun moveToMainActivity() {
        val intentMain = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intentMain)
    }

    private fun signIn(email: String, pw: String) {

        val dialog = ProgressDialog(this)

        dialog.on(this)

        auth.signInWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val mUser = auth.currentUser
                    updateUI(mUser)
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        val token = task.result
                        userViewModel.updateToken(mUser!!.uid, token )
                    })

                    userViewModel.getUserResponse(mUser!!.uid).observe(this) { user ->


                        if (user.name == null) {
                            dialog.off()
                            moveToSettingNickname()
                        } else {
                            dialog.off()
                            moveToMainActivity()
                        }
                    }
                } else {
                    dialog.off()
                    Toast.makeText(this, getString(R.string.not_match_id_pw), Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun moveToSettingNickname() {
        val intentMain = Intent(this, SettingNicknameActivity::class.java)
        finish()
        startActivity(intentMain)
    }

    private fun reload() {
    }
}