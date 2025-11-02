// FILE: app/src/main/java/com/smartfit/SmartFitApplication.kt

package com.smartfit

import android.app.Application
import android.util.Log
import com.smartfit.di.AppContainer

/**
 * Application class for manual dependency injection container initialization.
 */
class SmartFitApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        Log.d("SmartFitApp", "Application created - initializing dependencies")
        appContainer = AppContainer(this)
    }
}