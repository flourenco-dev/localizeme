package com.fabiolourenco.localizeme.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fabiolourenco.localizeme.database.entity.Journey

/**
 * Data Access Object for accessing and operating Room database, LocalizeMeDatabase
 */
@Dao
interface JourneyDao {

    /**
     * Get all Journey objects from the database
     */
    @Query("SELECT * FROM journeys")
    fun loadJourneys(): LiveData<List<Journey>>

    /**
     * Insert a new Journey object in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(journey: Journey): Long
}