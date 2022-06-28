package com.crystal.worldchat.fcmServices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crystal.worldchat.datas.NotificationBody
import kotlinx.coroutines.launch

class FirebaseViewModel(application: Application): AndroidViewModel(application) {
    private val repository : FirebaseRepository = FirebaseRepository()
    val myResponse = repository.myResponse

    fun sendNotification(notification: NotificationBody) {
        viewModelScope.launch {
            repository.sendNotification(notification)
        }
    }
}