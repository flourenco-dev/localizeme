package com.fabiolourenco.localizeme.database.converter

import android.arch.persistence.room.TypeConverter
import com.fabiolourenco.localizeme.model.JourneyLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * LocationTypeConverter will convert an ArrayList of JourneyLocation objects into a JSON string in
 * order to store in the database
 */
class LocationTypeConverter {
    private val type = object : TypeToken<ArrayList<JourneyLocation>>() {}.type

    /**
     * Convert a String into an ArrayList of locations, represented by JourneyLocation objects
     */
    @TypeConverter
    fun stringToLocations(json: String): ArrayList<JourneyLocation>? {
        return Gson().fromJson<ArrayList<JourneyLocation>>(json, type)
    }

    /**
     * Convert an ArrayList of locations, represented by JourneyLocation objects, into a String
     */
    @TypeConverter
    fun locationsToString(list: ArrayList<JourneyLocation>): String {
        return Gson().toJson(list, type)
    }

}