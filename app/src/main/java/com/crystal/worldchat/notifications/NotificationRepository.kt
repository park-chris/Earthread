package com.crystal.worldchat.notifications

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.crystal.worldchat.datas.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.IllegalStateException

class NotificationRepository private constructor(context: Context) {

    private val database = Firebase.database("https://worldchat-chris-4342-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val rootRef: DatabaseReference = database.reference
    private val helpRef: DatabaseReference = rootRef.child("help")
    private val notificationRef: DatabaseReference = rootRef.child("notification")

    fun getHelp(): MutableLiveData<ArrayList<Notification>> {

        val liveData = MutableLiveData<ArrayList<Notification>>()
        val notiList = arrayListOf<Notification>()

        helpRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (data in snapshot.children) {
                    val noti = Notification()
                    noti.question = data.key.toString()
                    noti.answer = data.value.toString().replace("다.", "다.\n\n")
                    notiList.add(noti)
                }

                liveData.value = notiList
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        return liveData
    }

    fun getNotification(): MutableLiveData<ArrayList<Notification>> {

        val liveData = MutableLiveData<ArrayList<Notification>>()
        val notiList = arrayListOf<Notification>()

        notificationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (data in snapshot.children) {
                    val noti = Notification()
                    noti.question = data.key.toString()
                    noti.answer = data.value.toString().replace(".", ".\n\n")
                    notiList.add(noti)
                }

                liveData.value = notiList
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        return liveData
    }


    companion object {

        private var INSTANCE: NotificationRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = NotificationRepository(context)
            }
        }

        fun get(): NotificationRepository {
            return INSTANCE ?: throw IllegalStateException("NotificationRepository muse be initialized")
        }

    }
}