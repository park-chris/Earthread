package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Chat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

class OpenChatAdapter(
    private val mContext: Context,
    private val chatList: ArrayList<Chat>
) : RecyclerView.Adapter<OpenChatAdapter.ChatHolder>() {

    private val uid = Firebase.auth.currentUser?.uid.toString()

    interface OnItemClickListener {
        fun onItemClick(view: View, chatId: String)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ChatHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView = itemView.findViewById(R.id.chat_img)
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val lastChatText: TextView = itemView.findViewById(R.id.last_chat_text)
        private val countText: TextView = itemView.findViewById(R.id.count_text)
        private val imageRecyclerView: RecyclerView = itemView.findViewById(R.id.image_recycler_view)

        fun bind(chat: Chat) {

            titleText.text = chat.title
            lastChatText.text = chat.information

            itemView.setOnClickListener {
                listener?.onItemClick(itemView, chat.id)
            }

            val mList = arrayListOf<String>()

            for (user in chat.users.keys) {
                if (user != uid) {
                    if (chat.users[user]!!.profileImageUrl != null) {
                        mList.add(chat.users[user]!!.profileImageUrl!!)
                    }
                }
            }

            if (mList.size > 1) {
                imageRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
                imageRecyclerView.adapter = ChatImageAdapter(mContext, mList)
                imageRecyclerView.visibility = View.VISIBLE
                imageView.visibility = View.INVISIBLE
            } else if (mList.size == 1)  {

                Glide.with(mContext).load(mList[0]).circleCrop().into(imageView)

            } else
            {
                Glide.with(mContext).load(R.drawable.user).circleCrop().into(imageView)
            }

            countText.text = chat.users.keys.size.toString()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {

        val view =
            LayoutInflater.from(mContext).inflate(R.layout.open_chat_list_item, parent, false)

        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {

        val chat = chatList[position]
        holder.bind(chat)

    }

    override fun getItemCount() = chatList.size


}