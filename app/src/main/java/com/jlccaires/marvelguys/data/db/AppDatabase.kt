package com.jlccaires.marvelguys.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity

@Database(entities = [CharacterEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        fun getDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "marvel_favs")
                .build()
    }

    abstract fun characterDao(): CharacterDao
}