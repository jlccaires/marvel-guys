package com.jlccaires.marvelguys.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "serie",
    indices = [Index("characterId")],
    foreignKeys = [ForeignKey(
        entity = CharacterEntity::class,
        parentColumns = ["id"],
        childColumns = ["characterId"],
        onDelete = CASCADE
    )]
)
data class SerieEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val thumbUrl: String,
    val characterId: Int
)