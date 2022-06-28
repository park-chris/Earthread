package com.crystal.worldchat.boards

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.crystal.worldchat.datas.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.lang.IllegalStateException
import kotlin.collections.HashMap

class BoardRepository private constructor(context: Context) {

    private val database =
        Firebase.database("https://worldchat-chris-4342-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val rootRef: DatabaseReference = database.reference
    private val boardNode: DatabaseReference = rootRef.child("boards")
    private val reportedBoardNode: DatabaseReference = rootRef.child("reportedBoard")
    private val favoriteBoardNode: DatabaseReference = rootRef.child("favoriteBoard")
    private val likeBoardNode: DatabaseReference = rootRef.child("likeBoard")
    private val userNode: DatabaseReference = rootRef.child("users")
    private val blockNode: DatabaseReference = rootRef.child("block")

    private val storage = FirebaseStorage.getInstance()
    private val storageBoardImageRef = storage.reference.child("boardImages")

    fun removeStorageBoardImage(board: Board) {
        storageBoardImageRef.child("${board.id}").delete().addOnSuccessListener {
        }
    }

    fun addReportedBoard(report: Report) {

        if (report.replyID != null) {
            reportedBoardNode.child("${report.boardID}").child("reply").child("${report.replyID}")
                .child("${report.userId}").setValue(report)
        } else {
            reportedBoardNode.child("${report.boardID}").child("${report.userId}").setValue(report)
        }
    }

    fun updateStorageBoardImage(board: Board, callback: FirebaseCallback) {

        for (i in 0 until board.imageUrlList.size) {

            if (board.imageUrlList[i].uri.contains("https://")) {
                if (i == board.imageUrlList.size - 1) {
                    callback.onSuccess(true)
                }
            } else {
                val uri = Uri.parse(board.imageUrlList[i].uri)
                storageBoardImageRef.child("${board.id}/${board.imageUrlList[i].id}")
                    .putFile(uri)
                    .addOnSuccessListener {
                        storageBoardImageRef.child("${board.id}/${board.imageUrlList[i].id}").downloadUrl.addOnSuccessListener {
                            board.imageUrlList[i].uri = it.toString()
                            if (i == board.imageUrlList.size - 1) {
                                callback.onSuccess(true)
                            }
                        }
                    }
            }


        }

    }

    fun addBoard(board: Board) {
        boardNode.child(board.id).setValue(board)
        userNode.child(board.userId).child("my").child("board").child(board.id).setValue(board)
    }

    fun addReply(board: Board, reply: Reply) {
        boardNode.child(board.id).child("replyList").child("${reply.userId}-${reply.date}")
            .setValue(reply)
        board.date = reply.date
        userNode.child(reply.userId).child("my").child("reply").child(board.id).setValue(board)

    }

    fun removeReply(board: Board, reply: Reply) {
        boardNode.child(board.id).child("replyList").child("${reply.userId}-${reply.date}")
            .removeValue()
    }

    fun removeBoard(board: Board) {
        boardNode.child(board.id).removeValue()
    }

    fun updateBoard(board: Board, path: String) {

        boardNode.child(board.id).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val result = task.result
                result?.let {
                    if (result.value != null) {
                        var value: Any = ""

                        when (path) {
                            "watcher" -> value = board.watcher
                            "likeCount" -> value = board.likeCount
                            "dislikeCount" -> value = board.dislikeCount
                            "replyCount" -> value = board.replyCount
                            "isLikeDislikeClicked" -> {
                                val result = board.isLikeDislikeClicked.toString()
                                value = result
                            }
                            "imageUrlList" -> value = board.imageUrlList
                        }

                        val boardMap = HashMap<String, Any>()
                        boardMap.put("${board.id}/$path", value)

                        boardNode.updateChildren(boardMap)
                    }
                }
            } else {
                Log.e("BoardRepository", "addOnCompleteListener process Failed")
            }

        }

    }

    fun getMyFavoriteBoard(userID: String): MutableLiveData<MutableList<Board>> {

        val mutableLiveData = MutableLiveData<MutableList<Board>>()
        val isLikeDislikeBoards = HashMap<String, Any>()
        val favoriteBoards = mutableListOf<String>()

        likeBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (boardId in snapshot.children) {
                    isLikeDislikeBoards.put(
                        boardId.key.toString(),
                        boardId.value.toString().toBoolean()
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        favoriteBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (boardId in snapshot.children) {
                    favoriteBoards.add(boardId.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        boardNode.addListenerForSingleValueEvent(object : ValueEventListener {
            val boards: MutableList<Board> = mutableListOf()

            override fun onDataChange(snapshot: DataSnapshot) {

                for (board in snapshot.children) {

                    val mBoard = Board()
                    val imageList = mutableListOf<BoardImage>()

                    for (favorite in favoriteBoards) {
                        if (favorite == board.key.toString()) {

                            mBoard.favorite = true
                            mBoard.id = board.key.toString()
                            mBoard.category = board.child("category").value as String?
                            mBoard.title = board.child("title").value.toString()
                            mBoard.likeCount =
                                board.child("likeCount").value.toString().toInt()
                            mBoard.content = board.child("content").value.toString()
                            mBoard.name = board.child("name").value.toString()
                            mBoard.date = board.child("date").value.toString()
                            mBoard.userId = board.child("userId").value.toString()
                            mBoard.dislikeCount =
                                board.child("dislikeCount").value.toString().toInt()
                            mBoard.replyCount =
                                board.child("replyList").childrenCount.toInt()
                            mBoard.watcher =
                                board.child("watcher").value.toString().toInt()


                            if (board.child("imageUrlList").childrenCount > 0) {
                                val boardImage = BoardImage()
                                boardImage.id =
                                    board.child("imageUrlList").children.first().key!!.toInt()
                                boardImage.uri =
                                    board.child("imageUrlList").children.first()
                                        .child("uri").value as String
                                boardImage.content =
                                    board.child("imageUrlList").children.first()
                                        .child("content").value as String

                                imageList.add(boardImage)

                                mBoard.imageUrlList = imageList
                            }


                            for (like in isLikeDislikeBoards) {
                                if (like.key == board.key) {
                                    mBoard.isLikeDislikeClicked =
                                        like.value.toString().toBoolean()
                                }
                            }


                            boards.add(mBoard)
                        }


                    }

                }


                mutableLiveData.value = boards
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



        return mutableLiveData
    }

    fun getBoardResponse(boardID: String, userID: String): MutableLiveData<Board> {

        val mutableLiveData = MutableLiveData<Board>()
        val favoriteBoards = mutableListOf<String>()
        val isLikeDislikeBoards = HashMap<String, Any>()

        likeBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (boardId in snapshot.children) {
                    isLikeDislikeBoards.put(
                        boardId.key.toString(),
                        boardId.value.toString().toBoolean()
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        favoriteBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (boardId in snapshot.children) {
                    favoriteBoards.add(boardId.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        boardNode.child(boardID).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val board = Board()
                val imageList = mutableListOf<BoardImage>()
                val replyList = mutableListOf<Reply>()

                val result = task.result
                result?.let { snapshot ->

                    if (snapshot.child("replyList").childrenCount > 0) {
                        snapshot.child("replyList").children.forEach { reply ->
                            val mReply = Reply()
                            mReply.name = reply.child("name").value.toString()
                            mReply.content = reply.child("content").value.toString()
                            mReply.date = reply.child("date").value.toString()
                            mReply.userId = reply.child("userId").value.toString()

                            replyList.add(mReply)
                        }
                    }

                    if (snapshot.child("imageUrlList").childrenCount > 0) {
                        snapshot.child("imageUrlList").children.forEach { childSnapshot ->
                            val boardImage = BoardImage()
                            boardImage.id = childSnapshot.key!!.toInt()
                            boardImage.uri = childSnapshot.child("uri").value as String
                            boardImage.content = childSnapshot.child("content").value as String

                            imageList.add(boardImage)
                        }
                    }
                    if (snapshot.value != null) {
                        board.id = snapshot.key.toString()
                        board.category = snapshot.child("category").value as String?
                        board.content = snapshot.child("content").value as String?
                        board.date = snapshot.child("date").value as String
                        board.name = snapshot.child("name").value as String
                        board.title = snapshot.child("title").value as String
                        board.userId = snapshot.child("userId").value as String
                        board.dislikeCount = snapshot.child("dislikeCount").value.toString().toInt()
                        board.likeCount = snapshot.child("likeCount").value.toString().toInt()
                        board.watcher = snapshot.child("watcher").value.toString().toInt()
                        board.replyCount = snapshot.child("replyList").childrenCount.toInt()
                        board.imageUrlList = imageList
                        board.replyList = replyList
                        board.date = snapshot.child("date").value.toString()

                        for (like in isLikeDislikeBoards) {
                            if (like.key == board.id) {
                                board.isLikeDislikeClicked = like.value.toString().toBoolean()
                            }
                        }
                        for (favorite in favoriteBoards) {
                            if (favorite == board.id) {
                                board.favorite = true
                            }
                        }
                    }

                }
                mutableLiveData.value = board
            } else {
                mutableLiveData.value = null
                Log.e("BoardRepository", "addOnCompleteListener process Failed")
            }
        }
        return mutableLiveData
    }

    fun getMyBoardsResponse(userID: String): MutableLiveData<MutableList<Board>> {

        val mutableLiveData = MutableLiveData<MutableList<Board>>()
        val boards = arrayListOf<Board>()

        boardNode.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                for (board in snapshot.children) {

                    if (board.child("userId").value.toString() == userID) {
                        val mBoard = Board()
                        val imageList = mutableListOf<BoardImage>()

                        mBoard.id = board.key.toString()
                        mBoard.category = board.child("category").value as String?
                        mBoard.title = board.child("title").value.toString()
                        mBoard.likeCount =
                            board.child("likeCount").value.toString().toInt()
                        mBoard.content = board.child("content").value.toString()
                        mBoard.name = board.child("name").value.toString()
                        mBoard.date = board.child("date").value.toString()
                        mBoard.userId = board.child("userId").value.toString()
                        mBoard.dislikeCount =
                            board.child("dislikeCount").value.toString().toInt()
                        mBoard.replyCount =
                            board.child("replyList").childrenCount.toInt()
                        mBoard.watcher =
                            board.child("watcher").value.toString().toInt()

                        if (board.child("imageUrlList").childrenCount > 0) {
                            val boardImage = BoardImage()
                            boardImage.id =
                                board.child("imageUrlList").children.first().key!!.toInt()
                            boardImage.uri =
                                board.child("imageUrlList").children.first()
                                    .child("uri").value as String
                            boardImage.content =
                                board.child("imageUrlList").children.first()
                                    .child("content").value as String

                            imageList.add(boardImage)

                            mBoard.imageUrlList = imageList
                        }

                        boards.add(mBoard)

                    }

                }
                mutableLiveData.value = boards
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return mutableLiveData
    }


    fun getBoardsResponse(
        userID: String,
        keyword: String?,
        filterList: ArrayList<String>?,
        sort: Int?
    ): MutableLiveData<MutableList<Board>> {

        val mutableLiveData = MutableLiveData<MutableList<Board>>()
        val favoriteBoards = mutableListOf<String>()
        val isLikeDislikeBoards = HashMap<String, Any>()
        val blockUsers = arrayListOf<String>()

        blockNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (value in snapshot.children) {
                    if (value.key == "board") {
                        for (data in value.children) {
                            blockUsers.add(data.key!!)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        likeBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (boardId in snapshot.children) {
                    isLikeDislikeBoards.put(
                        boardId.key.toString(),
                        boardId.value.toString().toBoolean()
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        favoriteBoardNode.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (boardId in snapshot.children) {
                    favoriteBoards.add(boardId.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        var sortString = "date"

        when (sort) {
            1 -> sortString = "watcher"
            2 -> sortString = "likeCount"
        }

        boardNode.orderByChild(sortString)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                val boards: MutableList<Board> = mutableListOf()

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (board in snapshot.children) {
                        if (board.value != null && board.key != null) {
                            val mBoard = Board()
                            val imageList = mutableListOf<BoardImage>()

                            if (keyword != null) {

                                if (board.child("content").value.toString().contains(keyword)) {


                                    if (!filterList.isNullOrEmpty()) {
                                        var filter = ""
                                        for (i in filterList) {
                                            filter += "$i "
                                        }


                                        if (filter.contains(board.child("category").value.toString())) {
                                            var isBlocked = false
                                            for (block in blockUsers) {
                                                if (block == board.child("userId").value.toString()) {
                                                    isBlocked = true
                                                }
                                            }

                                            if (!isBlocked) {
                                                mBoard.id = board.key.toString()
                                                mBoard.category =
                                                    board.child("category").value as String?
                                                mBoard.title = board.child("title").value.toString()
                                                mBoard.likeCount =
                                                    board.child("likeCount").value.toString().toInt()
                                                mBoard.content = board.child("content").value.toString()
                                                mBoard.name = board.child("name").value.toString()
                                                mBoard.date = board.child("date").value.toString()
                                                mBoard.userId = board.child("userId").value.toString()
                                                mBoard.dislikeCount =
                                                    board.child("dislikeCount").value.toString().toInt()
                                                mBoard.replyCount =
                                                    board.child("replyList").childrenCount.toInt()
                                                mBoard.watcher =
                                                    board.child("watcher").value.toString().toInt()


                                                if (board.child("imageUrlList").childrenCount > 0) {
                                                    val boardImage = BoardImage()
                                                    boardImage.id =
                                                        board.child("imageUrlList").children.first().key!!.toInt()
                                                    boardImage.uri =
                                                        board.child("imageUrlList").children.first()
                                                            .child("uri").value as String
                                                    boardImage.content =
                                                        board.child("imageUrlList").children.first()
                                                            .child("content").value as String

                                                    imageList.add(boardImage)

                                                    mBoard.imageUrlList = imageList
                                                }


                                                for (like in isLikeDislikeBoards) {
                                                    if (like.key == board.key) {
                                                        mBoard.isLikeDislikeClicked =
                                                            like.value.toString().toBoolean()
                                                    }
                                                }

                                                for (favorite in favoriteBoards) {
                                                    if (favorite == board.key.toString()) {
                                                        mBoard.favorite = true
                                                    }
                                                }

                                                boards.add(mBoard)

                                            }




                                        }

                                    }
                                    else {

                                        var isBlocked = false
                                        for (block in blockUsers) {
                                            if (block == board.child("userId").value.toString()) {
                                                isBlocked = true
                                            }
                                        }

                                        if (!isBlocked) {
                                            mBoard.id = board.key.toString()
                                            mBoard.category =
                                                board.child("category").value as String?
                                            mBoard.title = board.child("title").value.toString()
                                            mBoard.likeCount =
                                                board.child("likeCount").value.toString().toInt()
                                            mBoard.content = board.child("content").value.toString()
                                            mBoard.name = board.child("name").value.toString()
                                            mBoard.date = board.child("date").value.toString()
                                            mBoard.userId = board.child("userId").value.toString()
                                            mBoard.dislikeCount =
                                                board.child("dislikeCount").value.toString().toInt()
                                            mBoard.replyCount =
                                                board.child("replyList").childrenCount.toInt()
                                            mBoard.watcher =
                                                board.child("watcher").value.toString().toInt()


                                            if (board.child("imageUrlList").childrenCount > 0) {
                                                val boardImage = BoardImage()
                                                boardImage.id =
                                                    board.child("imageUrlList").children.first().key!!.toInt()
                                                boardImage.uri =
                                                    board.child("imageUrlList").children.first()
                                                        .child("uri").value as String
                                                boardImage.content =
                                                    board.child("imageUrlList").children.first()
                                                        .child("content").value as String

                                                imageList.add(boardImage)

                                                mBoard.imageUrlList = imageList
                                            }


                                            for (like in isLikeDislikeBoards) {
                                                if (like.key == board.key) {
                                                    mBoard.isLikeDislikeClicked =
                                                        like.value.toString().toBoolean()
                                                }
                                            }

                                            for (favorite in favoriteBoards) {
                                                if (favorite == board.key.toString()) {
                                                    mBoard.favorite = true
                                                }
                                            }

                                            boards.add(mBoard)

                                        }
                                    }

                                }

                            }
                            else {
                                if (!filterList.isNullOrEmpty()) {
                                    var filter = ""
                                    for (i in filterList) {
                                        filter += "$i "
                                    }

                                    if (filter.contains(board.child("category").value.toString())) {


                                        var isBlocked = false
                                        for (block in blockUsers) {
                                            if (block == board.child("userId").value.toString()) {
                                                isBlocked = true
                                            }
                                        }

                                        if (!isBlocked) {
                                            mBoard.id = board.key.toString()
                                            mBoard.category =
                                                board.child("category").value as String?
                                            mBoard.title = board.child("title").value.toString()
                                            mBoard.likeCount =
                                                board.child("likeCount").value.toString().toInt()
                                            mBoard.content = board.child("content").value.toString()
                                            mBoard.name = board.child("name").value.toString()
                                            mBoard.date = board.child("date").value.toString()
                                            mBoard.userId = board.child("userId").value.toString()
                                            mBoard.dislikeCount =
                                                board.child("dislikeCount").value.toString().toInt()
                                            mBoard.replyCount =
                                                board.child("replyList").childrenCount.toInt()
                                            mBoard.watcher =
                                                board.child("watcher").value.toString().toInt()


                                            if (board.child("imageUrlList").childrenCount > 0) {
                                                val boardImage = BoardImage()
                                                boardImage.id =
                                                    board.child("imageUrlList").children.first().key!!.toInt()
                                                boardImage.uri =
                                                    board.child("imageUrlList").children.first()
                                                        .child("uri").value as String
                                                boardImage.content =
                                                    board.child("imageUrlList").children.first()
                                                        .child("content").value as String

                                                imageList.add(boardImage)

                                                mBoard.imageUrlList = imageList
                                            }


                                            for (like in isLikeDislikeBoards) {
                                                if (like.key == board.key) {
                                                    mBoard.isLikeDislikeClicked =
                                                        like.value.toString().toBoolean()
                                                }
                                            }

                                            for (favorite in favoriteBoards) {
                                                if (favorite == board.key.toString()) {
                                                    mBoard.favorite = true
                                                }
                                            }

                                            boards.add(mBoard)

                                        }

                                    }
                                } else {


                                    var isBlocked = false
                                    for (block in blockUsers) {
                                        if (block == board.child("userId").value.toString()) {
                                            isBlocked = true
                                        }
                                    }

                                    if (!isBlocked) {
                                        mBoard.id = board.key.toString()
                                        mBoard.category =
                                            board.child("category").value as String?
                                        mBoard.title = board.child("title").value.toString()
                                        mBoard.likeCount =
                                            board.child("likeCount").value.toString().toInt()
                                        mBoard.content = board.child("content").value.toString()
                                        mBoard.name = board.child("name").value.toString()
                                        mBoard.date = board.child("date").value.toString()
                                        mBoard.userId = board.child("userId").value.toString()
                                        mBoard.dislikeCount =
                                            board.child("dislikeCount").value.toString().toInt()
                                        mBoard.replyCount =
                                            board.child("replyList").childrenCount.toInt()
                                        mBoard.watcher =
                                            board.child("watcher").value.toString().toInt()


                                        if (board.child("imageUrlList").childrenCount > 0) {
                                            val boardImage = BoardImage()
                                            boardImage.id =
                                                board.child("imageUrlList").children.first().key!!.toInt()
                                            boardImage.uri =
                                                board.child("imageUrlList").children.first()
                                                    .child("uri").value as String
                                            boardImage.content =
                                                board.child("imageUrlList").children.first()
                                                    .child("content").value as String

                                            imageList.add(boardImage)

                                            mBoard.imageUrlList = imageList
                                        }


                                        for (like in isLikeDislikeBoards) {
                                            if (like.key == board.key) {
                                                mBoard.isLikeDislikeClicked =
                                                    like.value.toString().toBoolean()
                                            }
                                        }

                                        for (favorite in favoriteBoards) {
                                            if (favorite == board.key.toString()) {
                                                mBoard.favorite = true
                                            }
                                        }

                                        boards.add(mBoard)

                                    }
                                }

                            }
                        }


                    }

                    mutableLiveData.value = boards
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        return mutableLiveData
    }


    fun addFavoriteBoard(board: Board, userID: String) {
        favoriteBoardNode.child(userID).child(board.id).setValue(true)
    }

    fun removeFavoriteBoard(board: Board, userID: String) {
        favoriteBoardNode.child(userID).child(board.id).removeValue()
    }

    fun updateLikeDislikeBoard(board: Board, result: Boolean, userID: String) {
        likeBoardNode.child(userID).child(board.id).setValue(result)
    }

    fun removeLikeDislikeBoard(board: Board, userID: String) {
        likeBoardNode.child(userID).child(board.id).removeValue()
    }


    companion object {

        private var INSTANCE: BoardRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = BoardRepository(context)
            }
        }

        fun get(): BoardRepository {
            return INSTANCE ?: throw IllegalStateException("BoardRepository muse be initialized")
        }

    }
}