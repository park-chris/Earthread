package com.crystal.worldchat.utils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import com.crystal.worldchat.R

class NotificationDialog(context: Context) {

    private val dialog = AppCompatDialog(context)

    fun on() {
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_notification)
        dialog.show()
    }

    fun off() {
        dialog.dismiss()
    }

}