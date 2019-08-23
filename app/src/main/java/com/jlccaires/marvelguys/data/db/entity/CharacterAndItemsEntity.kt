package com.jlccaires.marvelguys.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CharacterAndItemsEntity(
    @Embedded
    val characterEntity: CharacterEntity,
    @Relation(entity = ComicEntity::class, entityColumn = "characterId", parentColumn = "id")
    val comics: List<ComicEntity>,
    @Relation(entity = SerieEntity::class, entityColumn = "characterId", parentColumn = "id")
    val series: List<SerieEntity>
)