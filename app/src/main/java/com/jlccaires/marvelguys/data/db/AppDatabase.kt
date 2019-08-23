package com.jlccaires.marvelguys.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jlccaires.marvelguys.data.db.dao.CharacterDao
import com.jlccaires.marvelguys.data.db.dao.ComicsDao
import com.jlccaires.marvelguys.data.db.dao.SeriesDao
import com.jlccaires.marvelguys.data.db.entity.CharacterEntity
import com.jlccaires.marvelguys.data.db.entity.ComicEntity
import com.jlccaires.marvelguys.data.db.entity.SerieEntity

@Database(
    entities = [CharacterEntity::class, ComicEntity::class, SerieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        fun getDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "marvel_favs")
                .build()
    }

    abstract fun characterDao(): CharacterDao

    abstract fun comicsDao(): ComicsDao

    abstract fun seriesDao(): SeriesDao
}