package com.crystal.worldchat.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crystal.worldchat.datas.Chat
import com.crystal.worldchat.datas.Comment
import com.crystal.worldchat.datas.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatViewModel: ViewModel() {

    private val chatRepository = ChatRepository.get()
    private val chatIDLiveData = MutableLiveData<String>()

    private val auth: FirebaseAuth = Firebase.auth
    private var uid: String? = auth.currentUser?.uid

    var chatLiveData: LiveData<Chat?> = Transformations.switchMap(chatIDLiveData) { chatID ->
        chatRepository.getChatRoom(chatID, uid!!)
    }

    fun removeChatRoom(chat: Chat) {
        chatRepository.removeChatRoom(chat, uid!!)
    }

    fun addChatRoom(chat: Chat) {
        chatRepository.addChatRoom(chat)
    }

    fun getChatInfo(chatId: String): MutableLiveData<Chat> {
       return chatRepository.getChatInfo(chatId)
    }

    fun addChatRoomUser(chatID: String, user: User) {
        chatRepository.addChatRoomUser(chatID, user)
    }

    fun addComment(chatId: String, comment: Comment) {
        chatRepository.addComment(chatId, comment)
    }

    fun getChatRooms(): LiveData<ArrayList<Chat>> {
        return chatRepository.getChatRooms(uid!!)
    }

    fun getOpenChatRooms(): LiveData<ArrayList<Chat>> {
        return chatRepository.getOpenChatRooms()
    }


    fun loadChat(chatId: String) {
        chatIDLiveData.value = chatId
    }

    fun updateCommentReadUser(chatID: String) {

        chatRepository.updateCommentReadUser(chatID, uid!!)
    }

    fun removeRef() {
        chatRepository.removeRef( )
    }

    fun checkRoom(): LiveData<ArrayList<Chat>> {
        return chatRepository.checkRoom()
    }

    fun addBlock(blockUser: User, onlyMessage: Boolean) {
        chatRepository.addBlock(uid!!, blockUser, onlyMessage)
    }
}