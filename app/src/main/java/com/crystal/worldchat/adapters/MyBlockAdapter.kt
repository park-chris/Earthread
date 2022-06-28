package com.crystal.worldchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crystal.worldchat.R
import com.crystal.worldchat.datas.Block
import com.crystal.worldchat.datas.Board
import com.crystal.worldchat.utils.RemoveBlockDialog
import com.crystal.worldchat.utils.UIUtil

class MyBlockAdapter(
    private val mContext: Context,
    private val mList: MutableList<Block>
): RecyclerView.Adapter<MyBlockAdapter.BlockHolder>() {

    interface OnRemoveBlock {
        fun onSelectedBoard(string: String)
    }

    private var listener: OnRemoveBlock? = null

    fun onRemoveBlock(listener: OnRemoveBlock) {
        this.listener = listener
    }

    inner class BlockHolder(view: View): RecyclerView.ViewHolder(view) {

        private lateinit var block: Block

        private val nicknameText: TextView = itemView.findViewById(R.id.nickname_text)
        private val blockTypeText: TextView = itemView.findViewById(R.id.block_type_text)
        private val blockRemoveButton: Button = itemView.findViewById(R.id.block_remove_button)


        fun bind(block: Block) {

            this.block = block

            nicknameText.text = block.name

            if (block.message && block.board) {
                blockTypeText.text = ": 메세지, 게시물"
            } else {
                blockTypeText.text = ": 메세지"
            }

            blockRemoveButton.setOnClickListener {
                val dialog = RemoveBlockDialog(mContext)
                dialog.setOnOKClickedListener {
                    listener?.onSelectedBoard(block.uid!!)
                }
                dialog.start()
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.my_block_list_item, parent, false)
        return BlockHolder(view)
    }

    override fun onBindViewHolder(holder: BlockHolder, position: Int) {
        val block = mList[position]
        holder.bind(block)
    }

    override fun getItemCount() = mList.size

}