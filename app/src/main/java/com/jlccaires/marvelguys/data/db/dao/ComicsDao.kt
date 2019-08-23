package com.jlccaires.marvelguys.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.jlccaires.marvelguys.data.db.entity.ComicEntity
import io.reactivex.Completable

@Dao
interface ComicsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(character: List<ComicEntity>): Completable
}