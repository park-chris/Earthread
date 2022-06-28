package com.crystal.worldchat.datas

data class NotificationBody(
    val to: String,
    val data: NotificationData
) {
    data class NotificationData(
        val title: String,
        val name: String,
        val message: String
    )
}
