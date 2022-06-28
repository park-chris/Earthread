package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.boards.BoardViewModel
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.utils.UIUtil

class BoardAdapter(
    private val mContext: Context,
    private val mList: List<Board>,
    private val boardViewModel: BoardViewModel
) : RecyclerView.Adapter<BoardAdapter.BoardHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, board: Board)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class BoardHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var board: Board

        private val categoryTextView: TextView = itemView.findViewById(R.id.category_text)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text)
        private val contentTextView: TextView = itemView.findViewById(R.id.content_text)
        private val nameTextView: TextView = itemView.findViewById(R.id.name_text)
        private val watcherTextView: TextView = itemView.findViewById(R.id.watcher_text)
        private val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.dislike_button)
        private val likeTextView: TextView = itemView.findViewById(R.id.like_text)
        private val dislikeTextView: TextView = itemView.findViewById(R.id.dislike_text)
        private val replyTextView: TextView = itemView.findViewById(R.id.reply_text)
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text)
        private val boardImage: ImageView = itemView.findViewById(R.id.board_image)

        fun bind(board: Board) {
            this.board = board

            titleTextView.text = board.title
            contentTextView.text = board.content
            nameTextView.text = board.name
            watcherTextView.text = board.watcher.toString()
            likeTextView.text = board.likeCount.toString()
            dislikeTextView.text = board.dislikeCount.toString()
            replyTextView.text = board.replyCount.toString()
            dateTextView.text = UIUtil.stringTimeToStringDate(board.date)

            if (board.imageUrlList.isNotEmpty()) {
                Glide.with(itemView).load(board.imageUrlList[0].uri).into(boardImage)
            } else {
                boardImage.visibility = View.GONE
            }

            if (board.category != null) {
                categoryTextView.text = board.category
            }

            if (board.isLikeDislikeClicked != null) {
                if (board.isLikeDislikeClicked!!) {
                    likeButton.setImageResource(R.drawable.ic_like_red)
                } else {
                    dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
                }
            }

            if (board.favorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_yellow)
            }

            itemView.setOnClickListener {
                listener?.onItemClick(itemView, board)
            }

            favoriteButton.setOnClickListener {

                board.favorite = if (board.favorite) {
                    favoriteButton.setImageResource(R.drawable.ic_favorite)
                    boardViewModel.removeFavoriteBoard(board)
                    false
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_yellow)
                    boardViewModel.addFavoriteBoard(board)
                    true
                }
            }

            likeButton.setOnClickListener {

                if (board.isLikeDislikeClicked != null) {

                    if (board.isLikeDislikeClicked!!) {
                        likeButton.setImageResource(R.drawable.ic_like)
                        board.likeCount -= 1
                        board.isLikeDislikeClicked = null
                        likeTextView.text = board.likeCount.toString()
                        boardViewModel.removeLikeDislikeBoard(board)

                    } else {
                        board.isLikeDislikeClicked = true
                        likeButton.setImageResource(R.drawable.ic_like_red)
                        dislikeButton.setImageResource(R.drawable.ic_dislike)
                        board.dislikeCount += 1
                        board.likeCount += 1
                        board.isLikeDislikeClicked = true
                        likeTextView.text = board.likeCount.toString()
                        dislikeTextView.text = board.dislikeCount.toString()
                        boardViewModel.updateLikeDislikeBoard(board, true)
                    }

                } else {
                    likeButton.setImageResource(R.drawable.ic_like_red)
                    board.likeCount += 1
                    board.isLikeDislikeClicked = true
                    likeTextView.text = board.likeCount.toString()
                    boardViewModel.updateLikeDislikeBoard(board, true)
                }
                boardViewModel.updateBoard(board, "dislikeCount")
                boardViewModel.updateBoard(board, "likeCount")

            }

            dislikeButton.setOnClickListener {

                if (board.isLikeDislikeClicked != null) {
                    if (board.isLikeDislikeClicked!!) {
                        dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
                        likeButton.setImageResource(R.drawable.ic_like)
                        board.likeCount -= 1
                        board.dislikeCount -= 1
                        board.isLikeDislikeClicked = false
                        likeTextView.text = board.likeCount.toString()
                        dislikeTextView.text = board.dislikeCount.toString()
                        boardViewModel.updateLikeDislikeBoard(board, false)
                    } else {
                        dislikeButton.setImageResource(R.drawable.ic_dislike)
                        board.isLikeDislikeClicked = null
                        board.dislikeCount += 1
                        dislikeTextView.text = board.dislikeCount.toString()
                        boardViewModel.removeLikeDislikeBoard(board)
                    }
                } else {
                    dislikeButton.setImageResource(R.drawable.ic_dislike_blue)
                    board.dislikeCount -= 1
                    board.isLikeDislikeClicked = false
                    dislikeTextView.text = board.dislikeCount.toString()
                    boardViewModel.updateLikeDislikeBoard(board, false)
                }
                boardViewModel.updateBoard(board, "dislikeCount")
                boardViewModel.updateBoard(board, "likeCount")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.board_list_item, parent, false)
        return BoardHolder(view)

    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {

        val board = mList[mList.size -1 - position]
        holder.bind(board)

    }

    override fun getItemCount() = mList.size

}