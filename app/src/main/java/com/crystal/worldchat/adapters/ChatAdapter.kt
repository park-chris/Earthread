package com.crystal.worldchat.adapters

import android.content.Context
import android.util.Log
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

class ChatAdapter(
    private val mContext: Context,
    private val chatList: ArrayList<Chat>
) : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

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
        private val peopleCountText: TextView = itemView.findViewById(R.id.people_count_text)
        private val imageRecyclerView: RecyclerView = itemView.findViewById(R.id.image_recycler_view)

        fun bind(chat: Chat) {

            var count = 0

            if (chat.title != null) {
                titleText.text = chat.title
                peopleCountText.text = chat.users.keys.size.toString()

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
            }
             else {

                 if (chat.users.keys.size == 1 && chat.users.containsKey(uid)) {
                     titleText.text = "알수없음"
                     Glide.with(mContext).load(R.drawable.user).circleCrop().into(imageView)

                 }  else {
                     for (user in chat.users.keys) {
                         if (user != uid) {
                             titleText.text = chat.users[user]?.name
                             if (chat.users[user]?.profileImageUrl != null) {
                                 Glide.with(mContext).load(chat.users[user]!!.profileImageUrl)
                                     .circleCrop()
                                     .into(imageView)
                             } else {
                                 Glide.with(mContext).load(R.drawable.user).circleCrop().into(imageView)
                             }

                         }
                     }
                 }

            }

            if (chat.comments.isNotEmpty()) {
                lastChatText.text = chat.comments[chat.comments.size - 1].message
                if (chat.comments[chat.comments.size - 1].imageUrl != null && lastChatText.text.isEmpty()) {
                    lastChatText.text = mContext.resources.getString(R.string.send_image)
                }


                for (i in 0 until chat.comments.size) {
                    if (chat.comments[chat.comments.size - i -1].readUser.containsKey(uid) && chat.comments[chat.comments.size - i -1].readUser.get(uid) == true) {
                        count = i
                        break
                    }
                }

                if (count <= 0  ) {
                    countText.visibility = View.GONE
                }else {
                    countText.text = count.toString()
                    countText.visibility = View.VISIBLE
                }
            }

            itemView.setOnClickListener {
                listener?.onItemClick(itemView, chat.id)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item, parent, false)

        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {

        val chat = chatList[position]
        holder.bind(chat)

    }

    override fun getItemCount() = chatList.size


}