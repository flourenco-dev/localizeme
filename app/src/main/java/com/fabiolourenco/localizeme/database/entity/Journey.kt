package com.fabiolourenco.localizeme.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.fabiolourenco.localizeme.database.converter.LocationTypeConverter
import com.fabiolourenco.localizeme.model.JourneyLocation
import com.fabiolourenco.localizeme.model.JourneyModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Journey class that implements JourneyModel and defines the entity that will be stored in the
 * database
 */
@Entity(tableName = "journeys")
@TypeConverters(LocationTypeConverter::class)
class Journey : JourneyModel {

    @PrimaryKey(autoGenerate = true)
    private var id: Int = 0
    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var locations: ArrayList<JourneyLocation> = ArrayList()

    /**
     * Constructor for Journey using a JourneyModel
     */
    constructor(journey: JourneyModel) {
        startTime = journey.getStartTime()
        stopTime = journey.getStopTime()
        locations = journey.getLocations()
    }

    /**
     * Constructor for Journey using parameters
     */
    constructor(startTime: Long, stopTime: Long, locations: ArrayList<JourneyLocation>) {
        this.startTime = startTime
        this.stopTime = stopTime
        this.locations = locations
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getId() = id

    fun setStartTime(startTime: Long) {
        this.startTime = startTime
    }

    override fun getStartTime() = startTime

    fun setStopTime(stopTime: Long) {
        this.stopTime = stopTime
    }

    override fun getStopTime() = stopTime

    fun setLocations(locations: ArrayList<JourneyLocation>) {
        this.locations = locations
    }

    override fun getLocations() = locations

    override fun toString() = "\nJourney $id - Locations: $locations; start: $startTime; stop: $stopTime."

    /**
     * Get a string formatted with the Journey ID
     */
    fun getName() = "Journey $id"

    /**
     * Get a string formatted with the Journey ID and with the start and stop times
     */
    fun getFullName(): String {
        val format = SimpleDateFormat("HH:mm", Locale.UK)
        return "Journey $id: ${format.format(startTime)} - ${format.format(stopTime)}"
    }

    fun getAverageHigh() : String {
        var highSum = 0.0
        locations.forEach {
            highSum += it.altitude
        }
        return "Average high: ${highSum / locations.size}m"
    }

    fun getAverageSpeed() : String {
        var velocitySum = 0f
        locations.forEach {
            velocitySum += it.velocity
        }
        return "Average speed: ${velocitySum / locations.size}m/s"
    }
}