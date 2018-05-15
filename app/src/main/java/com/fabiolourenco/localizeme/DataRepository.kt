package com.fabiolourenco.localizeme

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.fabiolourenco.localizeme.database.LocalizeMeDatabase
import com.fabiolourenco.localizeme.database.entity.Journey
import java.util.concurrent.Executors

/**
 * Application data repository to manage all data of the app
 */
class DataRepository(private val database: LocalizeMeDatabase) {
    private val observableJourneys: MediatorLiveData<ArrayList<Journey>> = MediatorLiveData()
    // Executor to perform data operations without overloading the MainThread
    private val dataExecutor = Executors.newSingleThreadExecutor()

    /**
     * Get the list of journeys from the database and get notified when the data changes
     */
    val journeys: LiveData<ArrayList<Journey>>
        get() = observableJourneys

    init {
        observableJourneys.addSource(database.journeyDao().loadJourneys()) { journeys ->
            observableJourneys.postValue(ArrayList(journeys))
        }
    }

    /**
     * Insert a new Journey
     */
    fun insertJourney(journey: Journey) {
        dataExecutor.execute {
            database.journeyDao().insert(journey)
        }
    }

    companion object {
        private var instance: DataRepository? = null

        /**
         * Create DataRepository as singleton to prevent having multiple instances
         */
        fun getInstance(database: LocalizeMeDatabase): DataRepository {
            if (instance == null) {
                synchronized(DataRepository::class) {
                    if (instance == null) {
                        instance = DataRepository(database)
                    }
                }
            }
            return instance!!
        }
    }
}