package com.crystal.worldchat.datas

data class Comment(
    var uid: String = "",
    var message: String = "",
    var time: String = "",
    var imageUrl: String? = null,
    var readUser: HashMap<String, Any> = HashMap()
)
