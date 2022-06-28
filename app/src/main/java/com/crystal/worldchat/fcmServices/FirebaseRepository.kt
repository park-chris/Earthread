package com.crystal.worldchat.fcmServices

import androidx.lifecycle.MutableLiveData
import com.crystal.worldchat.datas.NotificationBody
import okhttp3.ResponseBody
import retrofit2.Response

class FirebaseRepository() {
    val myResponse : MutableLiveData<Response<ResponseBody>> = MutableLiveData() // 메세지 수신 정보

    // 푸시 메세지 전송
    suspend fun sendNotification(notification: NotificationBody) {
        myResponse.value = RetrofitInstance.api.sendNotification(notification)
    }
}