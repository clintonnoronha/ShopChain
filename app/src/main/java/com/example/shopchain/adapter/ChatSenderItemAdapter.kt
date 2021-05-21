package com.example.shopchain.adapter

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.shopchain.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatSenderItemAdapter(
    val context: Context,
    val text: String = "",
    val fileUrl: String = "",
    val mediaUrl: String = "",
    val fileName: String = ""
): Item<ViewHolder>() {


    override fun getLayout(): Int {
        return R.layout.chat_sender_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (text != "") {
            viewHolder.itemView.findViewById<TextView>(R.id.txtSenderMessage).visibility = View.VISIBLE
            viewHolder.itemView.findViewById<RelativeLayout>(R.id.rlMedia).visibility = View.GONE
            viewHolder.itemView.findViewById<TextView>(R.id.txtSenderMessage).text = text
        } else if (mediaUrl != "") {
            viewHolder.itemView.findViewById<TextView>(R.id.txtSenderMessage).visibility = View.GONE
            viewHolder.itemView.findViewById<RelativeLayout>(R.id.rlMedia).visibility = View.VISIBLE
            viewHolder.itemView.findViewById<TextView>(R.id.txtMediaName).text = fileName
        }
    }
}