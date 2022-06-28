package com.crystal.worldchat.datas

import java.util.*

data class Board(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var category: String? = null,
    var name: String? = null,
    var likeCount: Int = 0,
    var dislikeCount: Int = 0,
    var userId: String = "",
    var content: String? = null,
    var imageUrlList: List<BoardImage> = listOf(),
    var replyList: MutableList<Reply> = mutableListOf(),
    var replyCount: Int = 0,
    var watcher: Int = 0,
    var favorite: Boolean = false,
    var isLikeDislikeClicked: Boolean? = null,
    var date: String = ""
)