package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R

class ChatImageAdapter(
    private val mContext: Context,
    private val mList: List<String>
) : RecyclerView.Adapter<ChatImageAdapter.BoardImageHolder>() {

    inner class BoardImageHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageImg: ImageView = itemView.findViewById(R.id.image_img)

        fun bind(uri: String) {
            Glide.with(mContext).load(uri).into(imageImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatImageAdapter.BoardImageHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.chat_image_list_item, parent, false)
        return BoardImageHolder(view)

    }

    override fun onBindViewHolder(holder: ChatImageAdapter.BoardImageHolder, position: Int) {

        val item = mList[position]
        holder.bind(item)

    }

    override fun getItemCount() = mList.size

}