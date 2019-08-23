package com.jlccaires.marvelguys.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CharacterDao {

    @Query("SELECT * FROM Character ORDER BY name ASC")
    fun list(): Flowable<List<CharacterEntity>>

    @Query("SELECT COUNT(*) FROM Character WHERE id = :id")
    fun exists(id: Int): Single<Int>

    @Insert
    fun insert(character: CharacterEntity): Completable

    @Query("DELETE FROM Character WHERE id = :id")
    fun delete(id: Int): Completable
}