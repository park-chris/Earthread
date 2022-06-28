package com.crystal.worldchat.boards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.datas.FirebaseCallback
import com.crystal.worldchat.datas.Reply
import com.crystal.worldchat.datas.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BoardViewModel: ViewModel() {

    private val boardRepository = BoardRepository.get()
    private val boardIDLiveData = MutableLiveData<String>()

    private val auth: FirebaseAuth = Firebase.auth
    private var uid: String? = auth.currentUser?.uid

    var boardLiveData: LiveData<Board?> =
        Transformations.switchMap(boardIDLiveData) {boardId ->
            boardRepository.getBoardResponse(boardId, uid!!)
        }

    fun removeStorageBoardImage(board: Board) {
        boardRepository.removeStorageBoardImage(board)
    }

    fun loadBoard(boardId: String) {
        boardIDLiveData.value = boardId
    }

    fun addBoard(board: Board) {
        boardRepository.addBoard(board)
    }

    fun addReply(board: Board, reply: Reply) {
        boardRepository.addReply(board, reply)
    }

    fun addReportedBoard(report: Report) {
        boardRepository.addReportedBoard(report)
    }

    fun removeReply(board: Board, reply: Reply) {
        boardRepository.removeReply(board, reply)
    }

    fun removeBoard(board: Board) {
        boardRepository.removeBoard(board)
    }

    fun updateBoard(board: Board, path: String) {
        boardRepository.updateBoard(board, path)
    }

    fun getBoards(userID: String,
                  keyword: String?,
                  filterList: ArrayList<String>?,
                  sort: Int?): LiveData<MutableList<Board>> {
        return boardRepository.getBoardsResponse(userID, keyword, filterList, sort)
    }

    fun getMyFavoriteBoard():  LiveData<MutableList<Board>> {
        return boardRepository.getMyFavoriteBoard(uid!!)
    }

    fun getMyBoardsResponse(): LiveData<MutableList<Board>> {
        return boardRepository.getMyBoardsResponse(uid!!)
    }

    fun addFavoriteBoard(board: Board) {
        boardRepository.addFavoriteBoard(board, uid!!)
    }

    fun removeFavoriteBoard(board: Board) {
        boardRepository.removeFavoriteBoard(board, uid!!)
    }

    fun updateLikeDislikeBoard(board: Board, result: Boolean) {
        boardRepository.updateLikeDislikeBoard(board, result, uid!!)
    }

    fun removeLikeDislikeBoard(board: Board) {
        boardRepository.removeLikeDislikeBoard(board, uid!!)
    }

    fun updateStorageBoardImage(board: Board, callback: FirebaseCallback) {
        boardRepository.updateStorageBoardImage(board, callback)
    }
}