package com.crystal.worldchat.datas

data class User(
    var email: String? = null,
    var name: String? = null,
    var profileImageUrl: String? = null,
    var uid: String? = null,
    var myBoard: MutableList<Board>? = null,
    var myReply: MutableList<Board>? = null,
    var token: String? = null
    )
