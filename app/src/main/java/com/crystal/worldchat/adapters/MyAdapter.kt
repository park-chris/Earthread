package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.utils.UIUtil

class MyAdapter(
    private val mContext: Context,
    private val mList: MutableList<Board>
): RecyclerView.Adapter<MyAdapter.ReplyHolder>() {

    interface OnSelectedBoard {
        fun onSelectedBoard(string: String)
    }

    private var listener: OnSelectedBoard? = null

    fun selectedBoard(listener: OnSelectedBoard) {
        this.listener = listener
    }

    inner class ReplyHolder(view: View): RecyclerView.ViewHolder(view) {


        private lateinit var board: Board

        private val typeText: TextView = itemView.findViewById(R.id.type_text)
        private val contentText: TextView = itemView.findViewById(R.id.content_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)

        fun bind(board: Board) {
            this.board = board

            if (board.isLikeDislikeClicked != null) {
                if (board.isLikeDislikeClicked!!) {
                    typeText.text = "게시물"
                    contentText.text = board.title + "을 등록했습니다."

                } else {
                    typeText.text = "댓글"
                    contentText.text = board.title + "에 댓글을 등록했습니다."

                }
            } else {
                typeText.text = board.id
            }


            dateText.text = UIUtil.stringTimeToStringDateTime(board.date)

            itemView.setOnClickListener {
                listener?.onSelectedBoard(board.id)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.my_list_item, parent, false)
        return ReplyHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
        val reply = mList[position]
        holder.bind(reply)
    }

    override fun getItemCount() = mList.size

}