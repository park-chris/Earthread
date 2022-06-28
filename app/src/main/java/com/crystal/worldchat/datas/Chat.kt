package com.crystal.worldchat.datas

import java.util.*
import kotlin.collections.HashMap

data class Chat(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var users: HashMap<String, User?> = HashMap(),
    val comments: MutableList<Comment> = mutableListOf(),
    var opened: Boolean? = null,
    var information: String? = null
)
