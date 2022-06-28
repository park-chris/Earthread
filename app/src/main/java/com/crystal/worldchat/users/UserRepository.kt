package com.crystal.worldchat.users

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.crystal.worldchat.datas.Block
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.lang.IllegalStateException

class UserRepository private constructor(context: Context) {

    private val database =
        Firebase.database("https://worldchat-chris-4342-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val rootRef: DatabaseReference = database.reference
    private val userNode: DatabaseReference = rootRef.child("users")
    private val blockNode: DatabaseReference = rootRef.child("block")

    private val storage = FirebaseStorage.getInstance()
    private val storageUserImageRef = storage.reference.child("userImages")

    fun removeBlock(uid: String, blockUid: String) {
        blockNode.child(uid).child("board").child(blockUid).removeValue()
        blockNode.child(uid).child("message").child(blockUid).removeValue()
    }

    fun getMyBlock(uid: String): MutableLiveData<MutableList<Block>> {

        val mutableLiveData = MutableLiveData<MutableList<Block>>()
        val blockUsers = arrayListOf<Block>()

        blockNode.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (value in snapshot.children) {

                    if (value.key == "board") {
                        for (data in value.children) {
                            var existed = false
                            for (block in blockUsers) {
                                if (block.uid == data.key) {
                                    block.board = true
                                    existed = true
                                }
                            }
                            if (!existed) {
                                val mBlock = Block()
                                mBlock.uid = data.key
                                mBlock.name = data.value.toString()
                                mBlock.board = true
                                blockUsers.add(mBlock)
                            }
                        }
                    }
                    if (value.key == "message") {
                        for (data in value.children) {
                            var existed = false
                            for (block in blockUsers) {
                                if (block.uid == data.key) {
                                    block.message = true
                                    existed = true
                                }
                            }
                            if (!existed) {
                                val mBlock = Block()
                                mBlock.uid = data.key
                                mBlock.name = data.value.toString()
                                mBlock.message = true
                                blockUsers.add(mBlock)
                            }
                        }
                    }


                }
                mutableLiveData.value = blockUsers
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        return mutableLiveData

    }


    fun updateStorageUserImage(uri: Uri, uid: String) {
        storageUserImageRef.child("$uid/photo").putFile(uri).addOnSuccessListener {
            storageUserImageRef.child("$uid/photo").downloadUrl.addOnSuccessListener {
                updateUser("profileImageUrl", it.toString(), uid)
            }
        }
    }

    fun removeUserImageProfile(uid: String) {
        userNode.child(uid).child("profileImageUrl").removeValue()
    }

    fun updateToken(uid: String, token: String) {
        userNode.child(uid).child("token").setValue(token)
    }

    fun removeToken(uid: String) {
        userNode.child(uid).child("token").removeValue()
    }

    fun addUser(user: User) {
        userNode.child(user.uid.toString()).setValue(user)
    }

    //    path: name, email...   value: crystal, test@test.com...
    fun updateUser(path: String, value: String, uid: String) {
        userNode.child(uid).child(path).setValue(value)
    }

    fun getUsers(): MutableLiveData<List<User>> {

        val userList = mutableListOf<User>()
        val liveData = MutableLiveData<List<User>>()

        userNode.get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                task.result.let { snapshot ->
                    for (user in snapshot.children) {

                        val mUser = User()
                        mUser.uid = user.key.toString()
                        mUser.name = user.child("name").value as String?
                        mUser.profileImageUrl = user.child("profileImageUrl").value as String?
                        mUser.token = user.child("token").value as String?

                        userList.add(mUser)


                    }
                }
            }

            liveData.value = userList

        }
        return liveData
    }

    fun getUserResponse(userId: String): MutableLiveData<User> {

        val mutableLiveData = MutableLiveData<User>()
        userNode.child(userId).get().addOnCompleteListener { task ->

            val user = User()
            if (task.isSuccessful) {
                val result = task.result
                result?.let { snapshot ->
                    user.email = snapshot.child("email").value as String?
                    user.name = snapshot.child("name").value as String?
                    user.profileImageUrl = snapshot.child("profileImageUrl").value as String?
                    user.uid = snapshot.key.toString()
                    user.token = snapshot.child("token").value as String?


                    if (snapshot.child("my").childrenCount > 0) {


                        val myBoards = mutableListOf<Board>()
                        val myReplies = mutableListOf<Board>()

                        for (data in snapshot.child("my").children) {
                            if (data.key.toString() == "board") {
                                for (childData in data.children) {

                                    val board = Board()
                                    board.id = childData.key.toString()
                                    board.date = childData.child("date").value.toString()
                                    board.title = childData.child("title").value.toString()

                                    myBoards.add(board)
                                }
                                user.myBoard = myBoards
                            }


                            if (data.key.toString() == "reply") {

                                for (childData in data.children) {
                                    val board = Board()
                                    board.id = childData.key.toString()
                                    board.date = childData.child("date").value.toString()
                                    board.title = childData.child("title").value.toString()

                                    myReplies.add(board)
                                }

                                user.myReply = myReplies

                            }

                        }
                    }

                }
            }
            mutableLiveData.value = user
        }

        return mutableLiveData
    }

    companion object {

        private var INSTANCE: UserRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(context)
            }
        }

        fun get(): UserRepository {
            return INSTANCE ?: throw IllegalStateException("UserRepository muse be initialized")
        }

    }

}