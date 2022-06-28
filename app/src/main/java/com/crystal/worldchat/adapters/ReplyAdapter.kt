package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Reply
import com.crystal.worldchat.datas.Report
import com.crystal.worldchat.utils.RemoveDialog
import com.crystal.worldchat.utils.ReportDialog
import com.crystal.worldchat.utils.UIUtil

class ReplyAdapter(
    private val mContext: Context,
    private val mList: MutableList<Reply>,
    private val firebaseUid: String
): RecyclerView.Adapter<ReplyAdapter.ReplyHolder>() {

    interface OnRemoved {
        fun onItemRemove(reply: Reply)
    }

    interface OnReported {
        fun onItemReported(report: Report)
    }

    interface OnModified {
        fun onItemModified(reply: Reply)
    }

    interface OnSelectedItem {
        fun onSelectedItem(reply: Reply)
    }

    private var listener: OnRemoved? = null
    private var reportedListener: OnReported? = null
    private var modifiedListener: OnModified? = null
    private var onSelectedListener: OnSelectedItem? = null

    fun setOnModified(listener: OnModified) {
        this.modifiedListener = listener
    }

    fun setOnReported(listener: OnReported) {
        this.reportedListener = listener
    }

    fun setOnRemoved(listener: OnRemoved) {
        this.listener = listener
    }

    fun setOnSelected(listener: OnSelectedItem) {
        this.onSelectedListener = listener
    }

    inner class ReplyHolder(view: View): RecyclerView.ViewHolder(view) {

        private lateinit var reply: Reply

        private val nameText: TextView = itemView.findViewById(R.id.name_text)
        private val contentText: TextView = itemView.findViewById(R.id.content_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val reportText: TextView = itemView.findViewById(R.id.report_text)
        private val modifyText: TextView = itemView.findViewById(R.id.modify_text)
        private val removeText: TextView = itemView.findViewById(R.id.remove_text)
        private val messageImageButton: ImageButton = itemView.findViewById(R.id.message_image_button)

        fun bind(reply: Reply) {
            this.reply = reply

            nameText.text = reply.name
            contentText.text = reply.content
            dateText.text = UIUtil.stringTimeToStringDate(reply.date)

            if (reply.userId == firebaseUid) {
                reportText.visibility = View.GONE
                modifyText.visibility = View.VISIBLE
                removeText.visibility = View.VISIBLE
            }

            modifyText.setOnClickListener {
                modifiedListener?.onItemModified(reply)
            }

            messageImageButton.setOnClickListener {
                onSelectedListener?.onSelectedItem(reply)
            }

            reportText.setOnClickListener {
                val dialog = ReportDialog(mContext)
                dialog.setOnOKClickedListener {  content ->

                    val report = Report()
                    report.content = content
                    report.replyID = "${reply.userId} - ${reply.date}"
                    reportedListener?.onItemReported(report)
                }
                dialog.start(mContext)
            }

            removeText.setOnClickListener {
                val dialog = RemoveDialog(mContext)

                dialog.setOnOKClickedListener {
                    listener?.onItemRemove(reply)
                }
                dialog.start()

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.reply_list_item, parent, false)
        return ReplyHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
        val reply = mList[position]
        holder.bind(reply)
    }

    override fun getItemCount() = mList.size

}