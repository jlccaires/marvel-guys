package com.jlccaires.marvelguys.ui.vo

data class CharacterVo(
    val id: Int,
    val name: String,
    val thumbUrl: String? = null,
    var isFavorite: Boolean = false
)