package com.medha.mapwithcoordinatorlayout

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MapApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        LocationHelper.context = this
        Helper.context = this
    }
}