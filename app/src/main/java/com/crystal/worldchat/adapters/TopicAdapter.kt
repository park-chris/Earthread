package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R

class TopicAdapter(
    private val context: Context,
    private val topicList: MutableList<String>,
    private var prefTopics: ArrayList<String>
) : RecyclerView.Adapter<TopicAdapter.TopicHolder>() {


    interface OnSavedTopic {
        fun onSavedPref(topics: ArrayList<String>)
    }

    private var listener: OnSavedTopic? = null

    fun setOnSaved(listener: OnSavedTopic) {
        this.listener = listener
    }


    inner class TopicHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val topicText: TextView = itemView.findViewById(R.id.topic_text)

        fun bind(topic: String) {
            topicText.text = topic

            if (prefTopics.size > 0) {

                for (i in 0 until prefTopics.size) {
                    if (prefTopics[i] == topic) {
                        topicText.setBackgroundResource(R.drawable.peach_solid_box)
                    }
                }
            }

            topicText.setOnClickListener {
                var isIncluded = false
                if (prefTopics.isNotEmpty()) {
                    for (i in 0 until prefTopics.size) {
                        if (topic == prefTopics[i]) {
                            isIncluded = true
                        }
                    }
                }
                if (topicText.text.toString() == "전체") {
                    if (isIncluded) {
                        topicText.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                    } else {
                        topicText.setBackgroundResource(R.drawable.peach_solid_box)
                    }
                    prefTopics = arrayListOf("전체")
                } else {
                    if (isIncluded) {
                        prefTopics.remove(topic)
                        topicText.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                    } else {
                        prefTopics.add(topic)
                        topicText.setBackgroundResource(R.drawable.peach_solid_box)
                    }
                    if (prefTopics.contains("전체")) {
                        prefTopics.remove("전체")
                    }
                }
                listener?.onSavedPref(prefTopics)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.topic_list_item, parent, false)
        return TopicHolder(view)
    }

    override fun onBindViewHolder(holder: TopicHolder, position: Int) {
        val topic = topicList[position]
        holder.bind(topic)
    }

    override fun getItemCount() = topicList.size


}