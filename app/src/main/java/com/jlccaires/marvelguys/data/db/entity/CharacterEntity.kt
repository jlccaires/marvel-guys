package com.jlccaires.marvelguys.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Character")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val thumbUrl: String,
    val syncing: Boolean = true
)