package com.jlccaires.marvelguys.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface CharacterDao {

    @Query("SELECT * FROM Character ORDER BY name ASC")
    fun list(): Flowable<List<CharacterEntity>>

    @Query("SELECT * FROM Character WHERE id = :id")
    fun byId(id: Int): Maybe<CharacterEntity>

    @Insert
    fun insert(character: CharacterEntity): Completable

    @Update
    fun update(character: CharacterEntity): Completable

    @Query("DELETE FROM Character WHERE id = :id")
    fun delete(id: Int): Completable
}