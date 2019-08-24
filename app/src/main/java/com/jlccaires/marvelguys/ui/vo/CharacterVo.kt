package com.jlccaires.marvelguys.ui.vo

data class CharacterVo(
    val id: Int,
    val name: String,
    val thumbUrl: String,
    var isFavorite: Boolean = false,
    var syncing: Boolean = false
)