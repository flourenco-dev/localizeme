package com.fabiolourenco.localizeme.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.fabiolourenco.localizeme.LocalizeMeApplication
import com.fabiolourenco.localizeme.database.entity.Journey

/**
 * ViewModel to handle bridge all logic data related with Journeys between the repository and the UI
 */
class JourneysViewModel(application: Application): AndroidViewModel(application) {

    // Use instance of DataRepository to handle Journeys related data
    private val repository = (application as LocalizeMeApplication).repository
    // MediatorLiveData can observe other LiveData objects and react on their emissions
    var observableJourneys: MediatorLiveData<ArrayList<Journey>> = MediatorLiveData()

    val journeys: LiveData<ArrayList<Journey>>
        get() = observableJourneys

    init {
        // Set to null by default, until we get data from the database
        observableJourneys.value = null

        // Observe the changes of the Journeys from the database and forward them
        observableJourneys.addSource(repository.journeys, { journeys ->
            observableJourneys.value = journeys
        })
    }

    /**
     * Insert a new Journey
     */
    fun insertJourney(journey: Journey) {
        repository.insertJourney(journey)
    }
}