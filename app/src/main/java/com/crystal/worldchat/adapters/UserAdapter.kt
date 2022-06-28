package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.User
import com.crystal.worldchat.utils.BlockDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserAdapter(
    private val mContext: Context,
    private val mList: MutableList<User>,
): RecyclerView.Adapter<UserAdapter.UserHolder>() {

    interface OnSelected {
        fun onSelectedItem(result: Boolean, uid: String)
    }

    private var listener: OnSelected? = null
    private val auth: FirebaseAuth = Firebase.auth

    fun setOnSelected(listener: OnSelected) {
        this.listener = listener
    }

    inner class UserHolder(view: View): RecyclerView.ViewHolder(view) {

        private val uidText: TextView = itemView.findViewById(R.id.uid_text)
        private val arrowImageView: ImageView = itemView.findViewById(R.id.arrow_image_view)
        private val meText: TextView = itemView.findViewById(R.id.me_text)

        fun bind(user: User) {
            uidText.text = user.name

            if (auth.currentUser!!.uid == user.uid) {
                arrowImageView.visibility = View.GONE
                meText.visibility = View.VISIBLE
            } else {
                itemView.setOnClickListener {
                    val dialog = BlockDialog(mContext)
                    dialog.setOnOKClickedListener { result, uid ->
                        listener?.onSelectedItem(result, uid)
                    }
                    dialog.start(mContext, user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = mList[position]
        holder.bind(user)
    }

    override fun getItemCount() = mList.size
}