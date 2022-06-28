package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Comment
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.utils.UIUtil

class MessageAdapter(
    private val context: Context,
    private val messageList: MutableList<Comment>,
    private val uid: String,
    private val users: HashMap<String, User?>
) : RecyclerView.Adapter<MessageAdapter.BaseViewHolder<*>>() {

    interface OnItemClickListener {
        fun onItemClick(imageURI: String)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    private val ME = 0
    private val ANOTHER = 1

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T, list: MutableList<Int>)
    }

    inner class ViewHolder1(itemView: View) : BaseViewHolder<Comment>(itemView) {

        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val imgDateText: TextView = itemView.findViewById(R.id.img_date_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val dateDividerText: TextView = itemView.findViewById(R.id.date_divider_text)
        private val commentImg: ImageView = itemView.findViewById(R.id.comment_img)
        private val topLayout: ConstraintLayout = itemView.findViewById(R.id.top_layout)
        private val countText: TextView = itemView.findViewById(R.id.count_text)
        private val imgCountText: TextView = itemView.findViewById(R.id.img_count_text)

        override fun bind(item: Comment, list: MutableList<Int>) {
            messageText.text = item.message

            var count = 0
            for (key in item.readUser.keys) {
                if (item.readUser[key] == true) {
                    count += 1
                }
            }
            if (count != item.readUser.keys.size) {
                countText.text = (item.readUser.keys.size - count).toString()
                imgCountText.text = (item.readUser.keys.size - count).toString()
            }


            if (item.imageUrl != null) {
                topLayout.visibility = View.VISIBLE
                commentImg.visibility = View.VISIBLE
                imgCountText.visibility = View.VISIBLE

                Glide.with(context).load(item.imageUrl).into(commentImg)
                commentImg.setOnClickListener {
                    listener?.onItemClick(item.imageUrl!!)
                }
                if (item.message == "") {
                    messageText.visibility = View.GONE
                    imgDateText.text = UIUtil.formattedTime(item.time)
                    dateText.visibility = View.GONE
                    countText.visibility = View.GONE
                }
            }

            if (bindingAdapterPosition == 0) {
                dateText.text = UIUtil.formattedTime(item.time)

            } else {

                dateText.text =
                    UIUtil.compareTimeStamp(item.time, messageList[bindingAdapterPosition - 1].time)
            }

            for (i in list) {
                if (i == bindingAdapterPosition) {

                    dateDividerText.visibility = View.VISIBLE
                    dateDividerText.text = UIUtil.formattedYearDay(item.time)
                }
            }

        }
    }

    inner class ViewHolder2(itemView: View) : BaseViewHolder<Comment>(itemView) {

        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val dateDividerText: TextView = itemView.findViewById(R.id.date_divider_text)
        private val imgDateText: TextView = itemView.findViewById(R.id.img_date_text)
        private val commentImg: ImageView = itemView.findViewById(R.id.comment_img)
        private val topLayout: ConstraintLayout = itemView.findViewById(R.id.top_layout)
        private val nicknameText: TextView = itemView.findViewById(R.id.nickname_text)
        private val countText: TextView = itemView.findViewById(R.id.count_text)
        private val imgCountText: TextView = itemView.findViewById(R.id.img_count_text)


        override fun bind(item: Comment, list: MutableList<Int>) {
            messageText.text = item.message


            var count = 0
            for (key in item.readUser.keys) {
                if (item.readUser[key] == true) {
                    count += 1
                }
            }
            if (count != item.readUser.keys.size) {
                countText.text = (item.readUser.keys.size - count).toString()
                imgCountText.text = (item.readUser.keys.size - count).toString()
            }



            if (item.imageUrl != null) {
                topLayout.visibility = View.VISIBLE
                commentImg.visibility = View.VISIBLE
                imgCountText.visibility = View.VISIBLE

                Glide.with(context).load(item.imageUrl).into(commentImg)
                if (item.message == "") {
                    messageText.visibility = View.GONE
                    imgDateText.text = UIUtil.formattedTime(item.time)
                    dateText.visibility = View.GONE
                    countText.visibility = View.GONE

                }
            }


            if (bindingAdapterPosition == 0) {
                dateText.text = UIUtil.formattedTime(item.time)

                nicknameText.visibility = View.VISIBLE
                if (users.get(item.uid) != null) {
                    nicknameText.text = users.get(item.uid)!!.name
                }

            } else {
                dateText.text =
                    UIUtil.compareTimeStamp(item.time, messageList[bindingAdapterPosition - 1].time)

                if (UIUtil.compareTimeStamp(
                        item.time,
                        messageList[bindingAdapterPosition - 1].time
                    ) != ""
                ) {
                    nicknameText.visibility = View.VISIBLE
                    if (users.get(item.uid) != null) {
                        nicknameText.text = users.get(item.uid)!!.name
                    }
                }
            }



            for (i in list) {
                if (i == bindingAdapterPosition) {
                    dateDividerText.visibility = View.VISIBLE
                    dateDividerText.text = UIUtil.formattedYearDay(item.time)
                }
            }


        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ME -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.message_list_item1, parent, false)
                ViewHolder1(view)
            }
            ANOTHER -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.message_list_item2, parent, false)
                ViewHolder2(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        val dividerList = mutableListOf<Int>()

        for (i in 0 until messageList.size) {
            if (i == 0) {
                dividerList.add(i)
            } else {
                if (UIUtil.compareYearDay(messageList[i].time, messageList[i - 1].time)) {
                    dividerList.add(i)
                }
            }
        }

        val element = messageList[position]
        when (holder) {
            is ViewHolder1 -> holder.bind(element, dividerList)
            is ViewHolder2 -> holder.bind(element, dividerList)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {

        return if (messageList[position].uid == uid) {
            0
        } else if (messageList[position].uid != uid) {
            1
        } else {

            throw IllegalArgumentException()
        }

    }

    override fun getItemCount() = messageList.size

}