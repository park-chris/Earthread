package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.BoardImage

class BoardImageAdapter(
    private val mContext: Context,
    private val mList: List<BoardImage>
) : RecyclerView.Adapter<BoardImageAdapter.BoardImageHolder>() {

    inner class BoardImageHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var boardImage: BoardImage

        private val imageImg: ImageView = itemView.findViewById(R.id.image_img)
        private val imageText: TextView = itemView.findViewById(R.id.image_text)

        fun bind(boardImage: BoardImage) {
            this.boardImage = boardImage

            Glide.with(mContext).load(boardImage.uri).into(imageImg)
            imageText.text = boardImage.content
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardImageAdapter.BoardImageHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.board_image_list_item, parent, false)
        return BoardImageHolder(view)

    }

    override fun onBindViewHolder(holder: BoardImageAdapter.BoardImageHolder, position: Int) {

        val item = mList[position]
        holder.bind(item)

    }

    override fun getItemCount() = mList.size

}