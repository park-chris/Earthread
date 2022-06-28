package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R

class MenuAdapter(
    private val mContext: Context,
    private val mList: Array<String>,
): RecyclerView.Adapter<MenuAdapter.UserHolder>() {

    interface OnSelectedMenu {
        fun onSelectedMenu(string: String)
    }

    private var listener: OnSelectedMenu? = null

    fun selectedMenu(listener: OnSelectedMenu) {
        this.listener = listener
    }

    inner class UserHolder(view: View): RecyclerView.ViewHolder(view) {

        private val menuText: TextView = itemView.findViewById(R.id.menu_text)

        fun bind(menu: String) {
            menuText.text = menu

            itemView.setOnClickListener {
                listener?.onSelectedMenu(menu)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.menu_list_item, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val menu = mList[position]
        holder.bind(menu)
    }

    override fun getItemCount() = mList.size
}