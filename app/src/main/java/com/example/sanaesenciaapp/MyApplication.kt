package com.example.sanaesenciaapp

import android.app.Application
import com.example.sanaesenciaapp.di.AppContainer

class MyApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
