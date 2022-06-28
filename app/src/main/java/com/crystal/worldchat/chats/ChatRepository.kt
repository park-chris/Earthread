package com.crystal.worldchat.chats

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.crystal.worldchat.datas.Chat
import com.crystal.worldchat.datas.Comment
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.utils.UIUtil
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.lang.IllegalStateException

class ChatRepository private constructor(context: Context) {

    private val database =
        Firebase.database("https://worldchat-chris-4342-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val rootRef: DatabaseReference = database.reference
    private val chatRoomRef: DatabaseReference = rootRef.child("chatrooms")
    private val userRef: DatabaseReference = rootRef.child("users")
    private val blockNode: DatabaseReference = rootRef.child("block")

    private val storage = FirebaseStorage.getInstance()
    private val storageUserImageRef = storage.reference.child("commentImages")

    private var isListening = false


    fun addBlock(uid: String, blockUser: User, onlyMessage: Boolean) {
        blockNode.child(uid).child("message").child(blockUser.uid!!).setValue(blockUser.name)
        if (!onlyMessage) {
            blockNode.child(uid).child("board").child(blockUser.uid!!).setValue(blockUser.name)
        }
    }

    fun addChatRoom(chat: Chat) {
        chatRoomRef.child(chat.id).setValue(chat)

    }

    fun addChatRoomUser(chatID: String, user: User) {

        chatRoomRef.child(chatID).child("users").child(user.uid!!)
            .setValue(UIUtil.timeStampToString())

    }

    fun removeChatRoom(chat: Chat, uid: String) {
        if (chat.users.keys.size == 1) {
            chatRoomRef.child(chat.id).removeValue()
        } else {
            chatRoomRef.child(chat.id).child("users").child(uid).removeValue()
        }
    }

    fun addComment(chatID: String, comment: Comment) {
        if (comment.imageUrl != null) {
            storageUserImageRef.child("$chatID/${comment.uid}/${comment.time}")
                .putFile(Uri.parse(comment.imageUrl)).addOnSuccessListener {
                    storageUserImageRef.child("$chatID/${comment.uid}/${comment.time}").downloadUrl.addOnSuccessListener {
                        // downloadUri 저장하기
                        comment.imageUrl = it.toString()
                        chatRoomRef.child(chatID).child("comments").push().setValue(comment)

                    }
                }
        } else {
            chatRoomRef.child(chatID).child("comments").push().setValue(comment)
        }

    }

    fun removeRef() {
        isListening = false
    }

    fun checkRoom(): MutableLiveData<ArrayList<Chat>> {
        val mutableLiveData = MutableLiveData<ArrayList<Chat>>()

        val chatList = arrayListOf<Chat>()

        chatRoomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (data in snapshot.children) {
                    val chat = Chat()
                    chat.id = data.key.toString()
                    for (childData in data.child("users").children) {
                        val mUser = User()
                        mUser.uid = childData.key.toString()
                        chat.users.put(childData.key.toString(), mUser)
                    }
                    chatList.add(chat)
                }

                mutableLiveData.value = chatList

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return mutableLiveData
    }

    fun updateCommentReadUser(chatID: String, uid: String) {

        isListening = true

        chatRoomRef.child(chatID).child("comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isListening) {

                        val readUserMap = HashMap<String, Any>()

                        for (data in snapshot.children) {

                            for (i in data.children) {
                                if (i.key == "readUser") {
                                    for (j in i.children) {
                                        if (j.value == false && j.key == uid) {
                                            readUserMap.put(j.key!!, true)

                                        }
                                        if (readUserMap.keys.size > 0) {
                                            chatRoomRef.child(chatID).child("comments")
                                                .child(data.key!!)
                                                .child("readUser").updateChildren(readUserMap)

                                        }
                                    }

                                }
                            }

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    fun getChatInfo(chatID: String): MutableLiveData<Chat> {
        val mutableLiveData = MutableLiveData<Chat>()
        val chat = Chat()

        chatRoomRef.child(chatID).addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.key == "users") {
                    for (user in snapshot.children) {

                        val mUser = User()
                        mUser.uid = user.key.toString()
                        chat.users.put(user.key.toString(), mUser)
                    }
                }

                if (snapshot.key == "id") {
                    chat.id = snapshot.value.toString()
                }

                if (snapshot.key == "title") {
                    chat.title = snapshot.value.toString()
                }

                if (snapshot.key == "information") {
                    chat.information = snapshot.value.toString()
                }


                if (chat.users.isNotEmpty() && chat.id.isNotEmpty() && chat.title != null && chat.information != null) {

                    mutableLiveData.value = chat

                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val changedChat = Chat()

                if (snapshot.key == "users") {
                    for (user in snapshot.children) {

                        val mUser = User()
                        mUser.uid = user.key.toString()
                        chat.users.put(user.key.toString(), mUser)
                    }
                }

                if (snapshot.key == "id") {
                    chat.id = snapshot.value.toString()
                }

                if (snapshot.key == "title") {
                    chat.title = snapshot.value.toString()
                }

                if (snapshot.key == "information") {
                    chat.information = snapshot.value.toString()
                }


                if (chat.users.isNotEmpty() && chat.id.isNotEmpty() && chat.title != null && chat.information != null) {

                    mutableLiveData.value = changedChat

                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return mutableLiveData
    }

    fun getChatRoom(chatID: String, uid: String): MutableLiveData<Chat> {
        val mutableLiveData = MutableLiveData<Chat>()

        val chat = Chat()

        chatRoomRef.child(chatID).addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key == "comments") {

                    for (item in snapshot.children) {

                        val comment = Comment()
                        val mReadUser = HashMap<String, Any>()

                        comment.uid = item.child("uid").value.toString()
                        comment.message = item.child("message").value.toString()
                        comment.time = item.child("time").value.toString()
                        comment.imageUrl = item.child("imageUrl").value as String?


                        for (readUser in item.child("readUser").children) {

                            if (readUser.key != null && readUser.value != null) {
                                mReadUser.put(readUser.key!!, readUser.value!!)
                            }
                        }


                        if (mReadUser.containsKey(uid)) {
                            comment.readUser = mReadUser
                            chat.comments.add(comment)
                        }
                    }
                }

                if (snapshot.key == "users") {

                    for (user in snapshot.children) {

                        val mUser = User()
                        mUser.uid = user.key.toString()
                        chat.users.put(user.key.toString(), mUser)
                    }
                }

                if (snapshot.key == "id") {
                    chat.id = snapshot.value.toString()
                }

                if (snapshot.key == "title") {
                    chat.title = snapshot.value.toString()
                }

                if (snapshot.key == "information") {
                    chat.information = snapshot.value.toString()
                }

                if (chat.users.isNotEmpty() && chat.id.isNotEmpty()) {

                    userRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            for (i in 0 until chat.users.size) {

                                val mUser = User()
                                mUser.uid = chat.users.getValue(chat.users.keys.elementAt(i))?.uid
                                mUser.profileImageUrl =
                                    snapshot.child(mUser.uid!!)
                                        .child("profileImageUrl").value as String?
                                mUser.name =
                                    snapshot.child(mUser.uid!!).child("name").value as String?
                                mUser.token =
                                    snapshot.child(mUser.uid!!).child("token").value as String?

                                chat.users.put(
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid!!,
                                    mUser
                                )

                                if (i == chat.users.size - 1) {
                                    mutableLiveData.value = chat
                                }

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val changedChat = Chat()
                changedChat.id = chat.id
                changedChat.users = chat.users

                if (snapshot.key == "comments") {

                    for (item in snapshot.children) {

                        val comment = Comment()
                        val mReadUser = HashMap<String, Any>()

                        comment.uid = item.child("uid").value.toString()
                        comment.message = item.child("message").value.toString()
                        comment.time = item.child("time").value.toString()
                        comment.imageUrl = item.child("imageUrl").value as String?


                        for (readUser in item.child("readUser").children) {

                            if (readUser.key != null && readUser.value != null) {
                                mReadUser.put(readUser.key!!, readUser.value!!)

                            }
                        }

                        if (mReadUser.containsKey(uid)) {
                            comment.readUser = mReadUser
                            changedChat.comments.add(comment)
                        }
                    }
                }

                if (snapshot.key == "users") {

                    for (user in snapshot.children) {

                        val mUser = User()
                        mUser.uid = user.key.toString()
                        changedChat.users.put(user.key.toString(), mUser)
                    }

                    userRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            for (i in 0 until chat.users.size) {

                                val mUser = User()
                                mUser.uid = chat.users.getValue(chat.users.keys.elementAt(i))?.uid
                                mUser.profileImageUrl =
                                    snapshot.child(mUser.uid!!)
                                        .child("profileImageUrl").value as String?
                                mUser.name =
                                    snapshot.child(mUser.uid!!).child("name").value as String?
                                mUser.token =
                                    snapshot.child(mUser.uid!!).child("token").value as String?

                                changedChat.users.put(
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid!!,
                                    mUser
                                )

                                if (i == chat.users.size - 1) {
                                    mutableLiveData.value = changedChat
                                }

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }


                mutableLiveData.value = changedChat


            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return mutableLiveData

    }

    fun getChatRooms(uid: String): MutableLiveData<ArrayList<Chat>> {

        val liveData = MutableLiveData<ArrayList<Chat>>()
        val chatList = ArrayList<Chat>()
        val blockUserList = arrayListOf<String>()

        blockNode.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (value in snapshot.children) {
                    if (value.key == "message") {
                        for (data in value.children) {
                            blockUserList.add(data.key!!)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        chatRoomRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                var index = 0

                for (data in snapshot.children) {
                    if (data.key == "users") {

                        for (childData in data.children) {
                            if (childData.key == uid) {
                                val chat = Chat()
                                chat.id = snapshot.key!!
                                chatList.add(chat)
                            }
                        }
                    }
                }

                for (data in snapshot.children) {

                    for (chat in chatList) {
                        if (chat.id == snapshot.key) {

                            if (data.key == "users") {
                                for (childData in data.children) {
                                    val mUser = User()
                                    mUser.uid = childData.key.toString()
                                    chat.users.put(childData.key.toString(), mUser)
                                }
                            }


                            if (data.key == "title") {
                                chat.title = data.value as String?
                            }

                            if (data.key == "information") {
                                chat.information = data.value as String?
                            }


                            if (data.key == "comments") {
                                for (childData in data.children) {
                                    val comment = Comment()
                                    val mReadUser = HashMap<String, Any>()

                                    comment.uid = childData.child("uid").value.toString()
                                    comment.message = childData.child("message").value.toString()
                                    comment.time = childData.child("time").value.toString()
                                    comment.imageUrl = childData.child("imageUrl").value as String?


                                    for (readUser in childData.child("readUser").children) {

                                        if (readUser.key != null && readUser.value != null) {
                                            mReadUser.put(readUser.key!!, readUser.value!!)

                                        }
                                    }

                                    if (mReadUser.containsKey(uid)) {
                                        comment.readUser = mReadUser
                                        chat.comments.add(comment)
                                    }

                                }
                            }


                        }
                    }

                    index += 1

                    if (index == snapshot.childrenCount.toInt()) {

                        val removeChat = arrayListOf<Chat>()

                        if (blockUserList.size > 0) {
                            for (chat in chatList) {
                                for (blockId in blockUserList) {
                                    if (chat.users.containsKey(blockId) && chat.users.keys.size == 2) {
                                        removeChat.add(chat)
                                    }
                                }
                            }
                            if (removeChat.size > 0) {
                                for (removedChat in removeChat) {
                                    chatList.remove(removedChat)
                                }
                            }
                        }

                        chatList.sortWith(nullsLast(compareBy { it.comments.lastOrNull()?.time }))
                        chatList.reverse()

                        userRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for (chat in chatList) {

                                    for (i in 0 until chat.users.size) {

                                        val mUser = User()
                                        mUser.uid =
                                            chat.users.getValue(chat.users.keys.elementAt(i))?.uid
                                        mUser.profileImageUrl =
                                            snapshot.child(mUser.uid!!)
                                                .child("profileImageUrl").value as String?
                                        mUser.name = snapshot.child(mUser.uid!!)
                                            .child("name").value as String?

                                        chat.users.put(
                                            chat.users.getValue(chat.users.keys.elementAt(i))?.uid!!,
                                            mUser
                                        )
                                    }

                                }

                                liveData.value = chatList

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                    }


                }


            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val removed = arrayListOf<Chat>()

                var checked = false

                for (chat in chatList) {
                    if (chat.id == snapshot.key) {
                        checked = true
                        for (data in snapshot.children) {

                            if (data.key == "title") {
                                chat.title = data.value as String?
                            }

                            if (data.key == "information") {
                                chat.information = data.value as String?
                            }

                            if (data.key == "users") {
                                val controlMap = HashMap<String, User?>()
                                for (childData in data.children) {
                                    val mUser = User()
                                    mUser.uid = childData.key.toString()
                                    controlMap.put(childData.key.toString(), mUser)
                                }

                                if (controlMap.keys.size < chat.users.keys.size && !controlMap.containsKey(
                                        uid
                                    )
                                ) {
                                    removed.add(chat)
                                } else {
                                    for (childData in data.children) {
                                        val mUser = User()
                                        mUser.uid = childData.key.toString()
                                        chat.users.put(childData.key.toString(), mUser)
                                    }
                                }

                            }

                            if (data.key == "comments") {

                                for (childData in data.children) {

                                    val comment = Comment()
                                    val mReadUser = HashMap<String, Any>()

                                    comment.uid = childData.child("uid").value.toString()
                                    comment.message = childData.child("message").value.toString()
                                    comment.time = childData.child("time").value.toString()
                                    comment.imageUrl = childData.child("imageUrl").value as String?


                                    for (readUser in childData.child("readUser").children) {

                                        if (readUser.key != null && readUser.value != null) {
                                            mReadUser.put(readUser.key!!, readUser.value!!)

                                        }
                                    }
                                    if (mReadUser.containsKey(uid)) {
                                        comment.readUser = mReadUser
                                        chat.comments.add(comment)
                                    }


                                }
                            }

                        }

                    }

                    if (!chat.users.containsKey(uid)) {
                        removed.add(chat)
                    }

                }

                if (!checked) {
                    val chat = Chat()

                    if (snapshot.key != null) {

                        chat.id = snapshot.key!!

                        for (data in snapshot.children) {

                            if (data.key == "title") {
                                chat.title = data.value as String?
                            }

                            if (data.key == "information") {
                                chat.information = data.value as String?
                            }

                            if (data.key == "users") {
                                for (childData in data.children) {
                                    for (child in data.children) {
                                        val mUser = User()
                                        mUser.uid = child.key.toString()
                                        chat.users.put(child.key.toString(), mUser)
                                    }
                                }
                            }

                            if (data.key == "comments") {

                                for (childData in data.children) {

                                    val comment = Comment()
                                    val mReadUser = HashMap<String, Any>()

                                    comment.uid = childData.child("uid").value.toString()
                                    comment.message = childData.child("message").value.toString()
                                    comment.time = childData.child("time").value.toString()
                                    comment.imageUrl = childData.child("imageUrl").value as String?


                                    for (readUser in childData.child("readUser").children) {

                                        if (readUser.key != null && readUser.value != null) {
                                            mReadUser.put(readUser.key!!, readUser.value!!)

                                        }
                                    }
                                    if (mReadUser.containsKey(uid)) {
                                        comment.readUser = mReadUser
                                        chat.comments.add(comment)
                                    }


                                }
                            }
                        }
                    }
                    chatList.add(chat)
                }

                if (blockUserList.size > 0) {
                    for (chat in chatList) {
                        for (blockId in blockUserList) {
                            if (chat.users.containsKey(blockId) && chat.users.keys.size == 2) {
                                removed.add(chat)
                            }
                        }
                    }
                }

                removed.distinct()

                if (removed.isNotEmpty()) {
                    for (chat in removed) {
                        chatList.remove(chat)
                    }
                }

                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (chat in chatList) {

                            for (i in 0 until chat.users.size) {

                                val mUser = User()
                                mUser.uid =
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid
                                mUser.profileImageUrl =
                                    snapshot.child(mUser.uid!!)
                                        .child("profileImageUrl").value as String?
                                mUser.name = snapshot.child(mUser.uid!!)
                                    .child("name").value as String?

                                chat.users.put(
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid!!,
                                    mUser
                                )
                            }

                        }

                        liveData.value = chatList

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

                chatList.sortWith(nullsLast(compareBy { it.comments.lastOrNull()?.time }))
                chatList.reverse()

                liveData.value = chatList
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                var removedChat: Chat? = null
                for (chat in chatList) {
                    if (chat.id == snapshot.key) {
                        removedChat = chat
                    }
                }
                chatList.remove(removedChat)
                liveData.value = chatList
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return liveData
    }

    fun getOpenChatRooms(): MutableLiveData<ArrayList<Chat>> {

        val liveData = MutableLiveData<ArrayList<Chat>>()
        val chatList = ArrayList<Chat>()

        chatRoomRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                for (data in snapshot.children) {
                    if (data.key == "opened") {
                        if (data.value == true) {
                            val chat = Chat()
                            chat.id = snapshot.key!!
                            chatList.add(chat)
                        }
                    }
                }

                for (data in snapshot.children) {
                    for (chat in chatList) {
                        if (chat.id == snapshot.key) {

                            if (data.key == "title") {
                                chat.title = data.value as String?
                            }
                            if (data.key == "information") {
                                chat.information = data.value as String?
                            }

                            if (data.key == "users") {
                                for (childData in data.children) {
                                    val mUser = User()
                                    mUser.uid = childData.key.toString()
                                    chat.users.put(childData.key.toString(), mUser)
                                }
                            }

                        }
                    }

                }

                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (chat in chatList) {

                            for (i in 0 until chat.users.size) {

                                val mUser = User()
                                mUser.uid =
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid
                                mUser.profileImageUrl =
                                    snapshot.child(mUser.uid!!)
                                        .child("profileImageUrl").value as String?
                                mUser.name = snapshot.child(mUser.uid!!)
                                    .child("name").value as String?

                                chat.users.put(
                                    chat.users.getValue(chat.users.keys.elementAt(i))?.uid!!,
                                    mUser
                                )
                            }

                        }

                        liveData.value = chatList

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                var index = 0

                for (chat in chatList) {
                    if (chat.id == snapshot.key) {

                        for (data in snapshot.children) {

                            if (data.key == "title") {
                                chat.title = data.value as String?
                            }

                            if (data.key == "comments") {

                                for (childData in data.children) {

                                    val comment = Comment()
                                    val mReadUser = HashMap<String, Any>()

                                    comment.uid = childData.child("uid").value.toString()
                                    comment.message = childData.child("message").value.toString()
                                    comment.time = childData.child("time").value.toString()
                                    comment.imageUrl = childData.child("imageUrl").value as String?


                                    for (readUser in childData.child("readUser").children) {

                                        if (readUser.key != null && readUser.value != null) {
                                            mReadUser.put(readUser.key!!, readUser.value!!)

                                        }
                                    }

                                    comment.readUser = mReadUser

                                    chat.comments.add(comment)


                                }
                            }

                        }

                    }

                    index += 1



                    if (index == chatList.size) {
                        liveData.value = chatList
                    }
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return liveData
    }


    companion object {

        private var INSTANCE: ChatRepository? = null

        fun initialize(context: Context) {

            if (INSTANCE == null) {
                INSTANCE = ChatRepository(context)
            }

        }

        fun get(): ChatRepository {
            return INSTANCE ?: throw IllegalStateException("ChatRepository muse be initialized")
        }
    }

}