package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Notification

class NotificationAdapter(
    private val mContext: Context,
    private val mList: ArrayList<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotiHolder>() {

    inner class NotiHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val questionText: TextView = itemView.findViewById(R.id.question_text)
        private val answerText: TextView = itemView.findViewById(R.id.answer_text)

        fun bind(notification: Notification) {

            questionText.text = notification.question
            notification.answer.replace(".", ".\n")
            answerText.text = notification.answer


            itemView.setOnClickListener {

                if (answerText.isVisible) {
                    answerText.visibility = View.GONE

                } else {
                    answerText.visibility = View.VISIBLE
                }

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.notification_list_item, parent, false)
        return NotiHolder(view)
    }

    override fun onBindViewHolder(holder: NotiHolder, position: Int) {
        val notification = mList[position]
        holder.bind(notification)
    }

    override fun getItemCount() = mList.size

}