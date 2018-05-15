package com.fabiolourenco.localizeme

import android.app.Application
import com.fabiolourenco.localizeme.database.LocalizeMeDatabase
import timber.log.Timber

/**
 * Android Application class. Used for accessing DataRepository and LocalizeMeDatabase singletons,
 * and to initialize Timber (in this case, just for debug)
 */
class LocalizeMeApplication: Application() {

    val database: LocalizeMeDatabase
        get() = LocalizeMeDatabase.getInstance(this)

    val repository: DataRepository
        get() = DataRepository.getInstance(database)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}