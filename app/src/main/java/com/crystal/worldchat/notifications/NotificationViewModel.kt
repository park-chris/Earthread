package com.crystal.worldchat.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crystal.worldchat.datas.Notification

class NotificationViewModel: ViewModel() {

    private val notificationRepository = NotificationRepository.get()

    fun getHelp(): MutableLiveData<ArrayList<Notification>> {
        return notificationRepository.getHelp()
    }

    fun getNotification(): MutableLiveData<ArrayList<Notification>> {
        return notificationRepository.getNotification()
    }
}