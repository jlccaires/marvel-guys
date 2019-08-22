package com.jlccaires.marvelguys.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val thumbUrl: String,
    val description: String? = null
)