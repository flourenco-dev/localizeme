package com.fabiolourenco.localizeme.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.fabiolourenco.localizeme.database.dao.JourneyDao
import com.fabiolourenco.localizeme.database.entity.Journey

/**
 * Database class to store apps data
 */
@Database(entities = [Journey::class], version = 1)
abstract class LocalizeMeDatabase : RoomDatabase() {

    abstract fun journeyDao(): JourneyDao

    companion object {
        private const val DATABASE_NAME = "localize-me-db"

        private var instance: LocalizeMeDatabase? = null

        /**
         * Create Database as singleton to prevent having multiple instances of the database opened
         * at the same time
         */
        fun getInstance(context: Context): LocalizeMeDatabase {
            if (instance == null) {
                synchronized(LocalizeMeDatabase::class) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(context,
                                LocalizeMeDatabase::class.java, DATABASE_NAME).build()
                    }
                }
            }
            // By now instance must be not null
            return instance!!
        }
    }
}