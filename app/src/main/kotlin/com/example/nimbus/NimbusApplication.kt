package com.example.nimbus

import android.app.Application
import com.example.nimbus.di.AppContainer

/** Owns the dependency graph for the process; screens reach it through [MainActivity]. */
class NimbusApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
