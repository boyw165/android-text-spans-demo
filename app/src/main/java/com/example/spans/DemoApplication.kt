package com.example.spans

import androidx.multidex.MultiDexApplication
import timber.log.Timber
import timber.log.Timber.DebugTree

class DemoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())
    }
}