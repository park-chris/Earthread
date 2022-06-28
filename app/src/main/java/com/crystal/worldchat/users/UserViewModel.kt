package com.crystal.worldchat.users

import android.net.Uri
import androidx.lifecycle.*
import com.crystal.worldchat.datas.Block
import com.crystal.worldchat.datas.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository.get()
    private val auth: FirebaseAuth = Firebase.auth
    private var uid: String? = auth.currentUser?.uid

    fun removeBlock(blockUid: String) {
        userRepository.removeBlock(uid!!, blockUid)
    }

    fun getMyBlock(): MutableLiveData<MutableList<Block>> {
        return userRepository.getMyBlock(uid!!)
    }

    fun addUser(user: User) {
        userRepository.addUser(user)
    }
    fun updateStorageUserImage(uri: Uri) {
    userRepository.updateStorageUserImage(uri, uid!!)
    }

    fun updateUser(path: String, value: String) {
        userRepository.updateUser(path, value, uid!!)
    }

    fun getUserResponse(userId: String): LiveData<User> {
        return userRepository.getUserResponse(userId)
    }

    fun getUsers(): MutableLiveData<List<User>> {
        return userRepository.getUsers()
    }

    fun removeUserImageProfile() {
        userRepository.removeUserImageProfile(uid!!)
    }

    fun updateToken(uid: String, token: String) {
        userRepository.updateToken(uid, token)
    }

    fun removeToken(){
        userRepository.removeToken(uid!!)
    }

}
