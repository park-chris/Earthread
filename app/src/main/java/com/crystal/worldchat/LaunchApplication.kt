package com.crystal.worldchat

import android.app.Application
import com.crystal.worldchat.boards.BoardRepository
import com.crystal.worldchat.chats.ChatRepository
import com.crystal.worldchat.notifications.NotificationRepository
import com.crystal.worldchat.users.UserRepository

class LaunchApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        UserRepository.initialize(this)
        BoardRepository.initialize(this)
        ChatRepository.initialize(this)
        NotificationRepository.initialize(this)
    }
}