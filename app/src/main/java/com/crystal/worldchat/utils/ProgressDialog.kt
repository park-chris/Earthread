package com.crystal.worldchat.utils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import com.crystal.worldchat.R

class ProgressDialog(context: Context) {

    private val dialog = AppCompatDialog(context)

    fun on(context: Context) {
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_progress)
        dialog.show()
    }

    fun off() {
        dialog.dismiss()
    }

}