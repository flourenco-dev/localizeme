package com.fabiolourenco.localizeme.model

/**
 * This interface is used to define the basic methods that a Journey must have
 */
interface JourneyModel {

    fun getStartTime(): Long
    fun getStopTime(): Long
    fun getLocations(): ArrayList<JourneyLocation>
}