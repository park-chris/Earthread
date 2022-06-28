package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.BoardImage

class ImageEditAdapter(
    private val mContext: Context,
    private val mList: MutableList<BoardImage>
) : RecyclerView.Adapter<ImageEditAdapter.ImageEditHolder>() {

    interface OnRemoved {
        fun onItemRemove(index: Int)
    }

    private var listener: OnRemoved? = null

    fun setOnRemoved(listener: OnRemoved) {
        this.listener = listener
    }

    inner class ImageEditHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var boardImage: BoardImage

        private val imageImg: ImageView = itemView.findViewById(R.id.image_img)
        private val imageEdt: EditText = itemView.findViewById(R.id.image_edt)
        private val removeButton: ImageButton = itemView.findViewById(R.id.remove_button)

        fun bind(boardImage: BoardImage, position: Int) {
            this.boardImage = boardImage

            Glide.with(mContext).load(boardImage.uri).into(imageImg)

            if (boardImage.content != null) {
                imageEdt.setText(boardImage.content)
            }

            imageEdt.addTextChangedListener{
                boardImage.content = imageEdt.text.toString()
            }
            
            removeButton.setOnClickListener {

                listener?.onItemRemove(position)
            }
            
            
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageEditHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_edit_item, parent, false)
        return ImageEditHolder(view)

    }

    override fun onBindViewHolder(holder: ImageEditHolder, position: Int) {

        val item = mList[position]
        holder.bind(item, position)

    }

    override fun getItemCount() = mList.size


}